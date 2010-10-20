
use strict;
use warnings;

my $notelevel = 0;
my $resolution_height  = 600;
my $viewport = 0.8 * $resolution_height; # 80% of the resolution height

my $measure_size = $viewport * 0.8 * 1; # 80% of the viewport, times the speed

my $ojm_file = shift;

my $ojn = $ojm_file; $ojn =~ s/ojm$/ojn/;

my $samples_hash = extract_samples($ojm_file);
my ($note_array,$header) = parse_ojn($ojn);
play($note_array, $samples_hash,$header);
exit;

sub play
{
	my($note_list,$samples,$header) = @_;
	my $bpm = $header->{'bpm'};
	@$note_list = sort{ $a->{'measure'}+$a->{'pos'} <=> $b->{'measure'}+$b->{'pos'} } @$note_list;
	my $total_measures = @$note_list[-1]->{'measure'};

	my $buffer_offset = 0;
	my $now_measure = 0;
	my $this_measure_size;
	my $sub_measure = 0;
	my $measure_delta = $bpm/420;
	my $time = 0;
	while($now_measure < $total_measures)
	{
		$sub_measure = 0;

		# gathering the notes of this measure
		my @notes;
		push @notes, shift @$note_list while @$note_list && @$note_list[0]->{'measure'} == int($now_measure);

		foreach my $n(@notes)
		{
			if($n->{'channel'} == 0){ # fractional measure
				$this_measure_size = $measure_size * $n->{'value'};
			}
			elsif($n->{'channel'} == 1){ # BPM changing
				my $newbpm = $n->{'value'};
				$bpm = $newbpm;
				$measure_delta = $bpm/420;
			}
			else{ # notes
				if($sub_measure < $n->{'pos'}){
					select(undef, undef, undef, ($n->{'pos'}-$sub_measure)*$measure_delta);
				}
# 				dispatch($samples->{$n->{'type'}?0:5}{$n->{'value'}-1});
				$time += ($n->{'pos'}-$sub_measure)*$measure_delta;
			}
			$sub_measure += $n->{'pos'};
		}
		print "time: $time\n";
		$now_measure++;
		
	}
}

sub dispatch
{
	my ($data) = @_;
# 	return if fork;
# 	open MP, "| mplayer -really-quiet -";
# 	print MP $data;
# 	close MP;
}

sub parse_ojn
{
	my($file) = @_;
	open my $OJN, $file or die $!;
	binmode $OJN;

	my $data;
	read $OJN, $data, 300 or die $!;

	my $h = unpack2hash(join(' ',qw/
	i:$songid
	a4:$signature
	c4:@encoder_value
	i:$genre
	f:$bpm
	s4:@level
	i3:@event_count
	i3:@note_count
	i3:@measure_count
	i3:@package_count
	s:$unk_1D
	s:$unk_songid
	a20:$unk_oldgenre
	i:$bmp_size
	s2:@unk_a
	a64:$title
	a32:$artist
	Z32:$noter
	Z32:$ojm_file
	i:$cover_size
	i3:@time
	i4:@note_offset
	/), $data);

	my @notepos = @{$h->{'note_offset'}};

	my $bpm = $h->{'bpm'};
	my ($artist,$title,$noter) = ($h->{'artist'},$h->{'title'},$h->{'noter'});

	my ($startpos,$endpos) = ($notepos[$notelevel],$notepos[$notelevel + 1]);

	seek $OJN, $startpos, 0; # go to pos where lvl starts

	#### note parsing phase ####

	my @note_list;
	while(!eof $OJN && tell $OJN < $endpos)
	{
		read $OJN, $data, 8;
		my ($measure,$channel,$events_count) = unpack 'lss', $data;
		if($channel >= 0 && $channel < 9)
		{
			for my $i(0 .. $events_count-1)
			{
				read $OJN, $data, 4;

				my ($value, $unk, $type);
				if($channel == 0 || $channel == 1) # fractional measure or BPM change
				{
					($value) = unpack 'f', $data;
				}else{
					($value, $unk, $type) = unpack 'sCC', $data;
				}
				next if $value == 0;

				push @note_list, {
				'channel' => $channel,
				'measure' => $measure,
				'pos'     => $i / $events_count,
				'value'   => $value,
				'type'    => $type,
				};
			}
		}else{ ## jumping channels > 8, here is probably auto-play notes
	# 		seek $OJN, 4 * $events_count, 1;
			for(1..$events_count)
			{
				read $OJN, $data, 4;
				my ($value, $unk, $type) = unpack 'sCC', $data;
				next if $value == 0;
			}
		}
	}
	return (\@note_list, $h);
}

sub extract_samples
{
	my ($file) = @_;
	open DATA, $file or die $!;
	binmode DATA;

	my $header;
	read DATA, $header, 28 or die $!;

	my $h = unpack2hash(join(' ',qw/
	Z4:$signature
	c8:@unk_fixed
	s:$sample_count
	c6:@unk_fixed2
	i:$payload_size
	i:$unk_zero
	/), $header);

	die "Not a M30 file\n" unless $h->{'signature'} eq "M30";

	my $buf;
	my $samples_hash = {};
	while(!eof DATA)
	{
		read DATA, $header, 52 or die $!;
		my $nh = unpack2hash(join(' ',qw/
		a32:$sample_name
		i:$sample_size
		c:$unk_sample_type
		c:$unk_off
		s:$fixed_2
		i:$unk_sample_type2
		s:$ref
		s:$unk_zero
		c3:@wut2
		c:$unk_counter
		/), $header);
		$samples_hash->{$nh->{'unk_sample_type'}}{$nh->{'ref'}} = dump_ogg($nh->{'sample_size'});
	}
	return $samples_hash;
}

sub dump_ogg
{
	my ($sample_size) = @_;
	my $buf;
	read DATA, $buf, $sample_size;
	$buf = nami_xor($buf);
	return $buf;
}


sub nami_xor
{
	my ($data) = @_;
	my $nami = 'nami';
	my $bytes = length $data;
	my $mask = $nami x ($bytes/4);
	$mask .= substr $nami, 0, ($bytes%4);
	return $data ^ $mask;
}

sub unpack2hash
{
	my ($template, $source) = @_;
	my $hash = {};
	foreach(split ' ',$template)
	{
		my ($temp,$type,$var) = split /:(.)/;
		if($type eq '@')
		{
			@{$hash->{$var}} = unpack $temp, $source;
		}elsif($type eq '$')
		{
			$hash->{$var} = unpack $temp, $source;
		}
		else{ die "need context type\n" }
		substr $source, 0, length(pack $temp), '';
	}
	return $hash;
}


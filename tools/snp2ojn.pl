#!/usr/bin/perl
use strict;
use warnings;
use Storable qw/dclone/;
use XML::Simple;
use Compress::Zlib;
use File::Temp qw/tempdir tempfile/;
use Data::Dumper;

=pod

SNP to OJN/OJM converter

all that is necessary is the SNP file and the 
MusicInfo.xml(that can be found inside Data/DATA.SNP)
must be on the current directory of the script

Run as "perl snp2ojn.pl <mah_file.snp>"

The script will generate <title>.ojn and <title>.ojm

WARN: input SNP file must have to original name e.g: C0001.SNP
WARN2: don't forget to remove the comments from MusicInfo.xml

=cut

# 1/1024 is the smaller possible note,
# well, not really, but the note is impossible way before 1/1024
# so I don't think we will ever get to this limit
my $MAX_SM = 2**10;

# error tolerance
my $epsilon = 0.001;

# there's also "krazyhard", but o2jam only support 3 sections
# we could make SHD versions with normal, hard, krazyhard later..
my @extract_levels = ('easy', 'normal', 'hard');

my $header_template = {
	'artist' => 'artist',
	'title' => 'title',
	'noter' => 'noter',
	'ojm_file' => 'nofile.ojm',
	'bpm' => 120,
	'genre' => 10,
	'end_offset' => 0,
};
for (@extract_levels) {
	$header_template->{$_} = {
		'event_count' => 0,
		'note_count' => 0,
		'measure_count' => 0,
		'package_count' => 0,
		'offset' => 0,
		'time' => 0,
		'level' => 0,
	}
}

my $TEMP_DIR = tempdir(CLEANUP => 1);

dump_snp($ARGV[0]);
exit(0);

sub dump_snp
{
	my ($snp_file) = @_;
	$snp_file =~ /C(\d+)\.SNP/i;
	my $code = $1;
	
	my $metadata = read_musicfile($code);
	my @files = grep{$_->{'key'} == 7}@{$metadata->{'Note'}};
	my $input_files = {};
	for my $f(@files) {
		$input_files->{$f->{'difficult'}} = $f;
	}

	for(@extract_levels) {
		unless(defined $input_files->{$_}) {
			die "DEAD: there's no metadata for level [$_]";
		}
	}
	
	open my $SNP_FILE, $snp_file or die $!;
	binmode $SNP_FILE;
	my $snp_index = snp_build_index($SNP_FILE);
	
	my $ojn_file = $metadata->{'title'}.".ojn";
	my $ojm_file = $metadata->{'title'}.".ojm";
	
	open my $OJN, ">$ojn_file" or die $!;
	binmode $OJN;

	my $header = dclone($header_template);
	$header->{'title'} = $metadata->{'title'};
	$header->{'artist'} = $metadata->{'Composer'};  # i guess ?
	$header->{'noter'} = $metadata->{'Pattern'};
	$header->{'bpm'} = $metadata->{'bpm'}; # lol
	$header->{'ojm_file'} = $ojm_file;
	$metadata->{'playtime'} =~ /(\d+):(\d+)/;
	for my $k(@extract_levels) {
		$header->{$k}{'level'} = $input_files->{$k}->{'level'};
		$header->{$k}{'time'} = $1 * 60 + $2;
	}
	
	print_header($OJN, $header);
	my $samples = print_notes($OJN, $SNP_FILE, $snp_index, $input_files, $header);
	print_header($OJN, $header); ## update the header
	close $OJN;

	open my $OJM, ">$ojm_file" or die $!;
	binmode $OJM;
	dump_ojm($OJM, $SNP_FILE, $snp_index, $samples);
	close $OJM;
}

sub dump_ojm # M30 - plain OGG
{
	my ($OJM, $snp_fh, $snp_index, $samples) = @_;

	print $OJM pack 'Z4', "M30\0";
	print $OJM pack 'i', 196608; # file_format_version 
	print $OJM pack 'i', 0; # encryption_flag, I hope 0 means none for everyone
	print $OJM pack 'i', scalar keys %$samples;
	print $OJM pack 'i', 28; # samples offset
	print $OJM pack 'i', 0; # payload size, NEED UPDATE
	print $OJM pack 'i', 0; # padding

	for my $id(keys %$samples)
	{
		my $s_name = $samples->{$id}{'name'};
		unless(defined $snp_index->{$s_name}) {
			warn "WARNING: sample [$id][$s_name] not found on SNP, skipping";
			next;
		}
		my $data_fh = snp_get_file($snp_fh, $snp_index->{$s_name});
	
		print $OJM pack 'Z32', $s_name;
		print $OJM pack 'i', $snp_index->{$s_name}{'sizeOriginal'};
		print $OJM pack 's', 5; # 5: note key
		print $OJM pack 's', 2; # unk_fixed
		print $OJM pack 'i', 1; # unk_music_flag
		print $OJM pack 's', $id; # ref
		print $OJM pack 's', 0; # unk_zero
		print $OJM pack 'i', 0; # pcm_samples, TODO: see if anyone is gonna cry if I leave this zero

		my $data;
		read $data_fh, $data, $snp_index->{$s_name}{'sizeOriginal'};
		print $OJM $data;
	}
	my $payload_size = tell($OJM) - 28;
	seek $OJM, 20, 0;
	print $OJM pack 'i', $payload_size;
}

sub print_notes
{
	my ($OJN, $snp_fh, $index, $files, $header) = @_;

	my $sample_files = {};
	for my $k(@extract_levels)
	{
		my $f = $files->{$k}->{'xnt_file'};
		my $fh = snp_get_file($snp_fh, $index->{$f});
		my @notes = readXNT($fh);
		my @samples = readSamples($fh);
		$sample_files->{$_->{'id'}} = $_ for @samples; #TODO is it indexing by id or by name ?

		@notes = normalize_notes(@notes);	
	
		@notes = sort {
			my $i = $a->{'measure'} <=> $b->{'measure'};
			return $i if $i != 0;
			my $j = $a->{'channel'} <=> $b->{'channel'};
			return $j if $j != 0;
			return $a->{'position'} <=> $b->{'position'};
		} @notes;

		$header->{$k}{'note_count'} = scalar grep{$_->{'channel'} > 1 && $_->{'channel'} < 9 } @notes;
		$header->{$k}{'offset'} = tell $OJN;

		my ($measure, $channel, $event_count, $package_count) = (-1,-1,0,0);	
		my @ev_bag = ();
		for my $e(@notes)
		{
			if($measure != $e->{'measure'} ||
			   $channel != $e->{'channel'}) { # new measure/channel
				$event_count += scalar @ev_bag;
				$package_count++;
				print_package($OJN, \@ev_bag);
				@ev_bag = ();
				$measure = $e->{'measure'};
				$channel = $e->{'channel'};
			}
			push @ev_bag, $e;
		}
		$event_count += scalar @ev_bag;
		$package_count++;
		$header->{$k}{'event_count'} = $event_count;
		$header->{$k}{'measure_count'} = $measure;
		$header->{$k}{'package_count'} = $package_count;
		print_package($OJN, \@ev_bag); # last package
	}
	$header->{'end_offset'} = tell $OJN;
	return $sample_files;
}

sub print_package {
	my ($OJN, $ee) = @_;
	my @notes = sort { $a->{'position'} <=> $b->{'position'} }@$ee;
	return unless scalar @notes > 0;

	# we need to find the number of events based on 
	# how the notes are distributed on the measure
	my $total_events = $MAX_SM / multigcf($MAX_SM,map{int($_->{'position'}*$MAX_SM)}@notes);

	if($total_events != int($total_events)) {
		# this means that there are notes smaller than 1/$MAX_SM in this measure
		die "DEAD: WTF man, take your impossible song with you and get out of here";
	}

	print $OJN pack 'i', $notes[0]->{'measure'};
	print $OJN pack 's', $notes[0]->{'channel'}; 
	print $OJN pack 's', $total_events;

	my @events;
	my $bag_i = 0;
	for(my $i = 0; $i<$total_events; $i++) {

		if($bag_i <= $#notes) {
			my $apx = $notes[$bag_i]->{'position'} * $total_events;
		
			if(int($apx) - $i <= $epsilon) {
				#if($apx != int($apx)) {
				#	warn "WARNING: approximating uneven position [$apx]";
				#}
				print_event($OJN, $notes[$bag_i]);
				$bag_i++;
				next;
			}
		}
		print_event($OJN, {'value'=>0,'note_type'=>0});
	}
	if($bag_i < $#notes) {
		die "DEAD: couldn't fit all notes of channel[".$notes[0]->{'measure'}."] measure[".$notes[0]->{'channel'}."]"; 
	}
}

sub normalize_notes { # break long notes to ojn style
	my @notes = @_;
	my @extra_notes;
	for my $n(@notes) {
		if($n->{'channel'} < 2) {
			next;
		}
		$n->{'value'}++; # o2jam ignore 0 so push everyone 1 up
		if($n->{'channel'} > 8) {
			$n->{'note_type'} = 0;
			next;
		}
		if($n->{'length'} > 0) # long note
		{
			my $e = dclone($n);
			$n->{'note_type'} = 2; # longnote start
			$e->{'note_type'} = 3; # longnote end

			$e->{'position'} += $e->{'length'};
			my $em = int $e->{'position'}; # extra measures
			$e->{'position'} -= $em;
			$e->{'measure'} += $em;
			
			push @extra_notes, $e;
		}
		else { # normal note
			$n->{'note_type'} = 0; 
		}
	}
	push @notes, @extra_notes;
	return @notes;
}

sub print_event {
	my ($OJN, $n) = @_;
	if(defined $n->{'channel'} && $n->{'channel'} == 1) { # bpm change
		print $OJN pack 'f', $n->{'value'};
	} 
	else { # normal note
		print $OJN pack 's', $n->{'value'};
		print $OJN pack 'c', 0; # vol & pan
		print $OJN pack 'c', $n->{'note_type'};
	}
}

sub print_header
{
	my ($OJN, $info) = @_;
	seek $OJN, 0, 0; # header always at the start
	print $OJN pack 'i', 0; # songid
	print $OJN pack 'a4', "ojn\0";
	print $OJN pack 'f', 2.9; # encode_version
	print $OJN pack 'i', $info->{'genre'};
	print $OJN pack 'f', $info->{'bpm'};
	print $OJN pack 's3', map { $info->{$_}{'level'} }@extract_levels;
	print $OJN pack 's', 0;
	print $OJN pack 'i3', map { $info->{$_}{'event_count'} }@extract_levels;
	print $OJN pack 'i3', map { $info->{$_}{'note_count'} }@extract_levels;
	print $OJN pack 'i3', map { $info->{$_}{'measure_count'} }@extract_levels;
	print $OJN pack 'i3', map { $info->{$_}{'package_count'} }@extract_levels;
	print $OJN pack 's', 0; # old_encode_version
	print $OJN pack 's', 0; # old_songid
	print $OJN pack 'a20', "";  # old_genre
	print $OJN pack 'i', 0; # bmp_size
	print $OJN pack 'i', 0;  # old_file_version
	print $OJN pack 'Z64', $info->{'title'};
	print $OJN pack 'Z32', $info->{'artist'}; 
	print $OJN pack 'a32', $info->{'noter'};
	print $OJN pack 'Z32', $info->{'ojm_file'};
	print $OJN pack 'i', 0; # cover_size
	print $OJN pack 'i3', map { $info->{$_}{'time'} }@extract_levels;
	print $OJN pack 'i3', map { $info->{$_}{'offset'} }@extract_levels;
	print $OJN pack 'i', $info->{'end_offset'};
}

sub readXNT
{
	my ($fh) = @_;
	my ($header, @items);
	read $fh, $header, 11 or die $!;
	my $h = unpack2hash(join(' ',qw/
	Z4:$signature
	s:$unk
	i:$segments
	c:$unk2
	/), $header);

	if($h->{'segments'} == 3) {
		push @items, readBpmChange($fh);
	}
	#else {
		push @items, readNote($fh, 1); #keysounds
		push @items, readNote($fh, 0); #bgm
	#}
	return @items;
}

sub readNote
{
	my ($fh, $isKeysound) = @_;
	my ($header, @items);
	read $fh, $header, 16 or die $!;
	my $h = unpack2hash(join(' ',qw/
	c12:@garbage
	i:$count
	/), $header);

	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read $fh, $header, 14 or die $!;
		my $n = unpack2hash(join(' ',qw/
		c:$zero
		S:$measure
		f:$position
		C:$channel
		S:$sample_id
		f:$holdLength
		/), $header);

		if($isKeysound) { $n->{'channel'} += 1 }# adjust for o2jam(1~7 -> 2~8)
		else { $n->{'channel'} += 10; } # separate notes from bgm

		push @items, { 'measure' => $n->{'measure'}, 'channel' => $n->{'channel'}, 'position' => $n->{'position'},
				'value' => $n->{'sample_id'}, 'length' => $n->{'holdLength'} };
	}
	return @items;
}

sub readBpmChange 
{
	my ($fh) = @_;
	my ($header, @items);
	read $fh, $header, 16 or die $!;
	my $h = unpack2hash(join(' ',qw/
	c12:$garbage
	i:$count
	/), $header);

	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read $fh, $header, 14 or die $!;
		my $n = unpack2hash(join(' ',qw/
		c:$zero
		S:$measure
		f:$position
		c3:$skip
		f:$bpm
		/), $header);
		
		push @items, { 'measure' => $n->{'measure'}, 'channel' => 1, 'position' => $n->{'position'},
				'value' => $n->{'bpm'}, 'length' => 0, 'note_type' => 0 };
	}
	return @items;
}

sub readSamples
{
	my ($fh) = @_;
	my ($header, @items);
	read $fh, $header, 4 or die $!;
	my $h = unpack2hash(join(' ',qw/
	i:$count
	/), $header);
	
	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read $fh, $header, 8 or die $!;
		my $n = unpack2hash(join(' ',qw/
		S:$id
		s:$skip
		i:$name_len
		/), $header);
		
		read $fh, $header, $n->{'name_len'} or die $!;
		my $n2 = unpack2hash(join(' ',qw/
		Z*:$name
		/), $header);

		push @items, { 'id' => $n->{'id'}, 'name' => $n2->{'name'} };
	}
	return @items;
}

sub snp_build_index
{
	my ($fh) = @_;
	my ($index, $data);
	seek $fh, 0, 0;
	read $fh, $data, 24 or die $!;
	my $main_header = unpack2hash(join(' ',qw/
	Z8:$signature                   
	i:$unk                          
	i:$file_count                   
	i:$dir_count                    
	i:$size                         
	/), $data);
	while(!eof $fh)
	{
		read $fh, $data, 145 or die $!;
		my $header = unpack2hash(join(' ',qw/
		c:$isDir
		Z128:$name
		i:$sizeOriginal
		i:$sizePacked
		i:$parentDirOffset
		i:$nextAdressOffset
		/), $data);

		if($header->{'sizePacked'} > 0) {
			$header->{'offset'} = tell $fh;
			$index->{$header->{'name'}} = $header;
		}
		seek $fh, $header->{'sizePacked'}, 1;
	}
	return $index;
}

sub snp_get_file
{
	my ($snp_fh, $inf) = @_;
	my $data;
	seek $snp_fh, $inf->{'offset'}, 0;
	read $snp_fh, $data, $inf->{'sizePacked'};
	$data = uncompress($data);
	my $fh = tempfile(DIR => $TEMP_DIR);
	print $fh $data;
	seek $fh, 0, 0;
	return $fh;
}

sub read_musicfile
{
	my ($code) = @_;
	my $xml = XMLin('MusicInfo.xml', keyattr => 0) or die $!;
	$xml = $xml->{'Music'};
	($xml) = grep { $_->{'id'} == $code }@$xml;
	die "DEAD: could not find the song metadata" unless defined $xml;
	return $xml;
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

sub gcf {
  my ($x, $y) = @_;
  ($x, $y) = ($y, $x % $y) while $y;
  return $x;
}

sub multigcf {
  my $x = shift;
  $x = gcf($x, shift) while @_;
  return $x;
}



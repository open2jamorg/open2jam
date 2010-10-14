#!/usr/bin/perl
use strict;
use warnings;
use GD;
use Data::Dumper;

#
# ojnparser.pl - OJN Parser and PNG Render
# this is the implementation-reference for the other modules
#
# this implementation only (purposely) differs from the original algorithm
# in the graphics generated and in the note width variance
#

my $filename = shift;
my $notelevel = 2; # 0,1,2 <-> Easy,Normal,Hard, which note rank to print
my $speed = 1; # hi-speed

# this is the space height the user can see at a time,
# which is only defined here because the measure size is relative to this
my $resolution_height  = 600;
my $viewport = 0.8 * $resolution_height; # 80% of the resolution height

my $measure_size = $viewport * 0.8 * $speed; # 80% of the viewport, times the speed
my $sub_measures = 4; # sub measure demarcation

my $left_pad = 100; # left side padding

my $note_height = 7; # vertical size of a note
my $note_width = 28; # horizontal size of a note
my $csize = 200; # comments space size


# map the channels to a easier to render conf
my %channel_map  = (
1 => 'BPM',
2 => 0,
3 => 1,
4 => 2,
5 => 3,
6 => 4,
7 => 5,
8 => 6
);

my $OJN;
open $OJN, $filename or die $!;
binmode $OJN;

#### header parsing phase ####

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

$h->{$_} =~ s/\0//g for keys %$h;

my @notepos = @{$h->{'note_offset'}};

my $bpm = $h->{'bpm'};
my ($artist,$title,$noter) = ($h->{'artist'},$h->{'title'},$h->{'noter'});

my ($startpos,$endpos) = ($notepos[$notelevel],$notepos[$notelevel + 1]);

seek $OJN, $startpos, 0; # go to pos where lvl starts

#### note parsing phase ####

my @note;
my $total_measures = 0;
while(!eof $OJN && tell $OJN < $endpos)
{
	read $OJN, $data, 8;
	my ($measure,$channel,$events_count) = unpack 'lss', $data;
	$total_measures = $measure if ($measure > $total_measures);

	print STDERR "m: $measure, bb: $events_count\n";
	if(defined $channel_map{$channel})
	{
		for my $i(0 .. $events_count-1)
		{
			my ($value, $unk, $long_note);
			if($channel == 1)
			{
				read $OJN, $data, 4;
				($value) = unpack 'f', $data;
			}else{
				read $OJN, $data, 4;
				($value, $unk, $long_note) = unpack 'sCC', $data;
			}
			next if($value == 0);

			push @note, {
			'channel' => $channel_map{$channel},
			'measure'    => $measure + ($i / $events_count),
			'value'   => $value,
			'long'    => $long_note,
			};
		}
	}else{ ## jumping undefined channels, here is probably auto-play notes
		seek $OJN, 4 * $events_count, 1;
	}
}

#### render phase ####

my ($width,$height) = (($note_width * 7) + 2*$left_pad, ($total_measures * $measure_size) + $csize);

my $im = GD::Image->new($width, $height);

my @color;
my $black  = $im->colorAllocate(  0,  0,  0);  # black
$color[0]  = $im->colorAllocate(255,255,255);  # white
$color[1]  = $im->colorAllocate(241,241,243);  # white
$color[2]  = $im->colorAllocate( 68,212,246);  # cyan
$color[3]  = $im->colorAllocate(254,204, 83);  # yellow
$color[4]  = $im->colorAllocate( 50, 50, 50);  # dark gray
$color[5]  = $im->colorAllocate(120,120,120);  # gray
$color[6]  = $im->colorAllocate(255,150,150);  # pink
$color[7]  = $im->colorAllocate(180,180,180);  # light gray
$color[11] = $im->colorAllocate(198,194,198);  # light gray
$color[12] = $im->colorAllocate(104,185,193);  # cyan
$color[13] = $im->colorAllocate(204,197,126);  # light yellow

# vertical lines, delimiter the note space
my $x = $left_pad;
$im->line($x, 0, $x, $height, $color[5]); 
for my $i(1 .. 6)
{
	$x += $note_width;
	$im->line($x, 0, $x, $height, $color[4]);
}
$im->line($x + $note_width, 0, $x + $note_width, $height, $color[5]);

## measure and sub_measure marker lines
for my $measure(0 .. $total_measures)
{
	my $x2 = $left_pad + (7 * $note_width);
	my $y  = $height - ($measure * $measure_size);

	$im->line($left_pad, $y, $x2, $y, $color[5]);
	$im->string(gdSmallFont, $left_pad + (7 * $note_width) + 4, $y, sprintf("#%03d",$measure), $color[5]);

	for my $sub_measure(1..$sub_measures-1)
	{
		$y  = $height - ($measure+($sub_measure/$sub_measures)) * $measure_size;
		$im->line($left_pad, $y, $x2, $y, $color[4]);
	}
}

my @lch; # to store long notes
my %stat = ( # gather some stats
    'tap'  => 0,
    'long' => 0,
    'min'  => $bpm,
    'max'  => $bpm
);

foreach my $n(@note)
{
	my $channel = $n->{'channel'};
	if($channel eq 'BPM')
	{
		my $newbpm = $n->{'value'};
		my $x = $left_pad + (7 * $note_width) + 4;
		my $y = $height - ($measure_size * $n->{'measure'});
		$im->string(gdSmallFont, $x, $y,sprintf("BPM %.3f -> %.3f", $bpm, $newbpm), $color[6]);
		$stat{'min'} = $newbpm if ($newbpm < $stat{'min'});
		$stat{'max'} = $newbpm if ($newbpm > $stat{'max'});
		$bpm = $newbpm;
	}else{
		my $c;
		$c = 1 if($channel % 2 == 0); ## white notes
		$c = 2 if($channel % 2 != 0); ## blue notes
		$c = 3 if($channel == 3);     ## yellow note

		my $x = $left_pad + ($channel * $note_width);
		my $y = $height - ($measure_size * $n->{'measure'}) - 1; # apparently it is always 1 pixel higher

		if($n->{'long'} == 0) # tap note
		{
			$stat{'tap'}++;
			$im->filledRectangle($x, $y - $note_height, $x+$note_width, $y, $color[$c]);
		}elsif($n->{'long'} == 2) # start long note
		{
			$stat{'long'}++;
			$lch[$channel] = $n->{'measure'};
		}else{ # end long note
			my $y_old = $height - ($measure_size * $lch[$channel]) - 1;
			$im->filledRectangle($x, $y, $x+$note_width, $y_old, $color[10 + $c]);
			delete $lch[$channel];
		}
	}
}

### add some metadata and statistics to the image

my @lvl_str = map{"[$_$speed]"} ('Ex','Nx','Hx');
my $str =  "$lvl_str[$notelevel] $artist - $title";
my 
$y  = 20;$im->string(gdSmallFont, 24, $y, $str, $color[0]);
$y += 20;$im->string(gdSmallFont, 24, $y, $noter, $color[1]);
$y += 15;


my $bpm_text = sprintf('%.3f (%.3f ~ %.3f)',$h->{'bpm'},$stat{'min'},$stat{'max'});
$y +=  2;$im->string(gdSmallFont, 24, $y, " - Tap Notes:  " . $stat{'tap'},  $color[7]);
$y += 15;$im->string(gdSmallFont, 24, $y, " - Long Notes: " . $stat{'long'}, $color[7]);
$y += 15;$im->string(gdSmallFont, 24, $y, " - BPM:        " . $bpm_text,  $color[7]);


binmode STDOUT;
print $im->png;

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
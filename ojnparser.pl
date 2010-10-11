#!/usr/bin/perl
use strict;
use warnings;
use GD;
use Data::Dumper;


my $filename = shift;
my $notelevel = 2; # 0,1,2 <-> E,N,H, which note level to print

my @lvll = ('[Ex]','[Nx]','[Hx]');

my $beat_size = 400; # pixels
my $sub_beats = 4; # sub beat demarcation

my $left_pad = 100; # left side padding

my $note_height = 7; # vertical size of a note
my $note_width = 45; # horizontal size of a note
my $csize = 200; # comments space size


my %map  = (
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
s:$unk_h1D
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

my @note;
my $total_beats = 0;
while(!eof $OJN && tell $OJN < $endpos)
{
	read $OJN, $data, 8;
	my ($beat,$channel,$events_count) = unpack 'lss', $data;
	$total_beats = $beat if ($beat > $total_beats);

	if(defined $map{$channel})
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
			'channel' => $map{$channel},
			'beat'    => $beat + ($i / $events_count),
			'value'   => $value,
			'long'    => $long_note,
			};
			print STDERR "unk $unk\n";
		}
	}else{
		seek $OJN, 4 * $events_count, 1; ## jumping what ??
	}
}

my ($width,$height) = (($note_width * 7) + 2*$left_pad, ($total_beats * $beat_size) + $csize);

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

## beat and sub_beat marker lines
for my $beat(0 .. $total_beats)
{
	my $x2 = $left_pad + (7 * $note_width);
	my $y  = $height - ($beat * $beat_size);

	$im->line($left_pad, $y, $x2, $y, $color[5]);
	$im->string(gdSmallFont, $left_pad + (7 * $note_width) + 4, $y, sprintf("#%03d",$beat), $color[5]);

	for my $sub_beat(1..$sub_beats-1)
	{
		$y  = $height - ($beat+($sub_beat/$sub_beats)) * $beat_size;
		$im->line($left_pad, $y, $x2, $y, $color[4]);
	}
}

my @lch; # to store long notes
my %stat = (
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
		my $y = $height - ($beat_size * $n->{'beat'}) - $note_height;
		$im->string(gdSmallFont, $x, $y,sprintf("BPM %.2f -> %.2f", $bpm, $newbpm), $color[6]);
		$stat{'min'} = $newbpm if ($newbpm < $stat{'min'});
		$stat{'max'} = $newbpm if ($newbpm > $stat{'max'});
		$bpm = $newbpm;
	}else{
		my $c;
		$c = 1 if($channel % 2 == 0); ## white notes
		$c = 2 if($channel % 2 != 0); ## blue notes
		$c = 3 if($channel == 3);     ## yellow note

		my $x = $left_pad + ($channel * $note_width);

		if($n->{'long'} == 0) # tap note
		{
			$stat{'tap'}++;
			my $y = $height - ($beat_size * $n->{'beat'});
			$im->filledRectangle($x, $y - $note_height, $x+$note_width, $y, $color[$c]);
		}elsif($n->{'long'} == 2) # start long note
		{
			$stat{'long'}++;
			$lch[$channel] = $n->{'beat'};
		}else{ # end long note
			my $y1 = $height - ($beat_size * $lch[$channel]);
			my $y2 = $height - ($beat_size * $n->{'beat'});
			$im->filledRectangle($x, $y2, $x+$note_width, $y1, $color[10 + $c]);
			delete $lch[$channel];
		}
	}
}

my 
$y  = 20;$im->string(gdSmallFont, 24, $y, "$lvll[$notelevel] $artist - $title", $color[0]);
$y += 20;  $im->string(gdSmallFont, 24, $y, $noter, $color[1]);
$y += 15;

my $st = ' [' . sprintf("%.3d",$stat{'min'}) . '-' . sprintf("%.3d",$stat{'max'}) . ']';
$y += 2; $im->string(gdSmallFont, 24, $y, " - Tap Notes:  " . $stat{'tap'},  $color[7]);
$y += 15;$im->string(gdSmallFont, 24, $y, " - Long Notes: " . $stat{'long'}, $color[7]);
$y += 15;$im->string(gdSmallFont, 24, $y, " - BPM:        " . sprintf("%.3d",$bpm) . $st,  $color[7]);


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
#!/usr/bin/perl
use strict;
use warnings;
use GD;


my $filename = shift;

my $sub_beats = 6; # note distribution in the beat

my $sub_beat_size = 50; # beat spacing size

my @lvll = ('[Ex]','[Nx]','[Hx]');


my $NOTEPAD = 100; # left side padding
my $notelevel = 2; # 0,1,2 <-> E,N,H
my $note_height = 7; # vertical size of a note
my $note_width = 45; # horizontal size of a note
my $csize = 200; # comments space size



my %map  = (
1 => 'BZM',
2 => 0,
3 => 1,
4 => 2,
5 => 3,
6 => 4,
7 => 5,
8 => 6
);

open DATA, $filename or die $!;
binmode DATA;

my $data;
read DATA, $data, 300 or die $!;

my $h = unpack2hash(join(' ',qw/
l:$songid
c8:@signature
l:$genre
f:$bpm
s3:@level
s:$unk_0
l3:@unk2
l3:@notecount
l3:@time_related
s7:@unk3
s:$songid2
s10:@unk_zeroes
Z8:$unk_k
Z64:$title
Z32:$artist
Z32:$noter
Z32:$musicfile
l:$jpg
l3:@time
l4:@notepos
/), $data);


my @notepos = @{$h->{'notepos'}};

my $bpm = $h->{'bpm'};
my ($artist,$title,$noter) = ($h->{'artist'},$h->{'title'},$h->{'noter'});

my ($startpos,$endpos) = ($notepos[$notelevel],$notepos[$notelevel + 1]);

seek DATA, $startpos, 0; # go to pos where lvl starts

my @note;
my $total_sub_beats = 0;
while(!eof DATA && tell DATA < $endpos)
{
	read DATA, $data, 8;
	my ($measure,$channel,$length) = unpack 'lss', $data;

	if(defined $map{$channel})
	{
		for my $i(0 .. $length-1)
		{
			my @value;
			if($channel == 1)
			{
				read DATA, $data, 4;
				$value[0] = unpack 'f', $data;
			}else{
				read DATA, $data, 4;
				@value = unpack 'sCC', $data;
			}

			next if($value[0] == 0);

			my $beat = ($sub_beats * ($measure + ($i / $length)));
			$total_sub_beats = $beat if ($beat > $total_sub_beats);

			my $mychan;
			if($channel == 1)
			{
				$mychan = 'BZM';
			}else{
				$mychan = $map{$channel} + ($value[2] & 2 ? 10 : 0); # longnote ? 10 : 0
			}

			push @note, {
			'channel' => $mychan,
			'beat'    => $beat,
			'value'   => $value[0],
			'rawchan' => $channel
			};
			printf STDERR "%03.2f -- chan(%02d) ".($beat / $sub_beats)."\n",$beat,$mychan;
		}
	}else{
		seek DATA, 4 * $length, 1; ## jumping what ??

	}
}
$total_sub_beats = int($total_sub_beats+0.5);

my ($width,$height) = (($note_width * 7) + 2 * $NOTEPAD, ($total_sub_beats * $sub_beat_size) + $csize);

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
$color[11] = $im->colorAllocate(193,193,194);  # light gray
$color[12] = $im->colorAllocate( 54,170,197);  # light blue
$color[13] = $im->colorAllocate(203,163, 66);  # light brown
$color[21] = $im->colorAllocate(248,243,247);  # white
$color[22] = $im->colorAllocate(130,231,241);  # light cyan
$color[23] = $im->colorAllocate(255,246,157);  # light yellow
$color[31] = $im->colorAllocate(198,194,198);  # light gray
$color[32] = $im->colorAllocate(104,185,193);  # cyan
$color[33] = $im->colorAllocate(204,197,126);  # light yellow



# vertical lines, delimiter the note space
for my $i(0 .. 7)
{
	my $x = $NOTEPAD + ($i * $note_width);
	$im->line($x, 0, $x, $height, $color[4]);
}
$im->line($NOTEPAD, 0, $NOTEPAD, $height, $color[5]); 
$im->line($NOTEPAD + (7 * $note_width), 0, $NOTEPAD + (7 * $note_width), $height, $color[5]);


## horizontal lines
for my $i(0 .. $total_sub_beats)
{
	my $x2 = $NOTEPAD + (7 * $note_width);
	my $y  = $height - ($sub_beat_size * $i);
	$im->line($NOTEPAD, $y, $x2, $y, $color[4]);
	if ($i % $sub_beats == 0) # beat mark
	{
		$im->line($NOTEPAD, $y - 1, $x2, $y - 1, $color[5]);
		$im->string(gdSmallFont, $NOTEPAD + (7 * $note_width) + 4, $y - 10, sprintf("#%03d",$i/$sub_beats), $color[5]);
	}
}

my @lch; # to store long notes
my %stat = (
    'tap'  => 0,
    'long' => 0,
    'min'  => $bpm,
    'max'  => $bpm
);

foreach my $v(@note)
{
	my $channel = $v->{'channel'};
	if($channel eq 'BZM') # bpm changing ??
	{
		my $newbpm = $v->{'value'};
		my $y = $height - ($sub_beat_size * $v->{'beat'}) - $note_height;
		my $text = sprintf "%.2f", $newbpm;

		$im->string(gdSmallFont, $NOTEPAD - (length($text) * 9)-4, $y - 10, $text, $color[6]);

		$stat{'min'} = $newbpm if ($newbpm < $stat{'min'});
		$stat{'max'} = $newbpm if ($newbpm > $stat{'max'});
	}else{
		my $c = 1;
		$c = 2 if ($channel % 2 == 1);        
		$c = 3 if (($channel % 10 ) == 3);
		if ($channel >= 0 && $channel <= 6) # tap note
		{
			$stat{'tap'}++;
			my $x = $NOTEPAD + ($channel * $note_width);
			my $y = $height - ($sub_beat_size * $v->{'beat'});
			$im->filledRectangle($x, $y, $x+$note_width, $y-$note_height, $color[$c]);
		}elsif($channel >= 10 && $channel <= 16) # long note
		{
			$stat{'long'}++;
			if (!defined($lch[$channel]))
			{
				$lch[$channel] = $v->{'beat'};
			}else{
				my $x = $NOTEPAD + (($channel - 10) * $note_width);
				my $y = $height - ($sub_beat_size * $v->{'beat'}) - $note_height;
				my $z = $height - ($sub_beat_size * $lch[$channel]) - $note_height;
				delete $lch[$channel];
				$im->filledRectangle($x,$y,$x+$note_width,$z+$note_height, $color[20 + $c]);
			}
		}
	}
}


my $y = 20;$im->string(gdSmallFont, 24, $y, "$lvll[$notelevel] $artist - $title", $color[0]);
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
	foreach(split / /,$template)
	{
		my ($temp,$type,$var) = split /:(.)/;
		if($type eq '@')
		{
			my @r = unpack $temp, $source;
			$hash->{$var} = \@r;
			substr $source, 0, length(pack $temp, @r), '';
		}elsif($type eq '$')
		{
			my $r = unpack $temp, $source;
			$hash->{$var} = $r;
			substr $source, 0, length(pack $temp, $r), '';
		}
		else{ die "need context type\n" }
	}
	return $hash;
}







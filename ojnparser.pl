#!/usr/bin/perl
use strict;
use warnings;
use Data::Dumper;
$|++;


my $filename = shift;

my $speed = 2;
my $unk_beat = 8*$speed; # note distribution in the beat
#
# unk_beat = 8 -- x1 speed !!
#
my $beatsize = 4*$unk_beat; # beat spacing size

my @lvll = ('[Ex]','[Nx]','[Hx]');
my $NOTEPAD = 100; # left side padding
my $notelevel = 2; # 0,1,2 <-> E,N,H
my $notesize = 10; #7 vertical size of a note
my $notewidth = 50; #20 horizontal size of a note
my $csize = 200; # comments space size



my @color;
my $black  = "rgb(  0,  0,  0)";  # black
$color[0]  = "rgb(255,255,255)";  # white
$color[1]  = "rgb(241,241,243)";  # white
$color[2]  = "rgb( 68,212,246)";  # cyan
$color[3]  = "rgb(254,204, 83)";  # yellow
$color[4]  = "rgb( 50, 50, 50)";  # dark gray
$color[5]  = "rgb(120,120,120)";  # gray
$color[6]  = "rgb(255,150,150)";  # pink
$color[7]  = "rgb(180,180,180)";  # light gray
$color[11] = "rgb(193,193,194)";  # light gray
$color[12] = "rgb( 54,170,197)";  # light blue
$color[13] = "rgb(203,163, 66)";  # light brown
$color[21] = "rgb(248,243,247)";  # white
$color[22] = "rgb(130,231,241)";  # light cyan
$color[23] = "rgb(255,246,157)";  # light yellow
$color[31] = "rgb(198,194,198)";  # light gray
$color[32] = "rgb(104,185,193)";  # cyan
$color[33] = "rgb(204,197,126)";  # light yellow


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

#die Dumper $h;

my @notepos = @{$h->{'notepos'}};

my $bpm = $h->{'bpm'};
my ($artist,$title,$noter) = ($h->{'artist'},$h->{'title'},$h->{'noter'});

my ($startpos,$endpos) = ($notepos[$notelevel],$notepos[$notelevel + 1]);

seek DATA, $startpos, 0; # go to pos where lvl starts

my @note;
my $maxbeat = 0;
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

			my $beat = (4 * ($measure + ($i / $length)));
			$maxbeat = $beat if ($beat > $maxbeat);

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
		}
	}else{
		seek DATA, 4 * $length, 1; ## jumping what ??

	}
}

my ($width,$height) = (($notewidth * 7) + 2 * $NOTEPAD, ($maxbeat * $beatsize) + $csize);

print qq(<?xml version="1.0" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN"
"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">

<svg width="$width" height="$height"
style="background-color:black;" version="1.1"
xmlns="http://www.w3.org/2000/svg">\n\n);


# vertical lines, delimiter the note space
for my $i(0 .. 7)
{
	my $x = $NOTEPAD + ($i * $notewidth);
	print line($x, 0, $x, $height, $color[4]);
}
print line($NOTEPAD, 0, $NOTEPAD, $height, $color[5]); 
print line($NOTEPAD + (7 * $notewidth), 0, $NOTEPAD + (7 * $notewidth), $height, $color[5]);


## horizontal lines
for my $i(0 .. $maxbeat)
{
	my $x2 = $NOTEPAD + (7 * $notewidth);
	my $y  = $height - (($beatsize * $i) + $notesize);
	print line($NOTEPAD, $y, $x2, $y, $color[4]);
	if ($i % $unk_beat == 0)
	{
		print line($NOTEPAD, $y - 1, $x2, $y - 1, $color[5]);
		print string(12, $NOTEPAD + 4 + (7 * $notewidth), $y - 10, sprintf("#%03d",$i/$unk_beat), $color[5]);
	}
}

my @lch; # store long notes
my %stat = (
    'tap'  => 0,
    'long' => 0,
    'min'  => $bpm,
    'max'  => $bpm
);

print comment("start notes");

foreach my $v(@note)
{
	my $channel = $v->{'channel'};
	if($channel eq 'BZM') # bpm changing ??
	{
		my $newbpm = $v->{'value'};
		my $y = $height - ($beatsize * $v->{'beat'}) + $notesize;
		my $text = sprintf "%.2f", $newbpm;

		print string(5, $NOTEPAD - (length($text) * 9)-4, $y - 10, $text, $color[6]);

		$stat{'min'} = $newbpm if ($newbpm < $stat{'min'});
		$stat{'max'} = $newbpm if ($newbpm > $stat{'max'});
	}else{
		my $c = 1;
		$c = 2 if ($channel % 2 == 1);        
		$c = 3 if (($channel % 10 ) == 3);
		if ($channel >= 0 && $channel <= 6) # tap note
		{
			$stat{'tap'}++;
			my $x = $NOTEPAD + ($channel * $notewidth);
			my $y = $height - ($beatsize * $v->{'beat'});
			print rect($x, $y, $notewidth - 1, $notesize - 1, $color[$c]);
			print line($x, $y + $notesize - 1, $x + $notewidth - 1, $y + $notesize - 1, $color[10 + $c]);
			print line($x, $y, $x + $notewidth - 1, $y, $color[10 + $c]);
		}elsif($channel >= 10 && $channel <= 16) # long note
		{
			$stat{'long'}++;
			if (!defined($lch[$channel]))
			{
				$lch[$channel] = $v->{'beat'};
			}else{
				my $x = $NOTEPAD + (($channel - 10) * $notewidth);
				my $y = $height - ($beatsize * $v->{'beat'});
				my $z = $height - ($beatsize * $lch[$channel]);
				delete $lch[$channel];
				print rect($x,$y,$notewidth-1,($z-$y)+$notesize-1, $color[20 + $c]);
				print rect($x+1,$y,($notewidth*0.15)-1,($z-$y)+$notesize-1, $color[0]);
				print rect($x+($notewidth*0.85)-1,$y,($notewidth*0.15)-2,($z-$y)+$notesize-1,$color[30+$c]);
				print line($x,$z+$notesize-1,$x+$notewidth-1,$z+$notesize-1,$color[30 + $c]);
				print line($x, $y, $x + $notewidth - 1, $y, $color[30 + $c]);
			}
		}
	}
}

## shade comments block
print line(0, $_, $width, $_, $black) for(grep{$_%2}0 .. $csize);

my $y = 20;print string(5, 24, $y, "$lvll[$notelevel] $artist - $title", $color[0]);
$y += 20;  print string(3, 24, $y, $noter, $color[1]);
$y += 15;

my $st = ' [' . sprintf("%.3d",$stat{'min'}) . '-' . sprintf("%.3d",$stat{'max'}) . ']';
$y += 2; print string(1, 24, $y, " - Tap Notes:  " . $stat{'tap'},  $color[7]);
$y += 15;print string(1, 24, $y, " - Long Notes: " . $stat{'long'}, $color[7]);
$y += 15;print string(1, 24, $y, " - BPM:        " . sprintf("%.3d",$bpm) . $st,  $color[7]);

print "</svg>\n";



sub rect
{
	my ($x,$y,$wid,$hei,$color) = @_;
	return qq(<rect x="$x" y="$y" width="$wid" height="$hei" style="fill:$color;"/>\n);
}
sub line
{
	my($x1,$y1,$x2,$y2,$color) = @_;
	return qq(<line x1="$x1" y1="$y1" x2="$x2" y2="$y2" style="stroke:$color;stroke-width:1"/>\n);
}
sub string
{
	my ($font,$x,$y, $string, $color) = @_;
	$string =~ s/\0//g;
	return qq(<text x="$x" y="$y" style="fill:$color;font-size:$font">$string</text>\n);
}

sub comment
{
	my ($str) = @_;
	return qq(<!-- $str -->\n);
}

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







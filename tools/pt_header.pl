use strict;
use warnings;
use Switch;
use Data::Dumper;


my $filename = shift;

open DATA, $filename or die $!;

my $header;
read DATA, $header, 24 or die $!;

my $h = unpack2hash(join(' ',qw/
Z4:$signature
i:$unk1
f:$bpm
i:$unk2
i:$unk3
i:$unk4
/), $header);

print Dumper $h;
# die;
my $eztr;
my $eztr_cnt = -1;

while(!eof DATA)
{
	read DATA, $eztr, 4 or die $!;
	$eztr = unpack ("Z*", $eztr);
	
	if(($eztr ne "EZTR") && ($eztr_cnt < 0))
	{
		seek DATA, -4, 1;
		read DATA, $header, 66 or die $!;
		my $nh = unpack2hash(join(' ',qw/
		C:$sample_id
		c:$main_bgm 
		a*:$info
		/), $header);
		#main_bgm doesn't matter, it's the streamed one
		my $sample_name = index($nh->{'info'}, ".ogg");
		$sample_name = substr($nh->{'info'}, 0, $sample_name+4);
		print "Sample: $nh->{'sample_id'} Name: $sample_name \n";
	}
	elsif($eztr_cnt <= 63)
	{
		$eztr_cnt++;
		
		read DATA, $header, 74 or die $!;
		my $nh = unpack2hash(join(' ',qw/
		A66:$name
		i:$legth_block
		i:$offset
		/), $header);
		
		if($nh->{'offset'} != 0) {print Dumper $nh;}
		
		my $cnt = 0;
		#the length value in the notes is 6 for normal notes and  > 6 for longnotes
		while($cnt < $nh->{'offset'}/11)
		{	
			switch ($eztr_cnt)
			{
				case 0
				{
					read DATA, $header, 11 or die $!;
					my $nh = unpack2hash(join(' ',qw/
					i:$position
					c:$unk1
					f:$bpm
					c2:$unk2
					/), $header);
					my $measure = $nh->{'position'}/192;
					print "---BPM--- POS: $nh->{'position'} MEASURE: $measure BPM: $nh->{'bpm'}    UNK1: $nh->{'unk1'}\n";
				}
				case [3..9]
				{
					read DATA, $header, 11 or die $!;
					my $nh = unpack2hash(join(' ',qw/
					i:$position
					c:$unk1
					C:$sample_id
					c:$vol
					c:$pan
					c:$unk2
					C:$length
					c:$unk3
					/), $header);
					my $measure = $nh->{'position'}/192;
					print "---NOTE[$eztr_cnt]--- ";
					print "POS: $nh->{'position'} MEASURE: $measure ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}    UNK1: $nh->{'unk1'}\n";
				}
				case 10
				{
					read DATA, $header, 11 or die $!;
					my $nh = unpack2hash(join(' ',qw/
					i:$position
					c:$unk1
					C:$sample_id
					c:$vol
					c:$pan
					c:$unk2
					C:$length
					c:$unk3
					/), $header);
					my $measure = $nh->{'position'}/192;
					print "---SCRATCH--- ";
					print "POS: $nh->{'position'} MEASURE: $measure ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}    UNK1: $nh->{'unk1'}\n";
				}
				case 11
				{
					read DATA, $header, 11 or die $!;
					my $nh = unpack2hash(join(' ',qw/
					i:$position
					c:$unk1
					C:$sample_id
					c:$vol
					c:$pan
					c:$unk2
					C:$length
					c:$unk3
					/), $header);
					my $measure = $nh->{'position'}/192;
					print "---PEDAL--- ";
					print "POS: $nh->{'position'} MEASURE: $measure ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}    UNK1: $nh->{'unk1'}\n";
				}
				case [22..31]
				{
					read DATA, $header, 11 or die $!;
					my $nh = unpack2hash(join(' ',qw/
					i:$position
					c:$unk1
					C:$sample_id
					c:$vol
					c:$pan
					c:$unk2
					C:$length
					c:$unk3
					/), $header);
					my $measure = $nh->{'position'}/192;
					print "---BGM[$eztr_cnt]--- ";
					print "POS: $nh->{'position'} MEASURE: $measure ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}    UNK1: $nh->{'unk1'}\n";
				}
				else
				{
					read DATA, $header, 11 or die $!;
					my $nh = unpack2hash(join(' ',qw/
					i:$position
					c:$unk1
					C:$sample_id
					c:$vol
					c:$pan
					c:$unk2
					C:$length
					c:$unk3
					/), $header);
					my $measure = $nh->{'position'}/192;
					print "---UNK[$eztr_cnt]--- ";
					print "POS: $nh->{'position'} MEASURE: $measure ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}    UNK1: $nh->{'unk1'}\n";
				}
			}
			$cnt++;
		}
	}
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


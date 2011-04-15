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
c14:$unk
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
		Z66:$name
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
					
					print "---BPM--- POS: $nh->{'position'} BPM: $nh->{'bpm'}\n";
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
					
					print "---NOTE[$eztr_cnt]--- ";
					print "POS: $nh->{'position'} ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}\n";
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
					
					print "---SCRATCH--- ";
					print "POS: $nh->{'position'} ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}\n";
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
					
					print "---PEDAL--- ";
					print "POS: $nh->{'position'} ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}\n";
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
					
					print "---BGM[$eztr_cnt]--- ";
					print "POS: $nh->{'position'} ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}\n";
				}
				case [32..63]
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
					
					print "---UNK[$eztr_cnt]--- ";
					print "POS: $nh->{'position'} ";
					print "ID: $nh->{'sample_id'} ";
					print "LENGTH: $nh->{'length'} ";
					print "VOL/PAN: $nh->{'vol'}/$nh->{'pan'}\n";
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


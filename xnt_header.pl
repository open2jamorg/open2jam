use strict;
use warnings;
use Data::Dumper;

my $filename = shift;

open DATA, $filename or die $!;
binmode DATA;

my $header;
read DATA, $header, 11 or die $!;


my $h = unpack2hash(join(' ',qw/
Z4:$signature
s:$unk
i:$segments
c:$unk2
/), $header);

print Dumper $h;

if($h->{'segments'} == 3)
{
	readBpmChange();
}
else
{
	readNote(1); #keysounds
	readNote(0); #bgm
}

readSamples();

sub readNote
{
	my ($isKeysound) = @_;
	
	read DATA, $header, 16 or die $!;
	$h = unpack2hash(join(' ',qw/
	c12:$garbage
	i:$count
	/), $header);
	
	my $note;
	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read DATA, $note, 14 or die $!;
		
		my $n = unpack2hash(join(' ',qw/
		c:$zero
		S:$measure
		f:$position
		C:$channel
		S:$sample_id
		f:$holdLength
		/), $note);
		
		if($isKeysound)
		{	print "---NOTE[$n->{'channel'}]--- "; }
		else
		{	print "---BGM[$n->{'channel'}]--- "; }
		
		print "MEASURE: $n->{'measure'} POSITION: $n->{'position'} ";
		print "ID: $n->{'sample_id'} ";
		print "LENGTH: $n->{'holdLength'} ";
		print "\n";
	}	
}

sub readBpmChange 
{
	read DATA, $header, 16 or die $!;
	$h = unpack2hash(join(' ',qw/
	c12:$garbage
	i:$count
	/), $header);
	
	my $note;
	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read DATA, $note, 14 or die $!;
		
		my $n = unpack2hash(join(' ',qw/
		c:$zero
		S:$measure
		f:$position
		c3:$skip
		f:$bpm
		/), $note);
		
		print "---BPM--- MEASURE: $n->{'measure'} POSITION: $n->{'position'} BPM: $n->{'bpm'} \n";
	}	
}

sub readSamples
{
	read DATA, $header, 4 or die $!;
	$h = unpack2hash(join(' ',qw/
	i:$count
	/), $header);
	
	my $note;
	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read DATA, $note, 8 or die $!;
		
		my $n = unpack2hash(join(' ',qw/
		S:$id
		s:$skip
		i:$name_len
		/), $note);
		
		print "---SAMPLE[$n->{'id'}]--- ";
		
		read DATA, $note, $n->{'name_len'} or die $!;
		
		$n = unpack2hash(join(' ',qw/
		Z*:$name
		/), $note);
		
		print "name: $n->{'name'} \n";
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


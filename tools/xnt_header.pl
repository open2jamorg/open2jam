use strict;
use warnings;
use Data::Dumper;

my $filename = shift;
my $xnefile = substr ($filename, 0, length($filename)-4).".xne";
my $volumes = "volumes.txt";

open XNT, $filename or die $!;
binmode XNT;

open XNE, $xnefile or die $!;

readXNE();
readXNT();

sub readXNE
{
	while(<XNE>)
	{
		if($_ =~ /.+Tempo=\"(.+)\"/)
		{
			my $tempo = $1;
			print "Initial BPM = $tempo\n";
		}
	}
	close XNE;
}

sub readXNT
{
	my $header;
	read XNT, $header, 11 or die $!;


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
}

sub readNote
{
	my ($isKeysound) = @_;
	my $header;
	read XNT, $header, 16 or die $!;
	my $h = unpack2hash(join(' ',qw/
	c12:$garbage
	i:$count
	/), $header);
	
	my $note;
	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read XNT, $note, 14 or die $!;
		
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
	my $header;
	read XNT, $header, 16 or die $!;
	my $h = unpack2hash(join(' ',qw/
	c12:$garbage
	i:$count
	/), $header);
	
	my $note;
	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read XNT, $note, 14 or die $!;
		
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
	my $header;
	read XNT, $header, 4 or die $!;
	my $h = unpack2hash(join(' ',qw/
	i:$count
	/), $header);
	
	my $note;
	for(my $i = 0; $i < $h->{'count'}; $i++)
	{
		read XNT, $note, 8 or die $!;
		
		my $n = unpack2hash(join(' ',qw/
		S:$id
		s:$skip
		i:$name_len
		/), $note);
		
		print "---SAMPLE[$n->{'id'}]--- ";
		
		read XNT, $note, $n->{'name_len'} or die $!;
		
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


use strict;
use warnings;
use Switch;
use Data::Dumper;


my $filename = shift;

open DATA, $filename or die $!;
binmode DATA;

my $header;
read DATA, $header, 28 or die $!;

my $h = unpack2hash(join(' ',qw/
Z4:$signature
i:$file_format_version
i:$encryption_flag
i:$sample_count
i:$samples_offset
i:$payload_size
i:$padding
/), $header);

die "Not a M30 file\n" unless $h->{'signature'} eq "M30";

print Dumper $h;

my $encrypt = $h->{'encryption_flag'}; # 16 -XOR-> nami / 32 -XOR-> 0412

my $buf;

while(!eof DATA)
# for (0)
{
	read DATA, $header, 52 or die $!;
	my $nh = unpack2hash(join(' ',qw/
	Z32:$sample_name
	i:$sample_size
	s2:@codec_code
	I:$music_flag
	s:$ref
	s:$unk_zero
	i:$pcm_samples
	/), $header);

	print Dumper $nh;

 	dump_ogg($nh->{'sample_name'},$nh->{'sample_size'});
	#seek DATA, $nh->{'sample_size'}, 1;
}

sub dump_ogg
{
	my ($ref,$sample_size) = @_;
	my ($buf) = @_;
	open MP, ">$ref.ogg";
	binmode MP;
	read DATA, $buf, $sample_size;
	$buf = nami_xor($buf);
	print MP $buf;
	close MP;
}

sub nami_xor
{
	my ($data) = @_;
	my $xor;
	switch($encrypt)
	{
		case 16 { $xor = 'nami'; }
		case 32 { $xor = '0412'; }
		default { print "Make me an error also, IDFK what encryption($encrypt) is :/ "; }
	}
	my $bytes = length $data;
	my $mask = $xor x ($bytes/4);
	$mask .= "\0" x ($bytes%4);
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


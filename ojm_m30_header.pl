use strict;
use warnings;
use Data::Dumper;


my $filename = shift;

open DATA, $filename or die $!;

my $header;
read DATA, $header, 28 or die $!;

my $h = unpack2hash(join(' ',qw/
Z4:$signature
c8:@unk_fixed
s:$sample_count
c6:@unk_fixed2
i:$payload_size
i:$unk_zero
/), $header);

print Dumper $h;

open MP, ">t.ogg";
my $buf;
while(!eof DATA)
# for (0)
{
	read DATA, $header, 52 or die $!;
	my $nh = unpack2hash(join(' ',qw/
	a32:$sample_name
	i:$sample_size
	c8:@unk_1
	i:$ref
	i:$unk_2
	/), $header);

# 	print Dumper $nh;


	read DATA, $buf, $nh->{'sample_size'};
	$buf = nami_xor($buf);
	print MP $buf;
}
 
close MP;

# 	next unless $h->{'signature'} eq 'M30';


sub nami_xor
{
	my ($data) = @_;
	my $nami = 'nami';
	my $bytes = length $data;
	my $mask = $nami x ($bytes/4);
	$mask .= substr $nami, 0, ($bytes%4);
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


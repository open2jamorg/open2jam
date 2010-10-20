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

die "Not a M30 file\n" unless $h->{'signature'} eq "M30";

print Dumper $h;

my $buf;
while(!eof DATA)
# for (0)
{
	read DATA, $header, 52 or die $!;
	my $nh = unpack2hash(join(' ',qw/
	a32:$sample_name
	i:$sample_size
	c:$unk_sample_type
	c:$unk_off
	s:$fixed_2
	i:$unk_sample_type2
	s:$ref
	s:$unk_zero
	c3:@wut2
	c:$unk_counter
	/), $header);

	print Dumper $nh;

# 	dump_ogg($nh->{'ref'},$nh->{'sample_size'});
	seek DATA, $nh->{'sample_size'}, 1;
}

sub dump_ogg
{
	my ($ref,$sample_size) = @_;
	open MP, ">sample_$ref.ogg";
	read DATA, $buf, $sample_size;
	$buf = nami_xor($buf);
	print MP $buf;
	close MP;
}


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


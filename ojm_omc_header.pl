use strict;
use warnings;
use Data::Dumper;

my $filename = shift;

open DATA, $filename or die $!;

my $header;
read DATA, $header, 20 or die $!;


my $h = unpack2hash(join(' ',qw/
a4:$signature
s:$samples
s:$sum_count
i:$size
i:$sum_pos
I:$filesize
/), $header);


# each sample has header of 36 bytes
# Z32:$sample_name
# i:$sample_size


print Dumper $h;

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


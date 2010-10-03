use strict;
use warnings;
use Data::Dumper;
$|++;

open DATA, shift or die $!;
binmode DATA;

my $data;
read DATA, $data, 300 or die $!;

my $h = unpack2hash(join(' ',qw/
l:$songid
c8:@signature
l:$genre
f:$bpm
s3:@level
s:$unk
l3:@noterelated
l3:@notecount
l6:@unk2
c24:@genres
a8:$unk3
Z64:$title
a32:$artist
a32:$noter
a32:$musicfile
l:$jpg
l3:@time
l4:@notepos
/), $data);

######################
# get the cover
seek DATA, $h->{'notepos'}[3], 0; # cover offset

read DATA, $data, $h->{'jpg'} or die $!;

print $data;

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

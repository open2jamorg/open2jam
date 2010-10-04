use strict;
use warnings;
use Data::Dumper;

my $filename = shift;

open DATA, $filename or die $!;

my $header;
read DATA, $header, 20 or die $!;


my $h = unpack2hash(join(' ',qw/
a3:$signature
c:$a1
s:$samples
s:$musics
I:$size
I:$music_pos
I:$end_pos
/), $header);


print Dumper $h;

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


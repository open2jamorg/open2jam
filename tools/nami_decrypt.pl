use strict;
use warnings;

my $filename = shift;

open DATA, $filename or die $!;
binmode DATA;
binmode STDOUT;

my $dump;
read DATA, $dump, 80 or die $!; # header ?
# print $dump;
while(!eof DATA)
{
	read DATA, $dump, 4 or die $!;
	$dump = $dump ^ 'nami';
	print $dump;
}

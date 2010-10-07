use strict;
use warnings;
use Data::Dumper;


foreach(@ARGV)
{
	my $filename = $_;

	open DATA, $filename or die $!;

	my $header;
	read DATA, $header, 80 or die $!;


	my $h = unpack2hash(join(' ',qw/
	Z3:$signature
	S10:@unk
	a37:$comments
	S10:@unk2
	/), $header);
	next unless $h->{'signature'} eq 'M30';

	print "file: $filename\n";
	print Dumper $h;
}
# 80 or 144
# 250192
# size 250112 or 250048

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


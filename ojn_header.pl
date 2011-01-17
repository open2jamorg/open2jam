#!/usr/bin/perl
use strict;
use warnings;
use Data::Dumper;
$|++;


pp_header($_) for(@ARGV);

sub pp_header
{
	open DATA, $_[0] or die $!;
	binmode DATA;

	my $data;
	read DATA, $data, 300 or die $!;

	my $h = unpack2hash(join(' ',qw/
	i:$songid
	a4:$signature
	f:$encode_version
	i:$genre
	f:$bpm
	s4:@level
	i3:@event_count
	i3:@note_count
	i3:@measure_count
	i3:@package_count
	s:$old_encode_version
	s:$old_songid
	a20:$old_genre
	i:$bmp_size
	i:$file_version
	Z64:$title
	Z32:$artist
	a32:$noter
	Z32:$ojm_file
	i:$cover_size
	i3:@time
	i3:@note_offset
    i:$cover_offset
	/), $data);

	print Dumper $h;
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

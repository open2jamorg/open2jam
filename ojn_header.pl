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
	Z8:$signature
	i:$genre
	f:$bpm
	s3:@level
	i3:@unk_num
	c2:@unk_zero
	i3:@note_count
	i3:@unk_time
	i3:@package_count
	s2:@unk_id
	a20:$unk_oldgenre
	i:$bmp_size
	s2:@unk_a
	Z64:$title
	Z32:$artist
	Z32:$noter
	Z32:$ojm_file
	i:$cover_size
	i3:@time
	i4:@note_offset
	/), $data);

	my @kk = sort keys %$h;
	my @vv = map{$h->{$_}}@kk;
	print Data::Dumper->Dump(\@vv,\@kk);
}


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

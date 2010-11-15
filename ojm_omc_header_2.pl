use strict;
use warnings;
use Data::Dumper;

my $filename = shift;

open DATA, $filename or die $!;

my $header;
read DATA, $header, 20 or die $!;


my $h = unpack2hash(join(' ',qw/
Z4:$signature
s:$samples
s:$sum_count
i:$size
i:$sum_pos
I:$filesize
/), $header);

print Dumper $h;
if ($h->{'signature'} eq "OMC")
{
# 	seek DATA, 76, 0;

	while(!eof DATA)
	{
		read DATA, $header, 56 or die $!;
		my $sh = unpack2hash(join(' ',qw/
		Z32:$sample_name
		s:$audio_format
		s:$num_channels
		i:$sample_rate
		i:$byte_rate
		s:$block_align
		s:$bits_per_sample
		i:$data
		i:$chunk_size
		/), $header);

		print Dumper $sh;

		dump_data($sh->{'sample_name'},$sh->{'chunk_size'});
# 		seek DATA, $sh->{'chunk_size'}, 1;
	}
}
elsif ($h->{'signature'} eq "OJM")
{
	while(!eof DATA)
	{
		read DATA, $header, 36 or die $!;
		my $sh = unpack2hash(join(' ',qw/
		Z32:$sample_name
		i:$sample_size
		/), $header);

		print Dumper $sh;

		seek DATA, $sh->{'sample_size'}, 1;
	}
}

sub dump_data
{
	my ($ref,$sample_size) = @_;
	open MP, ">$ref.wav";
	my $buf;
	read DATA, $buf, $sample_size;
	print MP $buf;
	close MP;
}

# each sample has header of 36 bytes
# Z32:$sample_name
# i:$sample_size

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


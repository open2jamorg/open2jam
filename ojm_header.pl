use strict;
use warnings;
use Data::Dumper;

my $filename = shift;

open DATA, $filename or die $!;
binmode DATA;
my $header;
read DATA, $header, 20 or die $!;


my $h = unpack2hash(join(' ',qw/
Z4:$signature
s:$unk1
s:$unk2
i:$wav_start
i:$ogg_start
I:$filesize
/), $header);

print Dumper $h; 

# header info
# Z4:$signature -> Can be OMC or OJM
# s:$unk1       -> Seems to be the number of samples but in some files the number of samples is unk2 :/
# s:$unk2       -> It seems be the version of the container??? or maybe the type of files that the container has????
#                  Or it can be the number of samples if unk1 isn't!! I'm totally lost here :/
# i:$wav_start  -> The start of the wav files (always 0x14 0x00 0x00 0x00 ???)
# i:$ogg_start  -> The start of the ogg files
# I:$filesize   -> The size of the file (OMG RLY?)

if($h->{'signature'} ne "M30")
{
	while(tell DATA != $h->{'ogg_start'})
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
		#This header is part of a WAVE header
		#Link to the WAVE header https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
		
		#dont print the empty ones
		if($sh->{'chunk_size'} != 0)
		{
			print Dumper $sh;
		}
		
		dump_wav ($sh->{'sample_name'},
				  $sh->{'chunk_size'},
				  $sh->{'audio_format'},
				  $sh->{'num_channels'},
				  $sh->{'sample_rate'},
				  $sh->{'byte_rate'},
				  $sh->{'block_align'},
				  $sh->{'bits_per_sample'}
				  );
		#seek DATA, $sh->{'chunk_size'}, 1;
	}


	while(!eof DATA)
	{
		read DATA, $header, 36 or die $!;
		my $sh = unpack2hash(join(' ',qw/
		Z32:$sample_name
		i:$sample_size
		/), $header);
		
		#dont print the empty ones
		if($sh->{'sample_size'} != 0)
		{
			print Dumper $sh;
		}
	
		dump_ogg($sh->{'sample_name'},$sh->{'sample_size'});
		#seek DATA, $sh->{'sample_size'}, 1;
	}
}

sub dump_wav
{
	my ($ref, $sample_size, $audio_fmt, $num_chan, $sample_rate, $byte_rate, $block_align, $bits_per_sample) = @_;
	my ($buf);
	
	open MP, ">$ref.wav";
	binmode MP;	
	
	#wave header
		$buf .= "RIFF"; #RIFF
		$buf .= pack("V",$sample_size+36); #full chunk size = chunk_size + 36
		$buf .= "WAVE"; #WAVE
		$buf .= "fmt "; #fmt_
		$buf .= pack("V",0x10); #PCM FORMAT
		$buf .= pack("v",$audio_fmt); #audio_fmt
		$buf .= pack("v",$num_chan); #num_chan
		$buf .= pack("V",$sample_rate); #sample_rate
		$buf .= pack("V",$byte_rate); #byte rate
		$buf .= pack("v",$block_align); #block align
		$buf .= pack("v",$bits_per_sample); #bits per sample
		$buf .= "data"; #chunk size
		$buf .= pack("V",$sample_size); #chunk size
	#/wave header
	
	print MP $buf;
		
	read DATA, $buf, $sample_size;
	my $bytes = length $buf;
	print MP $buf;
	close MP;
	
}


sub dump_ogg
{
	my ($ref,$sample_size) = @_;
	my ($buf) = @_;
	open MP, ">$ref";
	binmode MP;
	read DATA, $buf, $sample_size;
	my $bytes = length $buf;
	print MP $buf;
	close MP;
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


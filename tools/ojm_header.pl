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
s:$wav_count
s:$ogg_count
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

		
			dump_wav ($sh->{'sample_name'},
					  $sh->{'chunk_size'},
					  $sh->{'audio_format'},
					  $sh->{'num_channels'},
					  $sh->{'sample_rate'},
					  $sh->{'byte_rate'},
					  $sh->{'block_align'},
					  $sh->{'bits_per_sample'}
					  );
		}
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
		$buf .= "RIFF";                     #RIFF
		$buf .= pack("V",$sample_size+36);  #full chunk size = chunk_size + 36
		$buf .= "WAVE";                     #WAVE
		$buf .= "fmt ";                     #fmt_
		$buf .= pack("V",0x10);             #PCM FORMAT
		$buf .= pack("v",$audio_fmt);       #audio_fmt
		$buf .= pack("v",$num_chan);        #num_chan
		$buf .= pack("V",$sample_rate);     #sample_rate
		$buf .= pack("V",$byte_rate);       #byte rate
		$buf .= pack("v",$block_align);     #block align
		$buf .= pack("v",$bits_per_sample); #bits per sample
		$buf .= "data";                     #chunk size
		$buf .= pack("V",$sample_size);     #chunk size
	#/wave header
	
	print MP $buf;
		
	read DATA, $buf, $sample_size;
	#the OMC files have their samples encrypted
	if($h->{'signature'} eq "OMC")
	{
		#fuck the person who invented this, FUCK YOU!... but with love =$
		$buf = arrange_blocks($buf);
		#some weird encryption
		$buf = acc_xor($buf);
	}
	
	print MP $buf;
	close MP;
	
}

sub arrange_blocks
{
	my ($data) = @_;

	my @table=( 										   			#this is a dump from debugging notetool
	0x10, 0x0E, 0x02, 0x09, 0x04, 0x00, 0x07, 0x01,
	0x06, 0x08, 0x0F, 0x0A, 0x05, 0x0C, 0x03, 0x0D,
	0x0B, 0x07, 0x02, 0x0A, 0x0B, 0x03, 0x05, 0x0D,
	0x08, 0x04, 0x00, 0x0C, 0x06, 0x0F, 0x0E, 0x10,
	0x01, 0x09, 0x0C, 0x0D, 0x03, 0x00, 0x06, 0x09,
	0x0A, 0x01, 0x07, 0x08, 0x10, 0x02, 0x0B, 0x0E,
	0x04, 0x0F, 0x05, 0x08, 0x03, 0x04, 0x0D, 0x06,
	0x05, 0x0B, 0x10, 0x02, 0x0C, 0x07, 0x09, 0x0A,
	0x0F, 0x0E, 0x00, 0x01, 0x0F, 0x02, 0x0C, 0x0D,
	0x00, 0x04, 0x01, 0x05, 0x07, 0x03, 0x09, 0x10,
	0x06, 0x0B, 0x0A, 0x08, 0x0E, 0x00, 0x04, 0x0B,
	0x10, 0x0F, 0x0D, 0x0C, 0x06, 0x05, 0x07, 0x01,
	0x02, 0x03, 0x08, 0x09, 0x0A, 0x0E, 0x03, 0x10,
	0x08, 0x07, 0x06, 0x09, 0x0E, 0x0D, 0x00, 0x0A,
	0x0B, 0x04, 0x05, 0x0C, 0x02, 0x01, 0x0F, 0x04,
	0x0E, 0x10, 0x0F, 0x05, 0x08, 0x07, 0x0B, 0x00,
	0x01, 0x06, 0x02, 0x0C, 0x09, 0x03, 0x0A, 0x0D,
	0x06, 0x0D, 0x0E, 0x07, 0x10, 0x0A, 0x0B, 0x00,
	0x01, 0x0C, 0x0F, 0x02, 0x03, 0x08, 0x09, 0x04,
	0x05, 0x0A, 0x0C, 0x00, 0x08, 0x09, 0x0D, 0x03,
	0x04, 0x05, 0x10, 0x0E, 0x0F, 0x01, 0x02, 0x0B,
	0x06, 0x07, 0x05, 0x06, 0x0C, 0x04, 0x0D, 0x0F,
	0x07, 0x0E, 0x08, 0x01, 0x09, 0x02, 0x10, 0x0A,
	0x0B, 0x00, 0x03, 0x0B, 0x0F, 0x04, 0x0E, 0x03,
	0x01, 0x00, 0x02, 0x0D, 0x0C, 0x06, 0x07, 0x05,
	0x10, 0x09, 0x08, 0x0A, 0x03, 0x02, 0x01, 0x00,
	0x04, 0x0C, 0x0D, 0x0B, 0x10, 0x05, 0x06, 0x0F,
	0x0E, 0x07, 0x09, 0x0A, 0x08, 0x09, 0x0A, 0x00,
	0x07, 0x08, 0x06, 0x10, 0x03, 0x04, 0x01, 0x02,
	0x05, 0x0B, 0x0E, 0x0F, 0x0D, 0x0C, 0x0A, 0x06,
	0x09, 0x0C, 0x0B, 0x10, 0x07, 0x08, 0x00, 0x0F,
	0x03, 0x01, 0x02, 0x05, 0x0D, 0x0E, 0x04, 0x0D,
	0x00, 0x01, 0x0E, 0x02, 0x03, 0x08, 0x0B, 0x07,
	0x0C, 0x09, 0x05, 0x0A, 0x0F, 0x04, 0x06, 0x10,
	0x01, 0x0E, 0x02, 0x03, 0x0D, 0x0B, 0x07, 0x00,
	0x08, 0x0C, 0x09, 0x06, 0x0F, 0x10, 0x05, 0x0A,
	0x04, 0x00);
	
	my $length = length $data;
	
	my $key = $length % 17;											#Let's start to looking for a key
	my $key2 = $key;												#Copy it, we'll need it later
	$key = $key << 4;												#Shift 4 bits left, let's make some room
	$key = ($key+$key2);											#Yeah, add them! =$
	$key2 = $key;													#Again, we'll need it later
	$key = $table[$key];											#Let's see the table... ummm ok! founded
	#printf("Init key: %02x\n",$key);
	
	my $block_size = int($length / 0x11);							#Ok, now the block size
	#printf("BLOCK SIZE: %08x\n", $block_size);
	my $buf = $data;												#Let's fill the buffer with the data, it'll overwriten
	my $counter = 0;												#Start my counter, tic tac tic tac
	while($counter < 17)											#loopy loop
	{
		my $block_start_encoded = $block_size * $counter;			#Where is the start of the enconded block
		my $block_start_plain = $block_size * $key;					#Where the final plain block will be 	
		my $block;												
		#printf("KEY: %02x ENCODED: %08x PLAIN: %08x KEYPOS: %i / %02x\n", $key, $block_start_encoded, $block_start_plain, $key2, $key2);

		$block = substr($data, $block_start_encoded, $block_size);	#Let's fill the block with the encoded info
		
		substr ($buf, $block_start_plain, $block_size, $block);		#Let's change the buf with the block starting
																	#from the final position of the block
		$key2 = $key2 + 1;											#Remember that key2, let's add 1
		$key = $table[$key2];										#Where is the key? :O
		#printf("Key: %02x\n",$key);
		$counter = $counter + 1;									#Keep the loop looping �-�
	}
	
	return $buf;
}

#Global variables 
our $KEYBYTE = 0xFF;
our $ACC_ACC_COUNTER = 0;

sub acc_xor
{
	my ($data) = @_;
	my ($buf, $temp, $byte) = @_;
	
	our $KEYBYTE;
	our $ACC_COUNTER;
	
	if(!defined($KEYBYTE)) { $KEYBYTE = 0xFF; }
	if(!defined($ACC_COUNTER)) { $ACC_COUNTER = 0; }
	#printf(" Keybyte: %02x Counter %02x\n", $KEYBYTE, $ACC_COUNTER);
	
	my $length = length $data;

	$buf = $data;
	
	for (my $i = 0; $i  <= $length; $i++)
	{		
		if($ACC_COUNTER > 7) 
		{
			$ACC_COUNTER = 0;
			$KEYBYTE = unpack("W",$temp);
		}
		
		$temp = $byte = substr($data, $i, 1);
		
		if(($KEYBYTE << $ACC_COUNTER) & 0x80)
		{
			$byte = ~$byte;
		}
		else
		{
			$byte = $byte;
		}
		
		substr($buf, $i, 1, $byte);
		
		if($i != $length) {$ACC_COUNTER++;}
	}
	
	return $buf;
}

sub dump_ogg
{
	my ($ref,$sample_size) = @_;
	my ($buf) = @_;
	open MP, ">$ref";
	binmode MP;
	read DATA, $buf, $sample_size;
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


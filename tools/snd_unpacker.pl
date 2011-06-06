use warnings;
use Data::Dumper;
use Compress::Zlib;

my $filename = shift;

open DATA, $filename or die $!;
binmode DATA;
my $header;
read DATA, $header, 24 or die $!;


my $h = unpack2hash(join(' ',qw/
Z8:$signature
i:$unk
i:$file_count
i:$dir_count
i:$size
/), $header);

print Dumper $h;
while (!eof DATA)
{
	read DATA, $header, 0x91 or die $!; 
	
	my $f = unpack2hash(join(' ',qw/
	c:$isDir
	Z128:$name
	i:$sizeOriginal
	i:$sizePacked
	i:$parentDirOffset
	i:$nextAdressOffset
	/), $header);
	
	print Dumper $f;
	if($f->{'sizePacked'} > 0)
	{
		dump_unpacked($f->{'name'},$f->{'sizePacked'});
	}
}
my $buf;
sub dump_unpacked
{
	my ($ref,$size) = @_;
	open MP, ">$ref";
	binmode MP;
	read DATA, $buf, $size;
	my $unpkd = uncompress($buf);
	print MP $unpkd;
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


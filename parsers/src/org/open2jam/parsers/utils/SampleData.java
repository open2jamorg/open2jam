package org.open2jam.parsers.utils;

import java.io.*;

/**
 *
 * @author CdK
 */
public class SampleData {

    public enum Type { WAV, WAV_NO_HEADER, OGG, MP3 };
    
    private InputStream input;
    private final Type type;
    private final String filename;
    
    private WAVHeader wavHeader = null;

    public SampleData(InputStream input, Type type, String filename) {
	this.input = input;
	this.type = type;
	this.filename = filename;
    }
    
    public SampleData(InputStream input, WAVHeader header, String filename) {
	this.input = input;
	this.type = Type.WAV_NO_HEADER;
	this.wavHeader = header;
	this.filename = filename;
    }
    
    public InputStream getInputStream() {
	return input;
    }
    
    public Type getType() {
	return type;
    }
    
    public String getName() {
	return filename;
    }
    
    public WAVHeader getWAVHeader() {
	return wavHeader;
    }
     
    public void dispose() throws IOException {
	this.input.close();
    }
    
    public void copyTo(OutputStream out) throws IOException {
        if(this.type == Type.WAV_NO_HEADER) {
            out.write(WAVHeader.writeHeader(this.wavHeader));
        }
	ByteHelper.copyTo(this.input, out);
    }
    
    public void copyToFolder(File directory) throws IOException {
	File f = new File(directory, filename);
	if(!f.exists()) {
	    FileOutputStream out = new FileOutputStream(f);
	    
	    if(this.type == Type.WAV_NO_HEADER)
		out.write(WAVHeader.writeHeader(this.wavHeader));

	    copyTo(out);

	    out.close();
	}
    }
    
    public static class WAVHeader {
	public short audio_format;
	public short num_channels;
	public int sample_rate;
	public int bit_rate;
	public short block_align;
	public short bits_per_sample;
//	int data;
	public int chunk_size;

	public WAVHeader(short audio_format, short num_channels, int sample_rate, int bit_rate, short block_align, short bits_per_sample, int data, int chunk_size) {
	    this.audio_format = audio_format;
	    this.num_channels = num_channels;
	    this.sample_rate = sample_rate;
	    this.bit_rate = bit_rate;
	    this.block_align = block_align;
	    this.bits_per_sample = bits_per_sample;
//	    this.data = data;
	    this.chunk_size = chunk_size;
	}
	
	public static byte[] writeHeader(WAVHeader header) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    out.write("RIFF".getBytes());
	    out.write(ByteHelper.intToByteArray(header.chunk_size+36), 0, 4);
	    out.write("WAVE".getBytes());
	    out.write("fmt ".getBytes());
	    out.write(ByteHelper.intToByteArray(0x10), 0, 4);
	    out.write(ByteHelper.shortToByteArray(header.audio_format), 0, 2);
	    out.write(ByteHelper.shortToByteArray(header.num_channels), 0, 2);
	    out.write(ByteHelper.intToByteArray(header.sample_rate), 0, 4);
	    out.write(ByteHelper.intToByteArray(header.bit_rate), 0, 4);
	    out.write(ByteHelper.shortToByteArray(header.block_align), 0, 2);
	    out.write(ByteHelper.shortToByteArray(header.bits_per_sample), 0, 2);
	    out.write("data".getBytes());
	    out.write(ByteHelper.intToByteArray(header.chunk_size), 0, 4);
	    
	    return out.toByteArray();
	}
    }
    
}

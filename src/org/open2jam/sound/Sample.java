/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.sound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javazoom.jl.decoder.*;
import org.open2jam.parsers.utils.ByteHelper;
import org.open2jam.parsers.utils.Logger;
import org.open2jam.parsers.utils.SampleData;
import org.open2jam.util.OggInputStream;

/**
 *
 * @author CdK
 */
public class Sample {

    public enum Format { MONO8, STEREO8, MONO16, STEREO16, FLOAT32 };
    public enum Method { STREAM_FROM_FILE, COPY_TO_MEMORY }; //TODO do this D:
    
    public final ByteBuffer data;
    public final Format format;
    public final int samplerate;
    
    private Sample(ByteBuffer data, Format format, int samplerate)
    {
	this.data = data;
	this.format = format;
	this.samplerate = samplerate;
    }
    
    /**
     * Dispose the data
     */
    public void dispose()
    {
	data.clear();
    }
    
    /*
     * Decode a SampleData using his type
     */
    public static Sample decode(SampleData data) {
	switch(data.getType()) {
	    case MP3:
		return Sample.decodeMP3(data);
	    case OGG:
		return Sample.decodeOGG(data);
	    case WAV:
	    case WAV_NO_HEADER:
		return Sample.decodeWAV(data);
	    default:
		return null;
	}
    }
      
    /**
     * Creates a new SampleData from an uncompressed ByteBuffer
     * @param buffer The ByteBuffer
     * @param bits The numbers of bits per channel
     * @param channels The number of channels
     * @param samplerate The sample rate
     * @param type The type of the sampled data
     * @return A new SampleData
     */
    public static Sample create(ByteBuffer data, int bits, int channels, int samplerate)
    {
	Format format;
	if(channels == 1){
	    format = bits == 8 ? Format.MONO8 : Format.MONO16;
	}else{
	    format = bits == 8 ? Format.STEREO8 : Format.STEREO16;
	}
	
	
	return new Sample(data, format, samplerate);
    }
    
    /**
     * Creates a new SampleData from an OggInputStream
     * @param ois The OggInputStream
     * @return a new SampleData
     */
    public static Sample decodeOGG(SampleData ad)
    {
	try
	{
	    OggInputStream ois = new OggInputStream((ad.getInputStream()));
	    
	    Format format = ois.getFormat() == OggInputStream.FORMAT_MONO16 ?
							    Format.MONO16 : Format.STEREO16;
	    int samplerate = ois.getRate();

	    ByteArrayOutputStream out = new ByteArrayOutputStream(ByteHelper.tmp_buffer.length);

	    ByteHelper.copyTo(ois, out);
	    
	    ByteBuffer b = ByteBuffer.allocateDirect(out.size());
	    b.put(out.toByteArray());
	    b.flip();
	    
	    ois.close();
	    
	    return new Sample(b, format, samplerate);
	    
	} catch(IOException e) {
	    Logger.global.log(Level.SEVERE, "Exception creating AudioData(OGG) : {0}", e.getMessage());
	    return null;
	}
    }

    public static Sample decodeWAV(SampleData ad)
    {
	if(ad.getWAVHeader() == null)
	    return decode_wav(ad);
	else
	    return decode_raw(ad);
    }
    
    /**
     * Creates a new SampleData from an InputStream (Only PCM uncompressed WAVE files please)
     * @param wav The InputStream
     * @return a new SampleData
     */
    private static Sample decode_wav(SampleData ad)
    {
	try {
	    AudioInputStream ais = AudioSystem.getAudioInputStream(ad.getInputStream());
	    AudioFormat audioformat = ais.getFormat();

	    ByteArrayOutputStream out = new ByteArrayOutputStream(ByteHelper.tmp_buffer.length);

	    ByteHelper.copyTo(ais, out);
	    
	    ByteBuffer b = ByteBuffer.allocateDirect(out.size());
	    b.put(out.toByteArray());
	    b.flip();
	    
	    ais.close();
	    
	    return create(b, audioformat.getSampleSizeInBits(), audioformat.getChannels(), (int) audioformat.getSampleRate());
	} catch (Exception e) {
	    Logger.global.log(Level.SEVERE, "Exception creating AudioData(WAVE) : {0}", e.getMessage());
	    return null;
	}
    }
    
    private static Sample decode_raw(SampleData ad)
    {
	try {
	    ByteArrayOutputStream out = new ByteArrayOutputStream(ByteHelper.tmp_buffer.length);

	    ByteHelper.copyTo(ad.getInputStream(), out);

	    ByteBuffer b = ByteBuffer.allocateDirect(out.size());
	    b.put(out.toByteArray());
	    b.flip();

	    ad.getInputStream().close();
	    
	    SampleData.WAVHeader h = ad.getWAVHeader();
	    
	    return create(b, h.bits_per_sample, h.num_channels, h.sample_rate);
	} catch (IOException ex) {
	    Logger.global.log(Level.SEVERE, "Exception creating AudioData(RAW) : {0}", ex.getMessage());
	    return null;
	}
    }
    
    /**
     * Creates a new SampleData from a Bitstream (Thanks to the libgdx team for the function & library :D)
     * @param stream The Bitstream
     * @return a new SampleData
     */
    public static Sample decodeMP3(SampleData ad)
    {
	try {
	    Bitstream stream = new Bitstream(ad.getInputStream());
	    
	    ByteArrayOutputStream out = new ByteArrayOutputStream(ByteHelper.tmp_buffer.length);

	    MP3Decoder decoder = new MP3Decoder();


	    OutputBuffer outputBuffer = null;
	    int sampleRate = -1 , channels = -1;
	    while (true) {
		Header header = stream.readFrame();
		if (header == null) break;
		if (outputBuffer == null) {
		    channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
		    outputBuffer = new OutputBuffer(channels, false);
		    decoder.setOutputBuffer(outputBuffer);
		    sampleRate = header.getSampleRate();
		}
		try {
		    decoder.decodeFrame(header, stream);
		} catch (Exception ignored) {
		    // JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
		}
		stream.closeFrame();
		out.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
	    }
	    
	    ByteBuffer b = ByteBuffer.allocateDirect(out.size());
	    b.put(out.toByteArray());
	    b.flip();

	    stream.close();

	    return create(b, 16/*TODO find&fix this*/, channels, sampleRate);
	} catch (BitstreamException ex) {
	    Logger.global.log(Level.SEVERE, "Exception creating AudioData(MP3) : {0}", ex.getMessage());
	    return null;
	}
    }
    
}

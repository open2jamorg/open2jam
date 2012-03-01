package org.open2jam.parsers.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javazoom.jl.decoder.*;

/**
 * A class that encapsulate the various ways of load an audio file.
 * It loads:
 * <ul>
 * <li>uncompressed PCM files</li>
 * <li>MP3 files</li>
 * <li>OGG files</li>
 * </ul>
 * @author CdK
 */
public class AudioData {
    
    public enum Format { MONO8, STEREO8, MONO16, STEREO16 };
    public enum Method { STREAM_FROM_FILE, COPY_TO_MEMORY }; //TODO do this D:
    public enum Type { WAV, WAV_NO_HEADER, OGG, MP3 };
    
    public final ByteBuffer data;
    public final Format format;
    public final int samplerate;
    public final Type type;
    public final Method method;
    public final String filename;
    
    private static final byte[] tmp_buffer = new byte[1024];
    
    private AudioData(ByteBuffer data, Format format, int samplerate, Type type, String filename)
    {
	this.data = data;
	this.format = format;
	this.samplerate = samplerate;
        this.type = type;
        this.method = Method.COPY_TO_MEMORY;
	this.filename = filename;
    }
    
    /**
     * Dispose the data
     */
    public void dispose()
    {
	data.clear();
    }
    
    /**
     * Creates a new AudioData from an uncompressed ByteBuffer
     * @param buffer The ByteBuffer
     * @param bits The numbers of bits per channel
     * @param channels The number of channels
     * @param samplerate The sample rate
     * @param filename The name of the file
     * @return A new AudioData
     */
    public static AudioData create(ByteBuffer buffer, int bits, int channels, int samplerate, Type type, String filename)
    {
	Format format;
	if(channels == 1){
	    format = bits == 8 ? Format.MONO8 : Format.MONO16;
	}else{
	    format = bits == 8 ? Format.STEREO8 : Format.STEREO16;
	}
	
	return new AudioData(buffer, format, samplerate, type, filename);
    }
    
    /**
     * Creates a new AudioData from an OggInputStream
     * @param ois The OggInputStream
     * @param filename The name of the file
     * @return a new AudioData
     */
    public static AudioData create(OggInputStream ois, String filename)
    {
	try
	{
	    Format format = ois.getFormat() == OggInputStream.FORMAT_MONO16 ?
							    Format.MONO16 : Format.STEREO16;
	    int samplerate = ois.getRate();

	    ByteArrayOutputStream out = new ByteArrayOutputStream(tmp_buffer.length);

	    while(true) {
		int r = ois.read(tmp_buffer);
		if (r == -1) break;
		out.write(tmp_buffer,0,r);
	    }
	    ByteBuffer b = ByteBuffer.allocateDirect(out.size());
	    b.put(out.toByteArray());
	    b.flip();
	    
	    ois.close();
	    
	    return new AudioData(b, format, samplerate, Type.OGG, filename);
	    
	} catch(IOException e) {
	    Logger.global.log(Level.SEVERE, "Exception creating AudioData(OGG) : {0}", e.getMessage());
	    return null;
	}
    }

    /**
     * Creates a new AudioData from an InputStream (Only PCM uncompressed WAVE files please)
     * @param wav The InputStream
     * @param filename The name of the file
     * @return a new AudioData
     */
    public static AudioData create(InputStream wav, String filename)
    {
	try {
	    AudioInputStream ais = AudioSystem.getAudioInputStream(wav);
	    AudioFormat audioformat = ais.getFormat();
	    

	    ByteArrayOutputStream out = new ByteArrayOutputStream(tmp_buffer.length);

	    while(true) {
		int r = ais.read(tmp_buffer);
		if (r == -1) break;
		out.write(tmp_buffer,0,r);
	    }
	    ByteBuffer b = ByteBuffer.allocateDirect(out.size());
	    b.put(out.toByteArray());
	    b.flip();
	    
	    ais.close();
	    
	    return create(b, audioformat.getSampleSizeInBits(), audioformat.getChannels(), (int) audioformat.getSampleRate(),
                    Type.WAV, filename);
	} catch (Exception e) {
	    Logger.global.log(Level.SEVERE, "Exception creating AudioData(WAVE) : {0}", e.getMessage());
	    return null;
	}
    }
    
    /**
     * Creates a new AudioData from a Bitstream (Thanks to the libgdx team for the function & library :D)
     * @param stream The Bitstream
     * @param filename The name of the file
     * @return a new AudioData
     */
    public static AudioData create(Bitstream stream, String filename)
    {
	try {
	    ByteArrayOutputStream out = new ByteArrayOutputStream(tmp_buffer.length);

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
	    stream.close();
	    ByteBuffer b = ByteBuffer.allocateDirect(out.size());
	    b.put(out.toByteArray());
	    b.flip();

	    stream.close();

	    return create(b, 16/*TODO find&fix this*/, channels, sampleRate, Type.MP3, filename);
	} catch (BitstreamException ex) {
	    Logger.global.log(Level.SEVERE, "Exception creating AudioData(MP3) : {0}", ex.getMessage());
	    return null;
	}
    }
}

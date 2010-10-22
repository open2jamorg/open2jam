package org.open2jam.render.lwjgl;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.WaveData;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import org.open2jam.parser.SampleRef;

/**
  this class is the bridge between OpenAL and the app.
  http://lwjgl.org/documentation_openal_06.php
*/
public class SampleLoader
{
	private static HashMap<SampleRef,Integer> sample_buffer = new HashMap<SampleRef,Integer>();
	private static ArrayList<Integer> source_buffer = new ArrayList<Integer>();

	private static FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
	private static FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });


	// we need to initialize the OpenAL context
	static {
		try{
			AL.create();
		} catch (Exception e) {
			die(e);
		}
		AL10.alGetError();
	}

	public static int loadSource(SampleRef s)
	{
		IntBuffer source = BufferUtils.createIntBuffer(1);
		Integer buffer;
		int result;

		// Get the files buffer id (load it if necessary).
		buffer = sample_buffer.get(s);
		if(buffer == null)throw new RuntimeException("sample not loaded");

		// Generate a source.
		AL10.alGenSources(source);

		if ((result = AL10.alGetError()) != AL10.AL_NO_ERROR)
			throw new RuntimeException(getALErrorString(result));

		// Setup the source properties.
		AL10.alSourcei(source.get(0), AL10.AL_BUFFER,   buffer     );
		AL10.alSourcef(source.get(0), AL10.AL_PITCH,    1.0f       );
		AL10.alSourcef(source.get(0), AL10.AL_GAIN,     1.0f       );
		AL10.alSource (source.get(0), AL10.AL_POSITION, sourcePos  );
		AL10.alSource (source.get(0), AL10.AL_VELOCITY, sourceVel  );
		AL10.alSourcei(source.get(0), AL10.AL_LOOPING,AL10.AL_FALSE);

		// Save the source id.
		source_buffer.add(source.get(0));

		// Return the source id.
		return source.get(0);
	}

	public static int loadBuffer(SampleRef s, InputStream i)
	{
		int result;
		IntBuffer buffer = BufferUtils.createIntBuffer(1);

		AL10.alGenBuffers(buffer);

		if ((result = AL10.alGetError()) != AL10.AL_NO_ERROR)
			throw new RuntimeException(getALErrorString(result));

		WaveData waveFile = WaveData.create(i);
		if (waveFile != null) {
			AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
			waveFile.dispose();
		} else {
			die(new Exception("wavedata error"));
		}

		// Do another error check and return.
		if ((result = AL10.alGetError()) != AL10.AL_NO_ERROR)
			throw new RuntimeException(getALErrorString(result));

		sample_buffer.put(s, buffer.get(0));
		return buffer.get(0);
	}

	/**
	* 1) Releases all buffers.
	* 2) Releases all sources.
	*/
	public static void killData()
	{
		IntBuffer scratch = BufferUtils.createIntBuffer(1);
		for (Iterator<Integer> iter = sample_buffer.values().iterator(); iter.hasNext();) {
			scratch.put(0, iter.next());
			AL10.alDeleteBuffers(scratch);
		}
		sample_buffer.clear();

		// Release all source data.
		for (Iterator<Integer> iter = source_buffer.iterator(); iter.hasNext();) {
			scratch.put(0, iter.next());
			AL10.alDeleteSources(scratch);
		}
		source_buffer.clear();
	}

	private static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		final java.io.PrintWriter pw = new java.io.PrintWriter(r);
		e.printStackTrace(pw);
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	/**
	* 1) Identify the error code.
	* 2) Return the error as a string.
	*/
	public static String getALErrorString(int err) {
		switch (err) {
		case AL10.AL_NO_ERROR:
			return "AL_NO_ERROR";
		case AL10.AL_INVALID_NAME:
			return "AL_INVALID_NAME";
		case AL10.AL_INVALID_ENUM:
			return "AL_INVALID_ENUM";
		case AL10.AL_INVALID_VALUE:
			return "AL_INVALID_VALUE";
		case AL10.AL_INVALID_OPERATION:
			return "AL_INVALID_OPERATION";
		case AL10.AL_OUT_OF_MEMORY:
			return "AL_OUT_OF_MEMORY";
		default:
			return "No such error code";
		}
	}
}

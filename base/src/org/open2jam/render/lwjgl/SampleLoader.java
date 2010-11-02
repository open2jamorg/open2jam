package org.open2jam.render.lwjgl;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import org.open2jam.parser.OggInputStream;

/**
  this class is the bridge between OpenAL and the app.
  http://lwjgl.org/documentation_openal_06.php
*/
public class SampleLoader
{
	private static ArrayList<Integer> sample_buffer = new ArrayList<Integer>();
	private static ArrayList<Integer> source_buffer = new ArrayList<Integer>();


	// we need to initialize the OpenAL context
	static {
		try{
			AL.create();
		} catch (Exception e) {
			die(e);
		}
		AL10.alGetError();
		FloatBuffer listenerPos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
		FloatBuffer listenerVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
		FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });

		listenerPos.flip();
		listenerVel.flip();
		listenerOri.flip();

		AL10.alListener(AL10.AL_POSITION,    listenerPos);
		AL10.alListener(AL10.AL_VELOCITY,    listenerVel);
		AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
	}

	public static int newSource()
	{
		IntBuffer source = BufferUtils.createIntBuffer(1);
		
		// Generate a source.
		AL10.alGenSources(source);

		checkOpenALError();

		FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
		FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

		sourcePos.flip();
		sourceVel.flip();

		// Setup the source properties.
// 		AL10.alSourcei(source.get(0), AL10.AL_BUFFER,   buffer     );
		AL10.alSourcef(source.get(0), AL10.AL_PITCH,    1.0f       );
		AL10.alSourcef(source.get(0), AL10.AL_GAIN,     1.0f       );
		AL10.alSource (source.get(0), AL10.AL_POSITION, sourcePos  );
		AL10.alSource (source.get(0), AL10.AL_VELOCITY, sourceVel  );
		AL10.alSourcei(source.get(0), AL10.AL_LOOPING,  AL10.AL_FALSE  );

		// Save the source id.
		source_buffer.add(source.get(0));

		// Return the source id.
		return source.get(0);
	}

	public static void bindSource(int source, int buffer)
	{
		AL10.alSourcei(source, AL10.AL_BUFFER,buffer);
	}

	private static byte[] tmp_buffer = new byte[1024];
	public static int newBuffer(OggInputStream in)
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		AL10.alGenBuffers(buffer);

		try{
			checkOpenALError();

			boolean mono = (in.getFormat() == OggInputStream.FORMAT_MONO16);
			int format = (mono ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16);

			ByteArrayOutputStream out = new ByteArrayOutputStream(tmp_buffer.length);

			while(true) {
				int r = in.read(tmp_buffer);
				if (r == -1) break;
				out.write(tmp_buffer,0,r);
			}
			ByteBuffer b = ByteBuffer.allocateDirect(out.size());
			b.put(out.toByteArray());
			b.flip();

			AL10.alBufferData(buffer.get(0), format, b, in.getRate());

			checkOpenALError();

		}catch(Exception e)
		{
			die(e);
		}

		sample_buffer.add(buffer.get(0));
		return buffer.get(0);
	}

 	public static void killSource(int s)
 	{
 		IntBuffer scratch = BufferUtils.createIntBuffer(1);
 		scratch.put(0, s);
 		AL10.alDeleteSources(scratch);
 		source_buffer.remove(source_buffer.indexOf(s));
 	}

	public static void killBuffer(int s)
	{
		IntBuffer scratch = BufferUtils.createIntBuffer(1);
		scratch.put(0, s);
		AL10.alDeleteBuffers(scratch);
		sample_buffer.remove(sample_buffer.indexOf(s));
	}

	/**
	* 1) Releases all buffers.
	* 2) Releases all sources.
	*/
	public static void killData()
	{
		IntBuffer scratch = BufferUtils.createIntBuffer(1);
		for (Iterator<Integer> iter = sample_buffer.iterator(); iter.hasNext();) {
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

	/**
	* 1) Identify the error code.
	* 2) Return the error as a string.
	*/
	public static void checkOpenALError()
	{
		int result = AL10.alGetError();
		if(result == AL10.AL_NO_ERROR)return;

		String err;
		switch (result)
		{
			case AL10.AL_NO_ERROR:err = "AL_NO_ERROR";break;
			case AL10.AL_INVALID_NAME:err = "AL_INVALID_NAME";break;
			case AL10.AL_INVALID_ENUM:err = "AL_INVALID_ENUM";break;
			case AL10.AL_INVALID_VALUE:err = "AL_INVALID_VALUE";break;
			case AL10.AL_INVALID_OPERATION:err = "AL_INVALID_OPERATION";break;
			case AL10.AL_OUT_OF_MEMORY:err = "AL_OUT_OF_MEMORY";break;
			default:err = "unknown error!";
		}
		throw new RuntimeException("OpenAL Error: "+err);
	}

	public static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		final java.io.PrintWriter pw = new java.io.PrintWriter(r);
		e.printStackTrace(pw);
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}

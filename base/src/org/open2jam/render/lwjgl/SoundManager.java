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
import org.open2jam.Logger;

import org.open2jam.OggInputStream;

/**
  this class is the bridge between OpenAL and the app.
  http://lwjgl.org/documentation_openal_06.php
*/
public class SoundManager
{
	private static ArrayList<Integer> sample_buffer = new ArrayList<Integer>();
	private static ArrayList<Integer> source_buffer = new ArrayList<Integer>();


	// we need to initialize the OpenAL context
	static {
		try{
			AL.create();
		} catch (Exception e) {
			Logger.die(e);
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

        public static void setGain(int source, float g)
        {
            AL10.alSourcef(source, AL10.AL_GAIN, g);
        }

        public static void setPan(int source, float x)
        {
            FloatBuffer pos = BufferUtils.createFloatBuffer(3).put(new float[] { x, 0.0f, 0.0f });
            AL10.alSource(source, AL10.AL_POSITION, pos);
        }

	public static int newSource()
	{
		IntBuffer source = BufferUtils.createIntBuffer(1);
		
		// Generate a source.
		AL10.alGenSources(source);

		org.lwjgl.openal.Util.checkALError();

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

        public static boolean isPlaying(int source)
        {
            return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
        }

        public static void play(int source, int buffer)
        {
            AL10.alSourcei(source, AL10.AL_BUFFER,buffer);
            AL10.alSourcePlay(source);
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
			org.lwjgl.openal.Util.checkALError();

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

			org.lwjgl.openal.Util.checkALError();

		}catch(Exception e)
		{
			Logger.die(e);
		}

		sample_buffer.add(buffer.get(0));
		return buffer.get(0);
	}

        public static int newBuffer(ByteBuffer buffer, int bits, int channels, int sample_rate)
        {
            IntBuffer id_buf = BufferUtils.createIntBuffer(1);
            AL10.alGenBuffers(id_buf);

            try{
                org.lwjgl.openal.Util.checkALError();

                int format = -1;
                if(channels == 1){
                    if(bits == 8)format = AL10.AL_FORMAT_MONO8;
                    else format = AL10.AL_FORMAT_MONO16;
                }else {
                    if(bits == 8)format = AL10.AL_FORMAT_STEREO8;
                    else format = AL10.AL_FORMAT_STEREO16;
                }

                AL10.alBufferData(id_buf.get(0), format, buffer, sample_rate);

                org.lwjgl.openal.Util.checkALError();
            }
            catch(Exception e){
                Logger.die(e);
            }
            
            sample_buffer.add(id_buf.get(0));
            return id_buf.get(0);
        }

	public static void killData()
	{
                IntBuffer scratch = BufferUtils.createIntBuffer(1);
		// Release all source data.
		for (Iterator<Integer> iter = source_buffer.iterator(); iter.hasNext();) {
			scratch.put(0, iter.next());
                        AL10.alSourceStop(scratch);
			AL10.alDeleteSources(scratch);
		}
		source_buffer.clear();
                
		for (Iterator<Integer> iter = sample_buffer.iterator(); iter.hasNext();) {
			scratch.put(0, iter.next());
			AL10.alDeleteBuffers(scratch);
		}
		sample_buffer.clear();
	}
}

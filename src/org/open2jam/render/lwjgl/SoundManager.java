package org.open2jam.render.lwjgl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.open2jam.util.Logger;
import org.open2jam.util.SampleDecoder;

/**
  this class is the bridge between OpenAL and the app.
  http://lwjgl.org/documentation_openal_06.php
*/
public class SoundManager
{

    private static final ArrayList<Integer> sample_buffer = new ArrayList<Integer>();
    private static final ArrayList<Integer> source_buffer = new ArrayList<Integer>();


    // we need to initialize the OpenAL context
    static {
        try {
            AL.create();

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

        } catch (LWJGLException ex) {
            Logger.global.severe("Could not initialize the OpenAL context !!");
        }
    }

    public static void setGain(int source, float g)
    {
        AL10.alSourcef(source, AL10.AL_GAIN, g);
    }
    
    public static float getGain(int source)
    {
        return AL10.alGetSourcef(source, AL10.AL_GAIN);
    }

    private static final FloatBuffer pan_pos_buffer = BufferUtils.createFloatBuffer(3);
    public static void setPan(int source, float x)
    {
        pan_pos_buffer.put(new float[] { x, 0.0f, 0.0f }).flip();
        AL10.alSource(source, AL10.AL_POSITION, pan_pos_buffer);
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
    public static void stop(int source)
    {
        AL10.alSourceStop(source);
    }
    
    public static int newBuffer(SampleDecoder data)
    {
	IntBuffer id_buf = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(id_buf);

        org.lwjgl.openal.Util.checkALError();
	int format = -1;
	switch(data.format)
	{
	    case MONO8: format = AL10.AL_FORMAT_MONO8; break;
	    case MONO16: format = AL10.AL_FORMAT_MONO16; break;
	    case STEREO8: format = AL10.AL_FORMAT_STEREO8; break;
	    case STEREO16: format = AL10.AL_FORMAT_STEREO16; break;
	}

        AL10.alBufferData(id_buf.get(0), format, data.data, data.samplerate);

        org.lwjgl.openal.Util.checkALError();

        sample_buffer.add(id_buf.get(0));
        return id_buf.get(0);	
    }

    public static void killData()
    {
        IntBuffer scratch = BufferUtils.createIntBuffer(1);
        // Release all source data.
        for (Integer aSource_buffer : source_buffer) {
            scratch.put(0, aSource_buffer);
            AL10.alSourceStop(scratch);
            AL10.alDeleteSources(scratch);
        }
        source_buffer.clear();

        for (Integer aSample_buffer : sample_buffer) {
            scratch.put(0, aSample_buffer);
            AL10.alDeleteBuffers(scratch);
        }
        sample_buffer.clear();
    }

    public static void mainVolume(float vol)
    {
        AL10.alListenerf(AL10.AL_GAIN, vol);
    }
}

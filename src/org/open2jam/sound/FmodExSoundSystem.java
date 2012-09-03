/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.sound;

import java.nio.ByteBuffer;
import java.util.HashMap;
import org.jouvieje.fmodex.Channel;
import org.jouvieje.fmodex.FmodEx;
import org.jouvieje.fmodex.Init;
import org.jouvieje.fmodex.Sound;
import org.jouvieje.fmodex.defines.FMOD_INITFLAGS;
import org.jouvieje.fmodex.defines.FMOD_MODE;
import org.jouvieje.fmodex.enumerations.FMOD_CHANNELINDEX;
import org.jouvieje.fmodex.enumerations.FMOD_RESULT;
import org.jouvieje.fmodex.enumerations.FMOD_SOUND_FORMAT;
import org.jouvieje.fmodex.exceptions.InitException;
import org.jouvieje.fmodex.structures.FMOD_CREATESOUNDEXINFO;

/**
 *
 * @author dttvb
 */
public class FmodExSoundSystem implements SoundSystem {

    private org.jouvieje.fmodex.System system;
    private HashMap<Sample, Sound> soundMap = new HashMap<Sample, Sound>();
    
    public FmodExSoundSystem() throws SoundSystemException {
        try
        {
            Init.loadLibraries();
        }
        catch(InitException e)
        {
            throw new SoundSystemInitException(e);
        }
        
        system = new org.jouvieje.fmodex.System();
        errorCheck(FmodEx.System_Create(system));
        errorCheck(system.setDSPBufferSize(1024, 2));
        errorCheck(system.init(1296, FMOD_INITFLAGS.FMOD_INIT_NORMAL, null));
        
    }
    
    private void errorCheck(FMOD_RESULT result) throws SoundSystemException {
        if (result != FMOD_RESULT.FMOD_OK) {
            throw new SoundSystemException(FmodEx.FMOD_ErrorString(result));
        }
    }
    
    @Override
    public void load(Sample sample) throws SoundSystemException {
        Sound sound = new Sound();
        FMOD_CREATESOUNDEXINFO exinfo = FMOD_CREATESOUNDEXINFO.allocate();
        FMOD_SOUND_FORMAT format = FMOD_SOUND_FORMAT.FMOD_SOUND_FORMAT_PCM16;
        ByteBuffer data = sample.data;
        int channels = 1;
	switch(sample.format)
	{
	    case MONO8: case STEREO8:
                format = FMOD_SOUND_FORMAT.FMOD_SOUND_FORMAT_PCM8; break;
	    case MONO16: case STEREO16:
                format = FMOD_SOUND_FORMAT.FMOD_SOUND_FORMAT_PCM16; break;
	}
        switch(sample.format)
	{
	    case MONO8: case MONO16:
                channels = 1; break;
	    case STEREO8: case STEREO16:
                channels = 2; break;
	}
        
        System.out.println(format + " : " + channels + " : " + sample.samplerate);
        exinfo.setLength(data.capacity());
        exinfo.setFormat(format);
        exinfo.setNumChannels(channels);
        exinfo.setDefaultFrequency(sample.samplerate);
        errorCheck(system.createSound(data, FMOD_MODE.FMOD_SOFTWARE | FMOD_MODE.FMOD_OPENMEMORY | FMOD_MODE.FMOD_OPENRAW, exinfo, sound));
        soundMap.put(sample, sound);
    }

    @Override
    public void play(Sample sample, float volume, float pan) throws SoundSystemException {
        Sound sound = soundMap.get(sample);
        system.update();
        Channel channel = new Channel();
        errorCheck(system.playSound(FMOD_CHANNELINDEX.FMOD_CHANNEL_FREE, sound, true, channel));
        System.out.println("sample: " + sample.format + " / " + volume + " pan " + pan);
        errorCheck(channel.setVolume(Math.min(1, volume)));
        errorCheck(channel.setPan(pan));
        errorCheck(channel.setPaused(false));
    }
    
}

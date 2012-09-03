/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.sound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jouvieje.fmodex.Channel;
import org.jouvieje.fmodex.ChannelGroup;
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
import org.open2jam.parsers.utils.ByteHelper;
import org.open2jam.parsers.utils.SampleData;

/**
 *
 * @author dttvb
 */
public class FmodExSoundSystem implements SoundSystem {

    private org.jouvieje.fmodex.System system;
    
    private ChannelGroup masterGroup = new ChannelGroup();
    private ChannelGroup bgmGroup = new ChannelGroup();
    private ChannelGroup keyGroup = new ChannelGroup();
    
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
        errorCheck(system.getMasterChannelGroup(masterGroup));
        errorCheck(system.createChannelGroup("BGM", bgmGroup));
        errorCheck(system.createChannelGroup("KEY", keyGroup));
    }
    
    private void errorCheck(FMOD_RESULT result) throws SoundSystemException {
        if (result != FMOD_RESULT.FMOD_OK) {
            throw new SoundSystemException(FmodEx.FMOD_ErrorString(result));
        }
    }
    
    public void release() {
        system.release();
    }
    
    @Override
    public org.open2jam.sound.Sound load(SampleData data) throws SoundSystemException {

        ByteArrayOutputStream out = new ByteArrayOutputStream(ByteHelper.tmp_buffer.length);
        
        try {
            data.copyTo(out);
        } catch (IOException ex) {
            Logger.getLogger(FmodExSoundSystem.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(out.size());
        buffer.put(out.toByteArray());
        buffer.flip();
        
        return new FmodSound(buffer);
        
    }

    @Override
    public void setBGMVolume(float factor) {
        bgmGroup.setVolume(factor);
    }

    @Override
    public void setKeyVolume(float factor) {
        keyGroup.setVolume(factor);
    }

    @Override
    public void setMasterVolume(float factor) {
        masterGroup.setVolume(factor);
    }
    
    class FmodSound implements org.open2jam.sound.Sound {
    
        private Sound sound;
        
        public FmodSound(ByteBuffer buffer) throws SoundSystemException {
            sound = new Sound();
            FMOD_CREATESOUNDEXINFO exinfo = FMOD_CREATESOUNDEXINFO.allocate();
            exinfo.setLength(buffer.capacity());
            errorCheck(system.createSound(buffer, FMOD_MODE.FMOD_SOFTWARE | FMOD_MODE.FMOD_OPENMEMORY, exinfo, sound));
        }
        
        public void play(SoundChannel soundChannel, float volume, float pan) throws SoundSystemException {
            system.update();
            Channel channel = new Channel();
            errorCheck(system.playSound(FMOD_CHANNELINDEX.FMOD_CHANNEL_FREE, sound, true, channel));
            System.out.println("sample:" + volume + " pan " + pan);
            errorCheck(channel.setVolume(Math.min(1, volume)));
            errorCheck(channel.setPan(pan));
            errorCheck(channel.setPaused(false));
            errorCheck(channel.setChannelGroup(soundChannel == SoundChannel.BGM ? bgmGroup : keyGroup));
        }
        
    }
    
}

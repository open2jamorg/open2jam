/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.sound;

/**
 *
 * @author dttvb
 */
public interface Sound {
    
    SoundInstance play(SoundChannel soundChannel, float volume, float pan) throws SoundSystemException;
    
}

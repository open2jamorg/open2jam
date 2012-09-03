/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.sound;

/**
 *
 * @author dttvb
 */
public interface SoundSystem {
    
    void load(Sample sample) throws SoundSystemException;
    void play(Sample sample, float volume, float pan) throws SoundSystemException;
    
}

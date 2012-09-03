/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.sound;

import org.open2jam.parsers.utils.SampleData;

/**
 *
 * @author dttvb
 */
public interface SoundSystem {
    
    Sound load(SampleData sample) throws SoundSystemException;
    void release();
    void update();
    
    void setBGMVolume(float factor);
    void setKeyVolume(float factor);
    void setMasterVolume(float factor);
    
}

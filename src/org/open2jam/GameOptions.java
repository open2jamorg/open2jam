package org.open2jam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.DisplayMode;
import org.open2jam.parser.Event;

/**
 * This class will store game options such as hi-speed and autoplay.
 * @author SilverHx
 */
public class GameOptions implements Serializable {
    
    // Hi-speed
    private double hispeed = 1.0;
    /*
     * "Hi-Speed"=>0, "xR-Speed"=>1, "W-Speed"=>2
     */
    public enum SpeedType implements Serializable {
        HiSpeed, xRSpeed, WSpeed;
        
        @Override
        public String toString() {
            return super.toString().replace("Speed", "-Speed:");
        }
    }
    private SpeedType speed_type = SpeedType.HiSpeed;
    /*
     * "None"=>0, "Hidden"=>1, "Sudden"=>2, "Dark"=>3
     */
    public enum VisibilityMod implements Serializable {
        None, Hidden, Sudden, Dark;
    }
    private VisibilityMod visibilityModifier = VisibilityMod.None;
    /*
     * "None"=>0, "Mirror"=>1, "Shuffle"=>2, "Random"=>3
     */
    public enum ChannelMod implements Serializable {
        None, Mirror, Shuffle, Random;
    }
    private ChannelMod channelModifier = ChannelMod.None;
    // Key volume
    private float keyVolume = 1f;
    // BGM volume
    private float BGMVolume = 1f;
    // Master volume
    private float masterVolume = 0.5f;
    // Autoplay?
    private boolean autoplay = false;
    // autoplay channels
    private ArrayList<Boolean> autoplay_channels = new ArrayList<Boolean>();
    // autosound?
    private boolean autosound = false;

    // full screen
    private boolean fullscreen = false;
    // bilinear option
    private boolean bilinear = true;
    // vsync 
    private boolean vsync = true;
    
    private int width,height,bpp,freq;

    //public constructor. give default options
    public GameOptions() {
	for(Event.Channel c : Event.Channel.values())
	{
	    if(c.toString().startsWith("NOTE_"))
		autoplay_channels.add(c.isAutoplay());
	}
    }

    /**
     * Gets the visibility modifier
     * @return the modifier
     */
    public VisibilityMod getVisibilityModifier() {
        return visibilityModifier;
    }

    /**
     * Sets visibility option value.
     * @param new visibility option value
     */
    public void setVisibilityModifier(VisibilityMod mod) {
        visibilityModifier = mod;
    }

    /**
     * Gets hi-speed value.
     * @return hi-speed
     */
    public double getHiSpeed() {
        return hispeed;
    }

    /**
     * Sets hi-speed value.
     * @param new hi-speed value
     */
    public void setHispeed(double hispeed) {
        this.hispeed = hispeed;
    }

    /**
     * get the speed type
     * @return 
     */
    public SpeedType getSpeedType() {
        return speed_type;
    }

    /**
     * set the speed type
     * @param mod 
     */
    public void setSpeedType(SpeedType mod) {
        this.speed_type = mod;
    }

    /**
     * Gets channelModifier option value.
     * @return a channel modifier
     */
    public ChannelMod getChannelModifier() {
        return channelModifier;
    }

    /**
     * Sets channelModifier option value.
     * @param new channelModifier option value
     */
    public void setChannelModifier(ChannelMod mod) {
        channelModifier = mod;
    }

    /**
     * Gets key volume in range [0, 1].
     * @return key volume
     */
    public float getKeyVolume() {
        return keyVolume;
    }

    /**
     * Sets key volume in range [0, 1].
     * @param new key volume
     */
    public void setKeyVolume(float vol) {
        keyVolume = (float) clamp(vol, 0, 1);
    }

    /**
     * Gets BGM volume in range [0, 1].
     * @return BGM volume
     */
    public float getBGMVolume() {
        return BGMVolume;
    }

    /**
     * Sets BGM volume in range [0, 1].
     * @param new BGM volume
     */
    public void setBGMVolume(float vol) {
        BGMVolume = (float) clamp(vol, 0, 1);
    }

    /**
     * Gets master volume in range [0, 1].
     * @return master volume
     */
    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Sets master volume in range [0, 1].
     * @param new master volume
     */
    public void setMasterVolume(float vol) {
        masterVolume = (float) clamp(vol, 0, 1);
    }

    /**
     * Gets autoplay option value.
     * @return true if autoplay option is enabled, false otherwise
     */
    public boolean getAutoplay() {
        return autoplay;
    }

    /**
     * Sets autoplay option value.
     * @param new autoplay option value
     */
    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }
    
    /**
     * Gets autosound option value.
     * @return true if autosound option is enabled, false otherwise
     */
    public boolean getAutosound() {
        return autosound;
    }

    /**
     * Sets autosound option value.
     * @param new autosound option value
     */
    public void setAutosound(boolean autosound) {
        this.autosound = autosound;
    }
    
    public List<Boolean> getAutoplayChannels() {
	return autoplay_channels;
    }
    
    public void setAutoplayChannels(List<Boolean> list) {
	this.autoplay_channels = (ArrayList<Boolean>) list;
    }

    public void setFullScreen(boolean fs) {
        this.fullscreen = fs;
    }

    public boolean getFullScreen() {
        return this.fullscreen;
    }

    public void setBilinear(boolean bi) {
        this.bilinear = bi;
    }

    public boolean getBilinear() {
        return bilinear;
    }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    public boolean getVsync() {
        return vsync;
    }

    public void setDisplay(DisplayMode dm) {
        this.width = dm.getWidth();
        this.height = dm.getHeight();
        this.bpp = dm.getBitsPerPixel();
        this.freq = dm.getFrequency();
    }
    
    public boolean isDisplaySaved(DisplayMode dm)
    {
        return dm.getWidth() == width && dm.getHeight() == height &&
               dm.getBitsPerPixel() == bpp && dm.getFrequency() == freq;
    }
    
    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}

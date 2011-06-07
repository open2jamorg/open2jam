package org.open2jam;

import java.io.Serializable;

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
    private int speed_type = 0;
    /*
     * "None"=>0, "Hidden"=>1, "Sudden"=>2, "Dark"=>3
     */
    private int visibilityModifier = 0;
    /*
     * "None"=>0, "Mirror"=>1, "Shuffle"=>2, "Random"=>3
     */
    private int channelModifier = 0;
    // Key volume
    private float keyVolume = 1f;
    // BGM volume
    private float BGMVolume = 0.8f;
    // Master volume
    private float masterVolume = 1f;
    // Autoplay?
    private boolean autoplay = false;

    // full screen
    private boolean fullscreen = false;
    // bilinear option
    private boolean bilinear = false;
    // vsync 
    private boolean vsync = true;

    //public empty constructor. give default options
    public GameOptions() { }

    /**
     * Gets the visibility modifier
     * TODO: enum this crap
     * @return the modifier
     */
    public int getVisibilityModifier() {
        return visibilityModifier;
    }

    /**
     * Sets visibility option value.
     * @param new visibility option value
     */
    public void setVisibilityModifier(int mod) {
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
        this.hispeed = clamp(hispeed, 0.5, 10);
    }

    /**
     * get the speed type
     * @return 
     */
    public int getSpeedType() {
        return speed_type;
    }

    /**
     * set the speed type
     * @param mod 
     */
    public void setSpeedType(int mod) {
        this.speed_type = mod;
    }

    /**
     * Gets channelModifier option value.
     * @return a channel modifier
     */
    public int getChannelModifier() {
        return channelModifier;
    }

    /**
     * Sets channelModifier option value.
     * @param new channelModifier option value
     */
    public void setChannelModifier(int mod) {
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

    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}

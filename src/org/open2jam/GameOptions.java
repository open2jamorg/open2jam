package org.open2jam;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.DisplayMode;
import org.open2jam.parsers.Event;

/**
 * This class will store game options such as hi-speed and autoplay.
 * @author SilverHx
 */
public class GameOptions {
    
    
    /*
     * "Hi-Speed"=>0, "xR-Speed"=>1, "W-Speed"=>2, "Regul-Speed"=>3
     */
    public enum SpeedType {
        HiSpeed, xRSpeed, WSpeed, RegulSpeed;
        
        @Override
        public String toString() {
            return super.toString().replace("Speed", "-Speed:");
        }
    }
    
    /**
     * Judgment type
     */
    public enum JudgmentType {
        BeatJudgment, TimeJudgment;
    }
    
    /*
     * "None"=>0, "Hidden"=>1, "Sudden"=>2, "Dark"=>3
     */
    public enum VisibilityMod {
        None, Hidden, Sudden, Dark;
    }
    
    /*
     * "None"=>0, "Mirror"=>1, "Shuffle"=>2, "Random"=>3
     */
    public enum ChannelMod {
        None, Mirror, Shuffle, Random;
    }
    
    // fields
    private double speedMultiplier = 1.0;
    private SpeedType speedType = SpeedType.HiSpeed;
    private VisibilityMod visibilityModifier = VisibilityMod.None;
    private ChannelMod channelModifier = ChannelMod.None;
    private JudgmentType judgmentType = JudgmentType.BeatJudgment;
    
    float keyVolume = 1.0f;
    float bgmVolume = 1.0f;
    float masterVolume = 1.0f;
    
    private boolean autoplay = false;
    private ArrayList<Boolean> autoplayChannels = generateDefaultAutoplayChannels();
    private boolean autosound = false;

    // display options
    private boolean displayFullscreen = false;
    private boolean displayVsync = true;
    private int displayWidth = 0;
    private int displayHeight = 0;
    private int displayBitsPerPixel = 0;
    private int displayFrequency = 0;
    
    // VLC lib path
    private String vlc = "";
    
    // display lag and audio latency
    private double displayLag = 0;
    private double audioLatency = 0;
    
    //public constructor. give default options
    public GameOptions() {
    }
    
    
    private ArrayList<Boolean> generateDefaultAutoplayChannels() {
        ArrayList<Boolean> out = new ArrayList<Boolean>();
        for(Event.Channel c : Event.Channel.values())
        {
            if(c.toString().startsWith("NOTE_"))
                out.add(c.isAutoplay());
        }
        return out;
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
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Sets hi-speed value.
     * @param new hi-speed value
     */
    public void setSpeedMultiplier(double hispeed) {
        this.speedMultiplier = hispeed;
    }

    /**
     * get the speed type
     * @return 
     */
    public SpeedType getSpeedType() {
        return speedType;
    }

    /**
     * set the speed type
     * @param mod 
     */
    public void setSpeedType(SpeedType mod) {
        this.speedType = mod;
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
        return bgmVolume;
    }

    /**
     * Sets BGM volume in range [0, 1].
     * @param new BGM volume
     */
    public void setBGMVolume(float vol) {
        bgmVolume = (float) clamp(vol, 0, 1);
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
    public boolean isAutoplay() {
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
    public boolean isAutosound() {
        return autosound;
    }

    /**
     * Sets autosound option value.
     * @param new autosound option value
     */
    public void setAutosound(boolean autosound) {
        this.autosound = autosound;
    }
    
    public void setDisplay(DisplayMode dm) {
        this.displayWidth = dm.getWidth();
        this.displayHeight = dm.getHeight();
        this.displayBitsPerPixel = dm.getBitsPerPixel();
        this.displayFrequency = dm.getFrequency();
    }
    
    public boolean isDisplaySaved(DisplayMode dm)
    {
        return dm.getWidth() == displayWidth && dm.getHeight() == displayHeight &&
               dm.getBitsPerPixel() == displayBitsPerPixel && dm.getFrequency() == displayFrequency;
    }
    
    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public double getAudioLatency() {
        return audioLatency;
    }

    public void setAudioLatency(double audioLatency) {
        this.audioLatency = audioLatency;
    }

    public ArrayList<Boolean> getAutoplayChannels() {
        return autoplayChannels;
    }

    public void setAutoplayChannels(List<Boolean> autoplayChannels) {
        this.autoplayChannels = new ArrayList<Boolean>(autoplayChannels);
    }

    public int getDisplayBitsPerPixel() {
        return displayBitsPerPixel;
    }

    public void setDisplayBitsPerPixel(int displayBitsPerPixel) {
        this.displayBitsPerPixel = displayBitsPerPixel;
    }

    public int getDisplayFrequency() {
        return displayFrequency;
    }

    public void setDisplayFrequency(int displayFrequency) {
        this.displayFrequency = displayFrequency;
    }

    public boolean isDisplayFullscreen() {
        return displayFullscreen;
    }

    public void setDisplayFullscreen(boolean displayFullscreen) {
        this.displayFullscreen = displayFullscreen;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public double getDisplayLag() {
        return displayLag;
    }

    public void setDisplayLag(double displayLag) {
        this.displayLag = displayLag;
    }

    public boolean isDisplayVsync() {
        return displayVsync;
    }

    public void setDisplayVsync(boolean displayVsync) {
        this.displayVsync = displayVsync;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public String getVLCLibraryPath() {
        return vlc;
    }

    public void setVLCLibraryPath(String vlc) {
        this.vlc = vlc;
    }

    public JudgmentType getJudgmentType() {
        return judgmentType;
    }

    public void setJudgmentType(JudgmentType judgmentType) {
        this.judgmentType = judgmentType;
    }
    
}

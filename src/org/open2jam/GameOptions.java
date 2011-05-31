/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam;

/**
 * This class will store game options such as hi-speed and autoplay.
 * @author SilverHx
 */
public class GameOptions 
{
    // Instance, I'm using a pseudo-singleton here
    private static GameOptions instance = null;
    
    // Hi-speed
    private double hiSpeed = 1.0;
    
    // Hidden option
    private boolean hidden = false;
    
    // Sudden option
    private boolean sudden = false;
    
    // Towel option
    private boolean towel = false;
    
    // Mirror option
    private boolean mirror = false;
    
    // Random option
    private boolean random = false;
    
    // Shuffle option
    private boolean shuffle = false;
    
    // Key volume
    private float keyVolume = 1f;
    
    // BGM volume
    private float BGMVolume = 0.8f;
    
    // Master volume
    private float masterVolume = 1f;
    
    // Autoplay?
    private boolean autoplay = false;
    
    //Private empty constructor. Use create() instead.
    private GameOptions() { }
    
    /**
     * Creates a new GameOptions instance, overwriting previous one. 
     * Normally, it should be called only once.
     */
    public static void create()
    {
        // Advantages: options reset whenever you call create(). With singleton
        // you must reset options manually.
        instance = new GameOptions();
    }
    
    /**
     * Gets towel option value.
     * @return true if towel option is enabled, false otherwise
     */
    public static boolean getTowel()
    {
        return instance.towel;
    }
    
    /**
     * Sets towel option value.
     * @param new towel option value
     */
    public static void setTowel(boolean towel)
    {
        instance.towel = towel;
    }
    
    /**
     * Gets hidden option value.
     * @return true if hidden option is enabled, false otherwise
     */
    public static boolean getHidden()
    {
        return instance.hidden;
    }
    
    /**
     * Sets hidden option value.
     * @param new hidden option value
     */
    public static void setHidden(boolean hidden)
    {
        instance.hidden = hidden;
    }
    
    /**
     * Gets sudden option value.
     * @return true if sudden option is enabled, false otherwise
     */
    public static boolean getSudden()
    {
        return instance.sudden;
    }
    
    /**
     * Sets sudden option value.
     * @param new sudden option value
     */
    public static void setSudden(boolean sudden)
    {
        instance.sudden = sudden;
    }
    
    /**
     * Gets dark (hidden + sudden) option value.
     * @return true if dark option is enabled, false otherwise
     */
    public static boolean getDark()
    {
        return instance.sudden && instance.hidden;
    }
    
    /**
     * Sets dark (hidden + sudden) option value.
     * @param new dark option value
     */
    public static void setDark(boolean dark)
    {
        instance.sudden = dark;
        instance.hidden = dark;
    }
    
    /**
     * Returns true if any visibility modifier is on, false otherwise.
     * @return true if any visibility modifier is on, false otherwise.
     */
    public static boolean visibilityModifiersOn()
    {
        return instance.hidden || instance.sudden || instance.towel;
    }
    
    /**
     * Returns true if any channel modifier is on, false otherwise.
     * @return true if any channel modifier is on, false otherwise.
     */
    public static boolean channelModifiersOn()
    {
        return instance.random || instance.shuffle || instance.mirror;
    }
    
    /**
     * Gets hi-speed value.
     * @return hi-speed
     */
    public static double getHiSpeed()
    {
        return instance.hiSpeed;
    }
    
    /**
     * Sets hi-speed value.
     * @param new hi-speed value
     */
    public static void setHiSpeed(double hiSpeed)
    {
        instance.hiSpeed = hiSpeed;
    }
    
    /**
     * Gets random option value.
     * @return true if random option is enabled, false otherwise
     */
    public static boolean getRandom()
    {
        return instance.random;
    }
    
    /**
     * Sets random option value.
     * @param new random option value
     */
    public static void setRandom(boolean random)
    {
        instance.random = random;
    }
    
    /**
     * Gets mirror option value.
     * @return true if mirror option is enabled, false otherwise
     */
    public static boolean getMirror()
    {
        return instance.mirror;
    }
    
    /**
     * Sets mirror option value.
     * @param new mirror option value
     */
    public static void setMirror(boolean mirror)
    {
        instance.mirror = mirror;
    }
    
    /**
     * Gets shuffle option value.
     * @return true if shuffle option is enabled, false otherwise
     */
    public static boolean getShuffle()
    {
        return instance.shuffle;
    }
    
    /**
     * Sets shuffle option value.
     * @param new shuffle option value
     */
    public static void setShuffle(boolean shuffle)
    {
        instance.shuffle = shuffle;
    }
    
    /**
     * Gets key volume in range [0, 1].
     * @return key volume
     */
    public static float getKeyVolume()
    {
        return instance.keyVolume;
    }

    /**
     * Sets key volume in range [0, 1].
     * @param new key volume
     */
    public static void setKeyVolume(float keyVolume)
    {
        if (keyVolume >= 0.0 && keyVolume <= 1.0)
            instance.keyVolume = keyVolume;
        else
        { /* Do Something */ }
    }
    
    /**
     * Gets BGM volume in range [0, 1].
     * @return BGM volume
     */
    public static float getBGMVolume()
    {
        return instance.BGMVolume;
    }

    /**
     * Sets BGM volume in range [0, 1].
     * @param new BGM volume
     */
    public static void setBGMVolume(float BGMVolume)
    {
        if (BGMVolume >= 0.0 && BGMVolume <= 1.0)
            instance.BGMVolume = BGMVolume;
        else
        { /* Do Something */ }
    }
    
    /**
     * Gets master volume in range [0, 1].
     * @return master volume
     */
    public static float getMasterVolume()
    {
        return instance.masterVolume;
    }

    /**
     * Sets master volume in range [0, 1].
     * @param new master volume
     */
    public static void setMasterVolume(float masterVolume)
    {
        if (masterVolume >= 0.0 && masterVolume <= 1.0)
            instance.masterVolume = masterVolume;
        else
        { /* Do Something */ }
    }
    
    /**
     * Gets autoplay option value.
     * @return true if autoplay option is enabled, false otherwise
     */
    public static boolean getAutoplay()
    {
        return instance.autoplay;
    }
    
    /**
     * Sets autoplay option value.
     * @param new autoplay option value
     */
    public static void setAutoplay(boolean autoplay)
    {
        instance.autoplay = autoplay;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam;

/**
 * This class will store game options such as hi-speed and autoplay.
 * It uses singleton pattern.
 * @author SilverHx
 */
public class GameOptions 
{
    // Instance
    private static GameOptions instance = null;
    
    // Sudden+ option
    private boolean suddenPlus = false;
    
    //Private empty constructor. Use create() instead.
    private GameOptions() { }
    
    /**
     * Creates a new GameOptions instance, overwriting previous one. 
     * Normally, it should be called only once.
     */
     public static void create()
    {
        instance = new GameOptions();
    }
    
    /**
     * Gets sudden+ option value.
     * @return true if sudden+ option is enabled, false otherwise
     */
    public static boolean getSuddenPlus()
    {
        return instance.suddenPlus;
    }
    
    /**
     * Gets sudden+ option value.
     * @param new sudden+ value
     */
    public static void setSuddenPlus(boolean suddenPlus)
    {
        instance.suddenPlus = suddenPlus;
    }
    

}

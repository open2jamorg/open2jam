package org.open2jam.render.util;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A pollable keyboard system, place holder until something generic
 * comes along
 *
 * @author Kevin Glass
 */
public class Keyboard {
	/** The status of the keys on the keyboard */
    private static boolean[] keys = new boolean[1024];
    
    /**
     * Initialise the central keyboard handler
     */
    public static void init() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new KeyHandler(),AWTEvent.KEY_EVENT_MASK);         
    }
    
    /**
     * Initialise the central keyboard handler
     * 
     * @param c The component that we will listen to
     */
    public static void init(Component c) {
    	c.addKeyListener(new KeyHandler());         
    }
    
    /**
     * Check if a specified key is pressed
     * 
     * @param key The code of the key to check (defined in KeyEvent)
     * @return True if the key is pressed
     */
    public static boolean isPressed(int key) {
        return keys[key];
    }
    
    /**
     * Set the status of the key
     * 
     * @param key The code of the key to set
     * @param pressed The new status of the key
     */
    public static void setPressed(int key,boolean pressed) {
        keys[key] = pressed;
    }
    
    /**
     * A class to respond to key presses on a global scale.
     * 
     * @author Kevin Glass
     */
    private static class KeyHandler extends KeyAdapter implements AWTEventListener {
        /**
         * Notification of a key press
         * 
         * @param e The event details
         */
        public void keyPressed(KeyEvent e) {
        	if (e.isConsumed()) {
        		return;
        	}
            keys[e.getKeyCode()] = true;
        }
        
        /**
         * Notification of a key release
         * 
         * @param e The event details
         */
        public void keyReleased(KeyEvent e) {
        	if (e.isConsumed()) {
        		return;
        	}
        	
        	KeyEvent nextPress = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
        	
        	if ((nextPress == null) || (nextPress.getWhen() != e.getWhen())) {
        		keys[e.getKeyCode()] = false;
        	}
        	
        }
        
        /**
         * Notification that an event has occured in the AWT event
         * system
         * 
         * @param e The event details
         */
        public void eventDispatched(AWTEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                keyPressed((KeyEvent) e);
            }
            if (e.getID() == KeyEvent.KEY_RELEASED) {
                keyReleased((KeyEvent) e);
            }
        }
        
    }
}

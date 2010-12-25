package org.open2jam;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.open2jam.parser.Event;

/**
 *
 * @author fox
 */
public class Config implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    EnumMap<Event.Channel,Integer> keyboard_map;
    private static final File CONFIG_FILE = new File("config.obj");

    Level log_level = Level.INFO;
    FileHandler log_handle = null;

    static final Logger logger = Logger.getLogger(Config.class.getName());

    /** singleton object */
    private static Config config = null;

    private Config()
    {
        keyboard_map = new EnumMap<Event.Channel,Integer>(Event.Channel.class);
	keyboard_map.put(Event.Channel.NONE, KeyEvent.VK_UP);
	keyboard_map.put(Event.Channel.NONE, KeyEvent.VK_DOWN);
        keyboard_map.put(Event.Channel.NOTE_1, KeyEvent.VK_S);
        keyboard_map.put(Event.Channel.NOTE_2, KeyEvent.VK_D);
        keyboard_map.put(Event.Channel.NOTE_3, KeyEvent.VK_F);
        keyboard_map.put(Event.Channel.NOTE_4, KeyEvent.VK_SPACE);
        keyboard_map.put(Event.Channel.NOTE_5, KeyEvent.VK_J);
        keyboard_map.put(Event.Channel.NOTE_6, KeyEvent.VK_K);
        keyboard_map.put(Event.Channel.NOTE_7, KeyEvent.VK_L);
        keyboard_map.put(Event.Channel.NOTE_SC, KeyEvent.VK_SHIFT);
    }

    public EnumMap getKeyboardMap(){
        return keyboard_map;
    }

    public void save()
    {
        try {
            new ObjectOutputStream(new FileOutputStream(CONFIG_FILE)).writeObject(this);
        } catch (FileNotFoundException ex) {
            logger.severe("Could not find file to write config !");
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "IO Error on writing config file ! :{0}", ioe.getMessage());
        }
    }

    public static Config get()
    {
        if(config == null){
            try {
                config = (Config) new ObjectInputStream(new FileInputStream(CONFIG_FILE)).readObject();
            } catch (ClassNotFoundException ex) {
                logger.severe("There's no Config class !! impossibru !");
            } catch (FileNotFoundException ex) {
                config = new Config();
                config.save();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "IO Error on reading config file ! :{0}", ioe.getMessage());
            }
        }
        return config;
    }
}

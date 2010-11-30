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
import org.open2jam.parser.Event;
import org.open2jam.util.Logger;


/**
 *
 * @author fox
 */
public class Config implements Serializable
{
    EnumMap<Event.Channel,Integer> keyboard_map;
    private static final File CONFIG_FILE = new File("config.obj");

    private Config()
    {
        keyboard_map = new EnumMap<Event.Channel,Integer>(Event.Channel.class);
        keyboard_map.put(Event.Channel.NOTE_1, KeyEvent.VK_S);
        keyboard_map.put(Event.Channel.NOTE_2, KeyEvent.VK_D);
        keyboard_map.put(Event.Channel.NOTE_3, KeyEvent.VK_F);
        keyboard_map.put(Event.Channel.NOTE_4, KeyEvent.VK_SPACE);
        keyboard_map.put(Event.Channel.NOTE_5, KeyEvent.VK_J);
        keyboard_map.put(Event.Channel.NOTE_6, KeyEvent.VK_K);
        keyboard_map.put(Event.Channel.NOTE_7, KeyEvent.VK_L);
    }

    public EnumMap getKeyboardMap(){
        return keyboard_map;
    }

    public void write()
    {
        try {
            new ObjectOutputStream(new FileOutputStream(CONFIG_FILE)).writeObject(this);
        } catch (FileNotFoundException ex) {
            Logger.warn(ex);
        } catch (IOException ioe) {
            Logger.warn(ioe);
        }
    }

    public static Config read()
    {
        try {
            return (Config) new ObjectInputStream(new FileInputStream(CONFIG_FILE)).readObject();
        } catch (ClassNotFoundException ex) {
            Logger.warn(ex);
        } catch (FileNotFoundException ex) {
            Config c = new Config();
            c.write();
            return c;
        } catch (IOException ioe) {
            Logger.warn(ioe);
        }
        return null;
    }
}

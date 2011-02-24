package org.open2jam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.input.Keyboard;
import org.open2jam.parser.Event;

/**
 *
 * @author fox
 */
public class Config implements Serializable
{
    private static final long serialVersionUID = 1L;
                                                    // A list of games that uses each configuration
                                                    // There will be no support for all of them right now, or ever xD
    EnumMap<Event.Channel,Integer> keyboard_map_4K; // DJMAX Triology / EZ2ON
    EnumMap<Event.Channel,Integer> keyboard_map_5K; // BEMANI / DJMAX series / Pop'n'Music
    EnumMap<Event.Channel,Integer> keyboard_map_6K; // DJMAX series / EZ2ON
    EnumMap<Event.Channel,Integer> keyboard_map_7K; // BEMANI / DJMAX series / O2JAM
    EnumMap<Event.Channel,Integer> keyboard_map_8K; // DJMAX Triology / EZ2ON
//  EnumMap<Event.Channel,Integer> keyboard_map_9K; // Pop'n'Music
    // TODO Not sure if we want a 9k map :/
    EnumMap<KeyboardType, EnumMap<Event.Channel,Integer>> keyboard_map;

    ArrayList<String> dir_list;

    private static final File CONFIG_FILE = new File("config.obj");

    Level log_level = Level.INFO;
    FileHandler log_handle = null;

    static final Logger logger = Logger.getLogger(Config.class.getName());

    /** singleton object */
    private static Config config = null;

    public enum KeyboardType{K4, K5, K6, K7, K8, /*K9*/};

    private Config()
    {
        dir_list = new ArrayList<String>();
        dir_list.add(System.getProperty("user.dir"));

        // TODO Needs the 2nd player keys, if we are going to add 2p support ofc xD
        keyboard_map_4K = new EnumMap<Event.Channel,Integer>(Event.Channel.class);
        keyboard_map_4K.put(Event.Channel.NOTE_P1_1, Keyboard.KEY_D);
        keyboard_map_4K.put(Event.Channel.NOTE_P1_2, Keyboard.KEY_F);
        keyboard_map_4K.put(Event.Channel.NOTE_P1_3, Keyboard.KEY_J);
        keyboard_map_4K.put(Event.Channel.NOTE_P1_4, Keyboard.KEY_K);
        keyboard_map_4K.put(Event.Channel.NOTE_P1_SC, Keyboard.KEY_LSHIFT);
        
        keyboard_map_5K = new EnumMap<Event.Channel,Integer>(Event.Channel.class);
        keyboard_map_5K.put(Event.Channel.NOTE_P1_1, Keyboard.KEY_D);
        keyboard_map_5K.put(Event.Channel.NOTE_P1_2, Keyboard.KEY_F);
        keyboard_map_5K.put(Event.Channel.NOTE_P1_3, Keyboard.KEY_SPACE);
        keyboard_map_5K.put(Event.Channel.NOTE_P1_4, Keyboard.KEY_J);
        keyboard_map_5K.put(Event.Channel.NOTE_P1_5, Keyboard.KEY_K);
        keyboard_map_5K.put(Event.Channel.NOTE_P1_SC, Keyboard.KEY_LSHIFT);

        keyboard_map_6K = new EnumMap<Event.Channel,Integer>(Event.Channel.class);
        keyboard_map_6K.put(Event.Channel.NOTE_P1_1, Keyboard.KEY_S);
        keyboard_map_6K.put(Event.Channel.NOTE_P1_2, Keyboard.KEY_D);
        keyboard_map_6K.put(Event.Channel.NOTE_P1_3, Keyboard.KEY_F);
        keyboard_map_6K.put(Event.Channel.NOTE_P1_4, Keyboard.KEY_J);
        keyboard_map_6K.put(Event.Channel.NOTE_P1_5, Keyboard.KEY_K);
        keyboard_map_6K.put(Event.Channel.NOTE_P1_6, Keyboard.KEY_L);
        keyboard_map_6K.put(Event.Channel.NOTE_P1_SC, Keyboard.KEY_LSHIFT);

        keyboard_map_7K = new EnumMap<Event.Channel,Integer>(Event.Channel.class);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_1, Keyboard.KEY_S);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_2, Keyboard.KEY_D);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_3, Keyboard.KEY_F);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_4, Keyboard.KEY_SPACE);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_5, Keyboard.KEY_J);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_6, Keyboard.KEY_K);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_7, Keyboard.KEY_L);
        keyboard_map_7K.put(Event.Channel.NOTE_P1_SC,Keyboard.KEY_LSHIFT);

        keyboard_map_8K = new EnumMap<Event.Channel,Integer>(Event.Channel.class);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_1, Keyboard.KEY_A);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_2, Keyboard.KEY_S);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_3, Keyboard.KEY_D);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_4, Keyboard.KEY_F);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_5, Keyboard.KEY_H);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_6, Keyboard.KEY_J);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_7, Keyboard.KEY_K);
        keyboard_map_8K.put(Event.Channel.NOTE_P1_SC,Keyboard.KEY_L);

        keyboard_map = new EnumMap<KeyboardType, EnumMap<Event.Channel,Integer>>(KeyboardType.class);
        keyboard_map.put(KeyboardType.K4, keyboard_map_4K);
        keyboard_map.put(KeyboardType.K5, keyboard_map_5K);
        keyboard_map.put(KeyboardType.K6, keyboard_map_6K);
        keyboard_map.put(KeyboardType.K7, keyboard_map_7K);
        keyboard_map.put(KeyboardType.K8, keyboard_map_8K);
    }

    public EnumMap<Event.Channel,Integer> getKeyboardMap(KeyboardType kt){
        return keyboard_map.get(kt);
    }

    public void setKeyboardMap(EnumMap<Event.Channel,Integer> kb_map, KeyboardType kt){
        keyboard_map.get(kt).putAll(kb_map);
    }

    public ArrayList<String> getDirsList (){
        return dir_list;
    }

    public void addDir(String s){
        dir_list.add(s);
    }

    public void delDir(String s){
        dir_list.remove(s);
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

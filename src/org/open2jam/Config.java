package org.open2jam;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.input.Keyboard;
import org.open2jam.parsers.ChartList;
import org.open2jam.parsers.Event;
import org.open2jam.parsers.Event.Channel;
import org.voile.VoileMap;

/**
 *
 * @author fox
 */
public abstract class Config
{
    private static final File CONFIG_FILE = new File("config.vl");
    
    private static VoileMap<String, Serializable> VMap;
    private static GameOptions options;
            
    public static final String OPTIONS_FILE = "game-options.xml";

    private static GameOptions loadGameOptions() {
        try {
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(OPTIONS_FILE)));
            Object result = decoder.readObject();
            decoder.close();
            if (result instanceof GameOptions) return (GameOptions)result;
        } catch(FileNotFoundException fnf) {
            return null; // thats ok a new file will be created
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, "{0}", ex);
        }
        return null;
    }
    
    public static void saveGameOptions() {
        try {
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(OPTIONS_FILE)));
            encoder.writeObject(options);
            encoder.close();
        } catch (IOException ex) {
            Logger.getLogger(GameOptions.class.getName()).log(Level.SEVERE, "{0}", ex);
        }
    }

    public enum KeyboardType {K4, K5, K6, K7, K8, /*K9*/}
    
    public enum MiscEvent {  
        NONE,                       //None
        SPEED_UP, SPEED_DOWN,       //speed changes
        MAIN_VOL_UP, MAIN_VOL_DOWN, //main volume changes
        KEY_VOL_UP, KEY_VOL_DOWN,   //key volume changes
        BGM_VOL_UP, BGM_VOL_DOWN,   //bgm volume changes
        //CHAT_TOGGLE,                //chat toggle... if we are going to add one
    }
    
    public static void openDB() {
        
        options = loadGameOptions();
        
        if (options == null) {
            options = new GameOptions();
        }
        
        if(!CONFIG_FILE.exists()) { // create now
            
            VMap = new VoileMap<String, Serializable>(CONFIG_FILE);
            
            setCwd(null);
            
            setDirsList(new ArrayList<File>());

            EnumMap<MiscEvent, Integer> keyboard_misc = new EnumMap<MiscEvent, Integer>(MiscEvent.class);
            keyboard_misc.put(MiscEvent.SPEED_DOWN,   Keyboard.KEY_DOWN);
            keyboard_misc.put(MiscEvent.SPEED_UP,     Keyboard.KEY_UP);
            keyboard_misc.put(MiscEvent.MAIN_VOL_UP,  Keyboard.KEY_2);
            keyboard_misc.put(MiscEvent.MAIN_VOL_DOWN,Keyboard.KEY_1);
            keyboard_misc.put(MiscEvent.KEY_VOL_UP,   Keyboard.KEY_4);
            keyboard_misc.put(MiscEvent.KEY_VOL_DOWN, Keyboard.KEY_3);
            keyboard_misc.put(MiscEvent.BGM_VOL_UP,   Keyboard.KEY_6);
            keyboard_misc.put(MiscEvent.BGM_VOL_DOWN, Keyboard.KEY_5);
            put("keyboard_misc", keyboard_misc);
            
            // TODO Needs the 2nd player keys, if we are going to add 2p support ofc xD
            EnumMap<Event.Channel, Integer> keyboard_map_4K = new EnumMap<Event.Channel, Integer>(Event.Channel.class);
            keyboard_map_4K.put(Event.Channel.NOTE_1, Keyboard.KEY_D);
            keyboard_map_4K.put(Event.Channel.NOTE_2, Keyboard.KEY_F);
            keyboard_map_4K.put(Event.Channel.NOTE_3, Keyboard.KEY_J);
            keyboard_map_4K.put(Event.Channel.NOTE_4, Keyboard.KEY_K);
            keyboard_map_4K.put(Event.Channel.NOTE_SC, Keyboard.KEY_LSHIFT);
            put("keyboard_map"+KeyboardType.K4.toString(), keyboard_map_4K);

            EnumMap<Event.Channel, Integer> keyboard_map_5K = new EnumMap<Event.Channel, Integer>(Event.Channel.class);
            keyboard_map_5K.put(Event.Channel.NOTE_1, Keyboard.KEY_D);
            keyboard_map_5K.put(Event.Channel.NOTE_2, Keyboard.KEY_F);
            keyboard_map_5K.put(Event.Channel.NOTE_3, Keyboard.KEY_SPACE);
            keyboard_map_5K.put(Event.Channel.NOTE_4, Keyboard.KEY_J);
            keyboard_map_5K.put(Event.Channel.NOTE_5, Keyboard.KEY_K);
            keyboard_map_5K.put(Event.Channel.NOTE_SC, Keyboard.KEY_LSHIFT);
            put("keyboard_map"+KeyboardType.K5.toString(), keyboard_map_5K);

            EnumMap<Event.Channel, Integer> keyboard_map_6K = new EnumMap<Event.Channel, Integer>(Event.Channel.class);
            keyboard_map_6K.put(Event.Channel.NOTE_1, Keyboard.KEY_S);
            keyboard_map_6K.put(Event.Channel.NOTE_2, Keyboard.KEY_D);
            keyboard_map_6K.put(Event.Channel.NOTE_3, Keyboard.KEY_F);
            keyboard_map_6K.put(Event.Channel.NOTE_4, Keyboard.KEY_J);
            keyboard_map_6K.put(Event.Channel.NOTE_5, Keyboard.KEY_K);
            keyboard_map_6K.put(Event.Channel.NOTE_6, Keyboard.KEY_L);
            keyboard_map_6K.put(Event.Channel.NOTE_SC, Keyboard.KEY_LSHIFT);
            put("keyboard_map"+KeyboardType.K6.toString(), keyboard_map_6K);

            EnumMap<Event.Channel, Integer> keyboard_map_7K = new EnumMap<Event.Channel, Integer>(Event.Channel.class);
            keyboard_map_7K.put(Event.Channel.NOTE_1, Keyboard.KEY_S);
            keyboard_map_7K.put(Event.Channel.NOTE_2, Keyboard.KEY_D);
            keyboard_map_7K.put(Event.Channel.NOTE_3, Keyboard.KEY_F);
            keyboard_map_7K.put(Event.Channel.NOTE_4, Keyboard.KEY_SPACE);
            keyboard_map_7K.put(Event.Channel.NOTE_5, Keyboard.KEY_J);
            keyboard_map_7K.put(Event.Channel.NOTE_6, Keyboard.KEY_K);
            keyboard_map_7K.put(Event.Channel.NOTE_7, Keyboard.KEY_L);
            keyboard_map_7K.put(Event.Channel.NOTE_SC, Keyboard.KEY_LSHIFT);
            put("keyboard_map"+KeyboardType.K7.toString(), keyboard_map_7K);

            EnumMap<Event.Channel, Integer> keyboard_map_8K = new EnumMap<Event.Channel, Integer>(Event.Channel.class);
            keyboard_map_8K.put(Event.Channel.NOTE_1, Keyboard.KEY_A);
            keyboard_map_8K.put(Event.Channel.NOTE_2, Keyboard.KEY_S);
            keyboard_map_8K.put(Event.Channel.NOTE_3, Keyboard.KEY_D);
            keyboard_map_8K.put(Event.Channel.NOTE_4, Keyboard.KEY_F);
            keyboard_map_8K.put(Event.Channel.NOTE_5, Keyboard.KEY_H);
            keyboard_map_8K.put(Event.Channel.NOTE_6, Keyboard.KEY_J);
            keyboard_map_8K.put(Event.Channel.NOTE_7, Keyboard.KEY_K);
            keyboard_map_8K.put(Event.Channel.NOTE_SC, Keyboard.KEY_L);
            put("keyboard_map"+KeyboardType.K8.toString(), keyboard_map_8K);

        } else {           
            VMap = new VoileMap<String, Serializable>(CONFIG_FILE);
        }
        
    }

    @SuppressWarnings("unchecked")
    public static EnumMap<Event.Channel,Integer> getKeyboardMap(KeyboardType kt){
        return (EnumMap<Channel, Integer>) get("keyboard_map"+kt.toString());
    }
    
    @SuppressWarnings("unchecked")
    public static EnumMap<MiscEvent,Integer> getKeyboardMisc(){
        return (EnumMap<MiscEvent, Integer>) get("keyboard_misc");
    }
    
    public static void setKeyboardMisc(EnumMap<MiscEvent,Integer> km_map)
    {
        put("keyboard_misc", km_map);
    }

    public static void setKeyboardMap(EnumMap<Event.Channel,Integer> kb_map, KeyboardType kt){
        put("keyboard_map"+kt.toString(), kb_map);
    }

    public static File getCwd() {
        return (File) get("cwd");
    }
    public static void setCwd(File new_file) {
        put("cwd",new_file);
    }

    public static void setDirsList(ArrayList<File> dl) {
        put("dir_list",dl);
    }
    
    @SuppressWarnings("unchecked")
    public static ArrayList<File> getDirsList() {
        return (ArrayList<File>) get("dir_list");
    }
    
    public static void setGameOptions(GameOptions go) {
        options = go;
        saveGameOptions();
    }
    
    public static GameOptions getGameOptions() {
        return options;
    }
    
    @SuppressWarnings("unchecked")
    public static ArrayList<ChartList> getCache(File dir) {
        return (ArrayList<ChartList>) get("cache:"+dir.getAbsolutePath());
    }

    public static void setCache(File dir, ArrayList<ChartList> data) {
        delete("cache:"+dir.getAbsolutePath());
        put("cache:"+dir.getAbsolutePath(),data);
    }

    public static void delCache(File dir){
        delete("cache:"+dir.getAbsolutePath());
    }
    
    private static Serializable get(String key) {
        return VMap.get(key);
    }
    
    private static void put(String key, Serializable value) {
        VMap.put(key, value);
    }

    private static void delete(String key) {
        VMap.remove(key);
    }
}

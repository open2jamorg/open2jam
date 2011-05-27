package org.open2jam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import org.lwjgl.input.Keyboard;
import org.open2jam.parser.ChartList;
import org.open2jam.parser.Event;
import org.open2jam.parser.Event.Channel;
import org.open2jam.util.Logger;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.schema.SqlJetConflictAction;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 *
 * @author fox
 */
public abstract class Config
{
    private static final File CONFIG_DBFILE = new File("config.db");
    
    private static SqlJetDb db;
    private static ISqlJetTable table;

    private static final String KEY_INDEX = "key_index";
    private static final String DATA_TABLE = "data";
    private static final String VALUE_FIELD = "value";
    private static String create_table = 
            "CREATE TABLE "+DATA_TABLE+" (key TEXT PRIMARY KEY, "+VALUE_FIELD+" BLOB )";
    private static String create_index = 
            "CREATE INDEX "+KEY_INDEX+" ON "+DATA_TABLE+"(key)";

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
        
        if(!CONFIG_DBFILE.exists()) { // create now
        
            try {
                db = SqlJetDb.open(CONFIG_DBFILE, true);
                db.createTable(create_table);
                db.createIndex(create_index);
                
                table = db.getTable(DATA_TABLE);

            } catch(SqlJetException e) {
                Logger.global.severe("Unable to create config db !");
                db = null;
                return;
            }
            
            setCwd(null);
            
            setDirsList(new ArrayList<File>());

            EnumMap<MiscEvent, Integer> keyboard_misc = new EnumMap<MiscEvent, Integer>(MiscEvent.class);
            keyboard_misc.put(MiscEvent.SPEED_DOWN, Keyboard.KEY_DOWN);
            keyboard_misc.put(MiscEvent.SPEED_UP, Keyboard.KEY_UP);
            keyboard_misc.put(MiscEvent.MAIN_VOL_UP, Keyboard.KEY_1);
            keyboard_misc.put(MiscEvent.MAIN_VOL_DOWN, Keyboard.KEY_2);
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
            try {
                db = SqlJetDb.open(CONFIG_DBFILE, true);
                table = db.getTable(DATA_TABLE);
            } catch(SqlJetException e) {
                Logger.global.severe("Unable to open the config db !");
                db = null;
            }
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

    public static void setKeyboardMap(EnumMap<Event.Channel,Integer> kb_map, KeyboardType kt){
        put("keyboard_map"+kt.toString(), kb_map);
    }

    public static File getCwd() {
        return (File) get("cwd");
    }
    public static void setCwd(File new_file) {
        put("cwd",new_file);
    }

    public static void setDirsList(List<File> dl) {
        put("dir_list",dl);
    }
    @SuppressWarnings("unchecked")
    public static List<File> getDirsList() {
        return (List<File>) get("dir_list");
    }
    
    @SuppressWarnings("unchecked")
    public static List<ChartList> getCache(File dir) {
        return (List<ChartList>) get("cache:"+dir.getAbsolutePath());
    }
    public static void setCache(File dir, List<ChartList> data) {
        put("cache:"+dir.getAbsolutePath(),data);
    }
    
    private static Object get(String key) {
        if(db == null)return null;
        
        try {
            db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
            try {
                ISqlJetCursor cursor = table.lookup(KEY_INDEX,key);
                if(cursor.eof())return null;
                return blob2object(cursor.getBlobAsStream(VALUE_FIELD));
                
            } finally {
                db.commit();
            }
        } catch(SqlJetException e) {
            Logger.global.log(Level.SEVERE, "Error during db get: {0}", e.getMessage());
        }
        return null;
    }
    
    private static void put(String key, Object value) {
        if(db == null)return;
        
        try {
            table.insertOr(SqlJetConflictAction.REPLACE, key, object2blob(value));
        } catch(SqlJetException e) {
            Logger.global.log(Level.SEVERE, "Error during db put: {0}", e.getMessage());
        }
    }

    private static byte[] object2blob(Object o) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(bao).writeObject(o);
        } catch (IOException ignored) {
            Logger.global.severe("We are in an alternate reality were ByteArrayOutputStream throws exceptions !");
            return null;
        }
        return bao.toByteArray();
    }
    private static Object blob2object(InputStream blob) {
        try {
            return new ObjectInputStream(blob).readObject();
        } catch(IOException e) {
            Logger.global.log(Level.SEVERE, "Config db read error : {0}", e.getMessage());
        } catch (ClassNotFoundException ignored) {
            Logger.global.severe("Oh no ! I got a unknown object ! gonna throw it away for ya..");
        }
        return null;
    }
}

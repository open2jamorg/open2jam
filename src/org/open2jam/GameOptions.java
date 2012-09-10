package org.open2jam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.DisplayMode;
import org.open2jam.parsers.Event;

/**
 * This class will store game options such as hi-speed and autoplay.
 * @author SilverHx
 */
public class GameOptions {
    
    private static final File OPTIONS_FILE = new File("game_options.properties");
    
    public static final Properties DEFAULT_PROPERTIES = new Properties();
    
    interface PropertyBinding {
        public void load(GameOptions options, String value);
        public String save(GameOptions options);
    }
    
    static class Binding implements PropertyBinding {
        
        final String fieldName;
        
        public Binding(String fieldName) {
            this.fieldName = fieldName;
        }
        
        @Override
        public void load(GameOptions options, String value) {
            try {
                GameOptions.class.getDeclaredField(fieldName).set(options, parse(value));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        
        public Object parse(String value) { return value; }
        public String stringify(Object value) { return value.toString(); }

        @Override
        public String save(GameOptions opts) {
            try {
                return stringify(GameOptions.class.getDeclaredField(fieldName).get(opts));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        
    }
    
    static class DoubleBinding extends Binding {
        public DoubleBinding(String fieldName) { super(fieldName); }
        @Override
        public Object parse(String value) { return Double.parseDouble(value); }
    }
    static class FloatBinding extends Binding {
        public FloatBinding(String fieldName) { super(fieldName); }
        @Override
        public Object parse(String value) { return Float.parseFloat(value); }
    }
    static class BooleanBinding extends Binding {
        public BooleanBinding(String fieldName) { super(fieldName); }
        @Override
        public Object parse(String value) { return Boolean.parseBoolean(value); }
    }
    static class IntegerBinding extends Binding {
        public IntegerBinding(String fieldName) { super(fieldName); }
        @Override
        public Object parse(String value) { return Integer.parseInt(value); }
    }
    
    static class EnumBinding extends Binding {
        
        public Class enumClass;
        
        public EnumBinding(Class enumClass, String fieldName) {
            super(fieldName);
            this.enumClass = enumClass;
        }

        @Override
        public Object parse(String value) {
            for (Object o : enumClass.getEnumConstants()) {
                if (((Enum)o).name().equals(value)) return o;
            }
            return null;
        }

        @Override
        public String stringify(Object value) {
            return ((Enum)value).name();
        }
        
    }
    
    static enum Property {
        speedMultiplier("mods.speed.multiplier", "1.0", new DoubleBinding("hispeed")),
        speedType("mods.speed.type", "HiSpeed", new EnumBinding(SpeedType.class, "speed_type")),
        visibility("mods.visibility", "None", new EnumBinding(VisibilityMod.class, "visibilityModifier")),
        channel("mods.channel", "None", new EnumBinding(ChannelMod.class, "channelModifier")),
        keyVolume("volume.key", "1.0", new FloatBinding("keyVolume")),
        bgmVolume("volume.bgm", "1.0", new FloatBinding("BGMVolume")),
        masterVolume("volume.master", "1.0", new FloatBinding("masterVolume")),
        autoplayEnabled("autoplay.enabled", "false", new BooleanBinding("autoplay")),
        autoplayChannels("autoplay.channels", "", new Binding("autoplay_channels") {

            @Override
            public Object parse(String value) {
                ArrayList<Boolean> out = new ArrayList<Boolean>();
                if (value.isEmpty()) {
                    for(Event.Channel c : Event.Channel.values())
                    {
                        if(c.toString().startsWith("NOTE_"))
                            out.add(c.isAutoplay());
                    }
                } else for (String s : value.split(",")) {
                    out.add(Boolean.parseBoolean(s));
                }
                return out;
            }

            @Override
            public String stringify(Object value) {
                StringBuilder builder = new StringBuilder();
                for (boolean c : (ArrayList<Boolean>)value) {
                    if (builder.length() > 0) builder.append(",");
                    builder.append(Boolean.toString(c));
                }
                return builder.toString();
            }
            
        }),
        autosoundEnabled("autosound.enabled", "false", new BooleanBinding("autosound")),
        fullScreen("display.full_screen", "false", new BooleanBinding("fullscreen")),
        bilinear("display.bilinear", "true", new BooleanBinding("bilinear")),
        vsync("display.vsync", "true", new BooleanBinding("vsync")),
        width("display.width", "0", new IntegerBinding("width")),
        height("display.height", "0", new IntegerBinding("height")),
        bpp("display.bpp", "0", new IntegerBinding("bpp")),
        freq("display.freq", "0", new IntegerBinding("freq")),
        vlcPath("vlc.library_path", "", new Binding("vlc")),
        displayLag("latency.display", "0.0", new DoubleBinding("displayLag"));
        
        final String name;
        final String defaultValue;
        final PropertyBinding binding;

        private Property(String name, String defaultValue, PropertyBinding binding) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.binding = binding;
        }
        
        public void load(Properties props, GameOptions opts) {
            binding.load(opts, props.getProperty(name));
        }
        
        public void save(Properties props, GameOptions opts) {
            props.setProperty(name, binding.save(opts));
        }
        
    }
    
    static {
        for (Property property : Property.values()) {
            DEFAULT_PROPERTIES.setProperty(property.name, property.defaultValue);
        }
    }
    
    /*
     * "Hi-Speed"=>0, "xR-Speed"=>1, "W-Speed"=>2
     */
    public enum SpeedType {
        HiSpeed, xRSpeed, WSpeed;
        
        @Override
        public String toString() {
            return super.toString().replace("Speed", "-Speed:");
        }
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
    double hispeed;
    SpeedType speed_type;
    VisibilityMod visibilityModifier;
    ChannelMod channelModifier;
    
    float keyVolume, BGMVolume, masterVolume;
    
    boolean autoplay;
    ArrayList<Boolean> autoplay_channels;
    boolean autosound;

    // display options
    boolean fullscreen, bilinear, vsync;
    int width,height,bpp,freq;
    
    // VLC lib path
    String vlc = "";
    double displayLag = 0;

    //public constructor. give default options
    public GameOptions() {
        load();
    }
    
    public void load() {
        Properties properties = new Properties(DEFAULT_PROPERTIES);
        try {
            properties.load(new FileInputStream(OPTIONS_FILE));
        } catch (IOException ex) {
            Logger.getLogger(GameOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Property property : Property.values()) {
            property.load(properties, this);
        }
    }
    
    public void save() {
        Properties properties = new Properties();
        for (Property property : Property.values()) {
            property.save(properties, this);
        }
        try {
            properties.store(new FileOutputStream(OPTIONS_FILE), "save the settings");
        } catch (IOException ex) {
            Logger.getLogger(GameOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public double getHiSpeed() {
        return hispeed;
    }

    /**
     * Sets hi-speed value.
     * @param new hi-speed value
     */
    public void setHispeed(double hispeed) {
        this.hispeed = hispeed;
    }

    /**
     * get the speed type
     * @return 
     */
    public SpeedType getSpeedType() {
        return speed_type;
    }

    /**
     * set the speed type
     * @param mod 
     */
    public void setSpeedType(SpeedType mod) {
        this.speed_type = mod;
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
    
    /**
     * Gets autosound option value.
     * @return true if autosound option is enabled, false otherwise
     */
    public boolean getAutosound() {
        return autosound;
    }

    /**
     * Sets autosound option value.
     * @param new autosound option value
     */
    public void setAutosound(boolean autosound) {
        this.autosound = autosound;
    }
    
    public List<Boolean> getAutoplayChannels() {
	return autoplay_channels;
    }
    
    public void setAutoplayChannels(List<Boolean> list) {
	this.autoplay_channels = (ArrayList<Boolean>) list;
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
    
    public void setVLC(String path) {
	this.vlc = path;
    }
    
    public String getVLC() {
	return vlc;
    }

    public void setDisplay(DisplayMode dm) {
        this.width = dm.getWidth();
        this.height = dm.getHeight();
        this.bpp = dm.getBitsPerPixel();
        this.freq = dm.getFrequency();
    }

    public double getDisplayLag() {
        return displayLag;
    }

    public void setDisplayLag(double displayLag) {
        this.displayLag = displayLag;
    }
    
    public boolean isDisplaySaved(DisplayMode dm)
    {
        return dm.getWidth() == width && dm.getHeight() == height &&
               dm.getBitsPerPixel() == bpp && dm.getFrequency() == freq;
    }
    
    private double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}

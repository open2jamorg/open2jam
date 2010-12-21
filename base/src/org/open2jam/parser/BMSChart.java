package org.open2jam.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class BMSChart extends Chart
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected int lntype;

    protected File source;
    protected int lnobj;

    public File getSource() { return source; }

    protected int level;
    public int getLevel() { return level; }

    protected int keys;
    public int getKeys()  {  return keys; }


    protected String title;
    public String getTitle() {
        return title;
    }

    protected String artist;
    public String getArtist() {
        return artist;
    }

    protected String genre;
    public String getGenre() {
        return genre;
    }

    protected Map<String, Integer> sample_files;
    public Map<Integer,Integer> getSamples() {
        return BMSParser.loadSamples(this);
    }

    protected double bpm = 130;
    public double getBPM() {
        return bpm;
    }

    protected int notes = 0;
    public int getNoteCount() { return notes; }
    protected int duration = 0;
    public int getDuration() { return duration; }

    protected File image_cover;
    public BufferedImage getCover() {
        if(image_cover == null)return null;
        try {
            return ImageIO.read(image_cover);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "IO Error on reading cover: {0}", ex.getMessage());
        }
        return null;
    }

    public String getNoter() { return ""; }

    public List<Event> getEvents() {
        return BMSParser.parseChart(this);
    }
}

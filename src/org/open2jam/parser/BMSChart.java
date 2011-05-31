package org.open2jam.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.open2jam.util.Logger;
import javax.imageio.ImageIO;

public class BMSChart extends Chart
{
    int lntype;
    boolean o2mania_style;

    File source;
    int lnobj;

    public File getSource() { return source; }

    int level;
    public int getLevel() { return level; }

    int keys;
    public int getKeys()  {  return keys; }


    String title;
    public String getTitle() {
        return title;
    }

    String artist;
    public String getArtist() {
        return artist;
    }

    String genre;
    public String getGenre() {
        return genre;
    }

    Map<String, Integer> sample_files;
    public Map<Integer,Integer> getSamples() {
        return BMSParser.loadSamples(this);
    }

    double bpm = 130;
    public double getBPM() {
        return bpm;
    }

    int notes = 0;
    public int getNoteCount() { return notes; }

    int duration = 0;
    public int getDuration() { return duration; }

    File image_cover;
    public BufferedImage getCover() {
        if(image_cover == null)return null;
        try {
            return ImageIO.read(image_cover);
        } catch (IOException ex) {
            Logger.global.log(Level.WARNING, "IO Error on reading cover: {0}", ex.getMessage());
        }
        return null;
    }

    public String getNoter() { return ""; }


    public List<Event> getEvents() {
        return BMSParser.parseChart(this);
    }
}

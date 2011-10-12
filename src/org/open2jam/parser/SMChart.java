package org.open2jam.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.open2jam.util.Logger;
import javax.imageio.ImageIO;

public class SMChart extends Chart
{

    File source;


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
        return SMParser.loadSamples(this);
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
        if(image_cover == null) return getNoImage();
        try {
            return ImageIO.read(image_cover);
        } catch (IOException ex) {
            Logger.global.log(Level.WARNING, "IO Error on reading cover: {0}", ex.getMessage());
        }
        return null;
    }
    
    String noter;
    public String getNoter() { return noter; }


    public List<Event> getEvents() {
        return SMParser.parseChart(this);
    }
}

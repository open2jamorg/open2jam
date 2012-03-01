package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.open2jam.parsers.utils.AudioData;
import org.open2jam.parsers.utils.Logger;

public class BMSChart extends Chart
{
    int lntype;
    int lnobj;
    
    boolean o2mania_style;
    
    public File getSource() {
	return source; 
    }

    public int getLevel() {
	return level; 
    }

    public int getKeys() {
	return keys; 
    }
    
    public int getPlayers() {
	return players;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }
    
    public String getNoter() {
	return noter; 
    }

    public Map<Integer,AudioData> getSamples() {
        return BMSParser.getSamples(this);
    }

    public Map<Integer, String> getSampleIndex() {
	return sample_index;
    }

    public double getBPM() {
        return bpm;
    }

    public int getNoteCount() {
	return notes; 
    }

    public int getDuration() {
	return duration; 
    }

    public BufferedImage getCover() {
        if(image_cover == null) return getNoImage();
        try {
            return ImageIO.read(image_cover);
        } catch (IOException ex) {
            Logger.global.log(Level.WARNING, "IO Error on reading cover: {0}", ex.getMessage());
        }
        return null;
    }

    public List<Event> getEvents() {
        return BMSParser.parseChart(this);
    }
}

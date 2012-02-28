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

public class SMChart extends Chart {

    File source;
    public File getSource() {
	return source;
    }
    
    int level = 0;
    public int getLevel() {
	return level;
    }
    
    int keys = 4;
    public int getKeys() {
	return keys;
    }
    
    int players = 1;
    public int getPlayers() {
	return players;
    }
    
    String title = "";
    public String getTitle() {
	return title;
    }
    
    String artist = "";
    public String getArtist() {
	return artist;
    }
    
    String genre = "";
    public String getGenre() {
	return genre;
    }
    
    String noter = "";
    public String getNoter() {
	return noter;
    }
    
    Map<String, Integer> sample_files;
    public Map<Integer, AudioData> getSamples() {
	return SMParser.loadSamples(this);
    }

    double bpm = 130d;
    public double getBPM() {
	return bpm;
    }
    
    int notes = 0;
    public int getNoteCount() {
	return notes;
    }
    
    int duration = 0;
    public int getDuration() {
	return duration;
    }
    
    File image_cover;
    public BufferedImage getCover() {
	if (image_cover == null) {
	    return getNoImage();
	}
	try {
	    return ImageIO.read(image_cover);
	} catch (IOException ex) {
	    Logger.global.log(Level.WARNING, "IO Error on reading cover: {0}", ex.getMessage());
	}
	return null;
    }

    public List<Event> getEvents() {
	return SMParser.parseChart(this);
    }
}

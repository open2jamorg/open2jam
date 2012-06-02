package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.open2jam.parsers.utils.Logger;
import org.open2jam.parsers.utils.SampleData;

public class SMChart extends Chart {

    public SMChart() {
	type = TYPE.SM;
    }

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
    
    public Map<Integer, SampleData> getSamples() {
	return SMParser.getSamples(this);
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
	if (!hasCover()) {
	    return getNoImage();
	}
	try {
	    return ImageIO.read(image_cover);
	} catch (IOException ex) {
	    Logger.global.log(Level.WARNING, "IO Error on reading cover: {0}", ex.getMessage());
	}
	return null;
    }

    public EventList getEvents() {
	return SMParser.parseChart(this);
    }
}

package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.open2jam.parsers.utils.AudioData;
import org.open2jam.parsers.utils.ByteBufferInputStream;
import org.open2jam.parsers.utils.Logger;

public class OJNChart extends Chart {

    int note_offset;
    int note_offset_end;
    int cover_offset;
    int cover_size;
    
    File source;
    public File getSource() {
	return source;
    }
    
    short level = 0;
    public int getLevel() {
	return level;
    }

    int keys = 7;
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
    
    File sample_file;
    public Map<Integer, AudioData> getSamples() {
	return OJMParser.parseFile(sample_file);
    }
    
    double bpm = 120d;
    public double getBPM() {
	return bpm;
    }
    
    int note_count = 0;
    public int getNoteCount() {
	return note_count;
    }
    
    int duration = 0;
    public int getDuration() {
	return duration;
    }

    public BufferedImage getCover() {
	if (cover_size <= 0) {
	    return getNoImage();
	}
	try {
	    RandomAccessFile f = new RandomAccessFile(source, "r");
	    ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, cover_offset, cover_size);
	    ByteBufferInputStream bis = new ByteBufferInputStream(buffer);
	    f.close();
	    return ImageIO.read(bis);
	} catch (IOException e) {
	    Logger.global.log(Level.WARNING, "IO exception getting image from file {0}", source.getName());
	}
	return null;
    }

    public List<Event> getEvents() {
	return OJNParser.parseChart(this);
    }
}
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
import org.open2jam.parsers.utils.ByteBufferInputStream;
import org.open2jam.parsers.utils.Logger;
import org.open2jam.parsers.utils.SampleData;

public class OJNChart extends Chart {

    int note_offset;
    int note_offset_end;
    int cover_offset;
    int cover_size;
    
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
    
    File sample_file;
    public Map<Integer, SampleData> getSamples() {
	return OJMParser.parseFile(sample_file);
    }
    
    public Map<Integer, String> getSampleIndex() {
//	if(sample_index.isEmpty())
//	    sample_index = OJMParser.getSampleIndex(sample_file);
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
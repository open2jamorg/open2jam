package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Map.Entry;
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

    public OJNChart() {
	type = TYPE.OJN;
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
    
    File sample_file;
    public Map<Integer, SampleData> getSamples() {
	return OJMParser.parseFile(sample_file);
    }
    
    public Map<Integer, String> getSampleIndex() {
	if(sample_index.isEmpty()) {
	    for(Entry<Integer, SampleData> entry : getSamples().entrySet()) {
		    sample_index.put(entry.getKey(), entry.getValue().getName());
		try {
		    entry.getValue().dispose();
		} catch (IOException ex) {
		    Logger.global.log(Level.WARNING, "As if I care about it :/");
		}
	    }
	}
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
    
    public String getCoverName() {
	if(!hasCover()) return null;
	return "OJN_"+this.title+"_"+this.level;
    }
    
    public boolean hasCover() {
	return cover_size > 0;
    }

    public BufferedImage getCover() {
	if (!hasCover()) {
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

    public EventList getEvents() {
	return OJNParser.parseChart(this);
    }
}
package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.open2jam.parsers.utils.ByteHelper;
import org.open2jam.parsers.utils.Logger;
import org.open2jam.parsers.utils.SampleData;

/** 
* this encapsulates a song chart.
* in case there's more than one rank(difficulty)
* for the song, the rank integer follows this pattern:
* 0 - easy, 1 - normal, 2 - hard, 3 - very hard, ... 
* there's no upper bound.
*/
public abstract class Chart implements Comparable<Chart>, java.io.Serializable
{
    public static enum TYPE {NONE, BMS, OJN, SM, XNT};
    
    public TYPE type = TYPE.NONE;
    
    protected File source;
    protected int level = 0;
    protected int keys = 7;
    protected int players = 1;
    protected String title = "";
    protected String artist = "";
    protected String genre = "";
    protected String noter = "";
    protected double bpm = 130;
    protected int notes = 0;
    protected int duration = 0;
    
    protected String cover_name = null;
    protected File image_cover = null;
    protected File video = null;
    
    protected Map<Integer, String> sample_index = new HashMap<Integer, String>();
    protected Map<Integer, String> bga_index = new HashMap<Integer, String>();
    
    /** the File object to the source file of this header */
    public abstract File getSource();

    /** 
    * an integer representing difficulty.
    * we _should_ have some standard here
    * maybe we could use o2jam as the default
    * and normalize the others to this rule
    */
    public abstract int getLevel();

    /** The number of keys in this chart */
    public abstract int getKeys();
    
    /** The number of player for this chart */
    public abstract int getPlayers();

    /** The title of the song */
    public abstract String getTitle();
    
    /** The artist of the song */
    public abstract String getArtist();
    
    /** The genre of the song */
    public abstract String getGenre();
    
    /** The noter of the song (Unused?) */
    public abstract String getNoter();
    
    /** The samples of the song */
    public Map<Integer, SampleData> getSamples() {
	return new HashMap<Integer, SampleData>();
    }
    
    /** The images of the song */
    public Map<Integer, File> getImages() {
	return new HashMap<Integer, File>();
    }

    /** a bpm representing the whole song.
    *** doesn't need to be exact, just for info */
    public abstract double getBPM();

    /** the number of notes in the song */
    public abstract int getNoteCount();

    /** the duration in seconds */
    public abstract int getDuration();

    /** a image cover, representing the song */
    public abstract BufferedImage getCover();
    
    /** this should return the list of events from this chart at this rank */
    public abstract EventList getEvents();
    
    /** return the cover image name without extension or null if there is no cover name */
    public String getCoverName() {
	if(cover_name == null) return null;
	
	int dot = cover_name.lastIndexOf(".");
	return cover_name.substring(0, dot);
    }
    
    /** Return true if the chart has a cover */
    public boolean hasCover() {
	return image_cover != null;
    }
    
    /** Return true if the chart has a video */
    public boolean hasVideo() {
	return video != null;
    }
    
    public File getVideo() {
	return video;
    }
           
    /** Get the sample index of the chart */
    public Map<Integer, String> getSampleIndex() {
	return sample_index;
    }
    
    /** Get the image index of the chart */
    public Map<Integer, String> getBgaIndex() {
	return bga_index;
    }
    
    /** Copy the sample files to another directory */
    public void copySampleFiles(File directory) throws IOException {
	Collection<SampleData> samples = getSamples().values();
	if(samples.isEmpty()) return;
	for(SampleData ad : samples) {
	    ad.copyToFolder(directory);
	}
    }
    
    public void copyBgaFiles(File directory) throws FileNotFoundException, IOException {
	Collection<File> images = getImages().values();
	if(images.isEmpty()) return;
	for(File f : images) {
	    File out = new File(directory, f.getName());
	    if(!out.exists()) {
		ByteHelper.copyTo(new FileInputStream(f), new FileOutputStream(out));
	    }
	}
    }

    public int compareTo(Chart c)
    {
        return getLevel() - c.getLevel();
    }
    
    public BufferedImage getNoImage()
    {
	URL u = Chart.class.getResource("/resources/no_image.png"); //TODO Change this
	if(u == null) return null;
	
	try {
	    return ImageIO.read(new File(u.toURI()));
	} catch (Exception ex) {
	    Logger.global.log(Level.WARNING, "Someone deleted or renamed my no_image image file :_ {0}", ex.getMessage());
	}
	return null;
    }
}

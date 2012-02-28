package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.open2jam.parsers.utils.AudioData;
import org.open2jam.parsers.utils.Logger;

/** 
* this encapsulates a song chart.
* in case there's more than one rank(difficulty)
* for the song, the rank integer follows this pattern:
* 0 - easy, 1 - normal, 2 - hard, 3 - very hard, ... 
* there's no upper bound.
*/
public abstract class Chart implements Comparable<Chart>, java.io.Serializable
{
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
    public abstract Map<Integer, AudioData> getSamples();

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
    public abstract List<Event> getEvents();
    
    /** 
     * This method will return a map with measures and a list of events for each measure
     * @param event_list The list of events
     * @return A map with measures => list of events
     */
    public static Map<Integer, List<Event>> getEventsPerMeasure(List<Event> event_list) {
	Map<Integer, List<Event>> epm = new HashMap<Integer, List<Event>>();
	
	Integer max_measure = null;
	for(Event e : event_list) {
	    if(max_measure == null || e.getMeasure() > max_measure) {
		max_measure = e.getMeasure();
		epm.put(e.getMeasure(), new ArrayList<Event>());
	    }
	    
	    epm.get(max_measure).add(e);
	}
	
	return epm;
    }
    
    public static Map<Event.Channel, List<Event>> getEventsPerChannel(List<Event> event_list) {
	Map<Event.Channel, List<Event>> epc = new EnumMap<Event.Channel, List<Event>>(Event.Channel.class);
	
//	for(Event.Channel c : Event.Channel.values()) 
//	    epc.put(c, new ArrayList<Event>());
	
	for(Event e : event_list) {
	    if(!epc.containsKey(e.getChannel()))
		epc.put(e.getChannel(), new ArrayList<Event>());
	    epc.get(e.getChannel()).add(e);
	}
	
	return epc;
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

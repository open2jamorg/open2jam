package org.open2jam.parsers;

import java.io.File;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.open2jam.utils.Logger;
/** this encapsulates a song chart.
*** in case there's more than one rank(difficulty)
*** for the song, the rank integer follows this pattern:
*** 0 - easy, 1 - normal, 2 - hard, 3 - very hard, ... 
*** there's no upper bound.
**/
public abstract class Chart implements Comparable<Chart>, java.io.Serializable
{
    /** the File object to the source file of this header */
    public abstract File getSource();

    /** an integer representing difficulty.
    *** we _should_ have some standard here
    *** maybe we could use o2jam as the default
    *** and normalize the others to this rule
    **/
    public abstract int getLevel();

    /** the number of keys in this chart */
    public abstract int getKeys();

    public abstract String getTitle();
    public abstract String getArtist();
    public abstract String getGenre();
    public abstract String getNoter();
    public abstract Map<Integer,Integer> getSamples();

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
	} catch (URISyntaxException ex) {
	    Logger.global.log(Level.WARNING, "Someone deleted or renamed my no_image image file :_ {0}", ex.getMessage());
	} catch (IOException ex) {
	    Logger.global.log(Level.WARNING, "Someone deleted or renamed my no_image image file :_ {0}", ex.getMessage());
	}
	return null;
    }
}

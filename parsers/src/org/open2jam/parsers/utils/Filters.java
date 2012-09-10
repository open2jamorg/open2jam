package org.open2jam.parsers.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This class contains a bunch of common FilenameFilter
 * @author CdK
 */
public class Filters {
    
    public static final FilenameFilter sampleFilter = new FilenameFilter() {

	public boolean accept(File dir, String name) {
            if (name.lastIndexOf(".") == -1) return false;
	    String n = name.substring(name.lastIndexOf("."), name.length());

	    return (n.equalsIgnoreCase(".wav")
		    || n.equalsIgnoreCase(".ogg")
		    || n.equalsIgnoreCase(".mp3"));
	}
    };
    
    public static final FilenameFilter videoFilter = new FilenameFilter() {

	    public boolean accept(File dir, String name) {
                if (name.lastIndexOf(".") == -1) return false;
		String n = name.substring(name.lastIndexOf("."), name.length());
		
		return (n.equalsIgnoreCase(".avi") 
			|| n.equalsIgnoreCase(".mpg")
			|| n.equalsIgnoreCase(".mpeg")
			|| n.equalsIgnoreCase(".mov")
			|| n.equalsIgnoreCase(".mkv")
			|| n.equalsIgnoreCase(".flv")
			|| n.equalsIgnoreCase(".mp4"));
	    }
	};
    
    public static final FilenameFilter imageFilter = new FilenameFilter() {

	public boolean accept(File dir, String name) {
            if (name.lastIndexOf(".") == -1) return false;
	    String n = name.substring(name.lastIndexOf("."), name.length());

	    return (n.equalsIgnoreCase(".bmp")
		    || n.equalsIgnoreCase(".png")
		    || n.equalsIgnoreCase(".jpg")
		    || n.equalsIgnoreCase(".jpeg"));
	}
    };
}

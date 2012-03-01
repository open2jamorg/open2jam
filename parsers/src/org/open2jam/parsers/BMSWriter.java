package org.open2jam.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.open2jam.parsers.Event.Channel;

/**
 *
 * @author CdK
 */
public class BMSWriter {
    
    private static final int MAX_POSITIONS = 192;
    
    private static List<Event> event_list;
    private static HashMap<Event, Integer> bpmChanges;
    
    public static void export(ChartList list, String path) throws IOException
    {
	for(Chart c : list) {
	    export(c, path);
	}
    }
    
    public static void export(Chart chart, String path) throws IOException
    {
	event_list = chart.getEvents();
	
	String dirName = (chart.getArtist()+" - "+chart.getTitle()).replaceAll("/", " ");
	String name = chart.getSource().getName();
	name = name.substring(0, name.lastIndexOf("."))+"_"+chart.hashCode()+".bms";
	File dirs = new File(path+File.separator+dirName);
	File file = new File(dirs, name);
	dirs.mkdirs();
	file.createNewFile();
	System.out.println(file.getAbsolutePath());
	
	BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
        
	makeHeader(buffer, chart);
	writeEvents(buffer, chart);
	
	buffer.close();
    }
    
    private static void makeHeader(BufferedWriter buffer, Chart chart) throws IOException
    {
	buffer.write("*----HEADER----*\n");
	
	buffer.write(String.format("#PLAYER 1\n")); //TODO Add player x support
	buffer.write(String.format("#TITLE %s\n",chart.getTitle()));
	buffer.write(String.format("#ARTIST %s\n",chart.getArtist()));
	buffer.write(String.format("#GENRE %s\n",chart.getGenre()));
	buffer.write(String.format("#PLAYLEVEL %d\n",chart.getLevel()));
	buffer.write(String.format("#BPM %.2f\n",chart.getBPM()));
	buffer.write(String.format("#LNTYPE 1\n"));
	
	buffer.newLine();
	
	buffer.write("*----WAV LIST----*\n");
	for(Entry<Integer, String> entry : chart.getSampleIndex().entrySet())
	{
	    buffer.write(String.format("#WAV%s %s\n", toBase36(entry.getKey() + 1), entry.getValue()));
	}
	buffer.newLine();
        
        bpmChanges = new HashMap<Event, Integer>();
        int i = 1;
        for(Event e : EventHelper.getEventsFromThisChannel(event_list, Channel.BPM_CHANGE))
            bpmChanges.put(e, i++);

        buffer.write("*----BPM LIST----*\n");
        
        for(Entry<Event, Integer> entry : bpmChanges.entrySet()) {
            buffer.write(String.format("#BPM%s %f\n", toBase36(entry.getValue()), entry.getKey().getValue()));
        }    
        buffer.newLine();
    }
    
    private static void writeEvents(BufferedWriter buffer, Chart chart) throws IOException
    {
	buffer.write("*----EVENTS----*\n");
	
	Iterator<Entry<Integer, List<Event>>> measure_iterator = 
		EventHelper.getEventsPerMeasure(event_list).entrySet().iterator();
	
	while(measure_iterator.hasNext()) {
	    Entry<Integer, List<Event>> measure = measure_iterator.next();
	    String measure_start = String.format("#%03d", measure.getKey());
	    
	    Iterator<Entry<Channel, List<Event>>> channel_iterator =
		    EventHelper.getEventsPerChannel(measure.getValue()).entrySet().iterator();
	    
	    while(channel_iterator.hasNext()) {
		Entry<Channel, List<Event>> channel = channel_iterator.next();
		String channel_start = String.format("%02d", getChannel(channel.getKey()));

		int step = MAX_POSITIONS;
		String[] values = new String[MAX_POSITIONS];
		for(Event e : channel.getValue()) {
		    int p = (int) (MAX_POSITIONS * e.getPosition());
		    step = gcd(step, p);
                    if(channel.getKey().equals(Channel.BPM_CHANGE)) {
                        if(bpmChanges.containsKey(e))
                            values[p] = toBase36(bpmChanges.get(e));
                    }
		    else if(channel.getKey().equals(Channel.TIME_SIGNATURE))
			values[p] = Double.toString(e.getValue());
		    else
                        values[p] = toBase36((int)e.getValue() + 1);
		}
		
		String v = "";
		for(int i = 0; i < values.length; i+=step) {
		    if(values[i] == null)
			v += "00";
		    else
			v += values[i];
		}
		
		buffer.write(measure_start+channel_start+":"+v);
		buffer.newLine();
	    }
	    
	    buffer.newLine();
	}
    }

    private static int getChannel(Event.Channel c) {
	switch(c) {
	    case AUTO_PLAY: return 1;
	    case TIME_SIGNATURE: return 2;
	    case BPM_CHANGE: return 8;
	    case NOTE_1: return 11;
	    case NOTE_2: return 12;
	    case NOTE_3: return 13;
	    case NOTE_4: return 14;
	    case NOTE_5: return 15;
	    case NOTE_6: return 18;
	    case NOTE_7: return 19;
		
	    case NOTE_SC: return 16;
		
	    default: return 0;
	}
    }
    
    public static int gcd(int p, int q) {
	if (q == 0) {
	    return p;
	}
	return gcd(q, p % q);
    }
    
    private static String toBase36(int i) {
	String key = Integer.toString(i, 36).toUpperCase();
	if(key.length() < 2) 
	    key = "0" + key;
	return key;
    }
}

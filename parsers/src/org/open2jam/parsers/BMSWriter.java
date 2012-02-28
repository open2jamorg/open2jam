package org.open2jam.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import org.open2jam.parsers.Event.Channel;
import org.open2jam.parsers.utils.AudioData;

/**
 *
 * @author CdK
 */
public class BMSWriter {
    
    private static final int MAX_POSITIONS = 192;
    
    public static void export(ChartList list, String path) throws IOException
    {
	for(Chart c : list) {
	    export(c, path);
	}
    }
    
    public static void export(Chart chart, String path) throws IOException
    {
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
	for(Entry<Integer, AudioData> entry : chart.getSamples().entrySet())
	{
	    buffer.write(String.format("#WAV%s %s\n", toBase36(entry.getKey() + 1), entry.getValue().filename));
	}
	
	buffer.newLine();
    }
    
    private static void writeEvents(BufferedWriter buffer, Chart chart) throws IOException
    {
	buffer.write("*----EVENTS----*\n");
	
	for(Entry<Integer, List<Event>> measures : Chart.getEventsPerMeasure(chart.getEvents()).entrySet()) {
	    String measure_start = String.format("#%03d", measures.getKey());
	    
	    for(Entry<Channel, List<Event>> channels : Chart.getEventsPerChannel(measures.getValue()).entrySet()) {
		String channel = String.format("%02d", getChannel(channels.getKey()));
		
		int step = MAX_POSITIONS;
		String[] values = new String[MAX_POSITIONS];
		for(Event e : channels.getValue()) {
		    int p = (int) (MAX_POSITIONS * e.getPosition());
		    step = gcd(step, p);
		    values[p] = toBase36((int)e.getValue() + 1);
		}
		
		String v = "";
		for(int i = 0; i < values.length; i+=step) {
		    if(values[i] == null)
			v += "00";
		    else
			v += values[i];
		}
		buffer.write(measure_start+channel+":"+v);
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

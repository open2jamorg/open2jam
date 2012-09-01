package org.open2jam.parsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import org.open2jam.parsers.Event.Channel;
import org.open2jam.parsers.Event.Flag;

/**
 *
 * @author CdK
 */
public class BMSWriter {
    
    private static final int MAX_POSITIONS = 192;
    
    private static final Locale locale = Locale.US; //TODO: any fix for this?
    
    private static EventList event_list;
    private static HashMap<Event, Integer> bpmChanges;
    
    //Empty value will be ZZ
    private static final int emptyValue = 1295;
    
    private static char sampleStart = 0;
    
    private static class BMSLine {
	int step;
	int channel;
	String[] values;

	public BMSLine(int channel) {
	    this.channel = channel;
	    
	    this.step = MAX_POSITIONS;
	    this.values = new String[MAX_POSITIONS];
	}
	
	public void add(Event e, int p) {
	    //We can have longnotes into the autoplay channel because bms can't have longnotes on it xD
	    //Flags NONE and HOLD are able to make sounds so we can have these
	    if(channel == getChannel(Channel.AUTO_PLAY) && e.getFlag() == Flag.RELEASE)
		return;
	    
	    double value = e.getValue();
	    
	    step = gcd(step, p);
	    
	    if(channel == getChannel(Event.Channel.BPM_CHANGE)) {
		if(bpmChanges.containsKey(e))
		    values[p] = toBase36(bpmChanges.get(e));
	    }
	    else if(channel == getChannel(Event.Channel.TIME_SIGNATURE))
		values[p] = Double.toString(value);
	    else if(channel == getChannel(Event.Channel.BGA))
		values[p] = toBase36((int)value);  
	    else {
		//A 00 value is nothing in bms files, use the emptyValue (ZZ) TODO Find a better fix XD
		value = value > 0 ? e.getValue() + sampleStart : emptyValue;
		values[p] = toBase36((int)value);
	    }
	    
	}
	
	public String get(int i) {
	    return values[i];
	}
	
	public void write(BufferedWriter buffer, String measure_start) throws IOException {
		String channel_start = String.format("%02d", channel);
		
                String line = "";
                for(int i = 0; i < MAX_POSITIONS; i+=step) {
                    if(values[i] == null)
                        line += "00";
                    else
                        line += values[i];
		}
		buffer.write(measure_start+channel_start+":"+line);
		buffer.newLine();
	}
	
	public boolean isEmpty() {
	    for(int i = 0; i < MAX_POSITIONS; i++) {
		if(values[i] != null)
		    return false;
	    }
	    return true;
	}
	
    }
    
    public static void export(ChartList list, String path) throws IOException
    {
	for(Chart c : list) {
	    export(c, path);
	}
    }
    
    public static void export(Chart chart, String path) throws IOException
    {
	event_list = chart.getEvents();
	//fix possible broken longnotes
	event_list.fixEventList(EventList.FixMethod.OPEN2JAM, false);
	Collections.sort(event_list);

	String dirName = (chart.getArtist()+" - "+chart.getTitle()).replaceAll("/", " ");
	String name = chart.getSource().getName();
	name = name.substring(0, name.lastIndexOf("."))+"_"+chart.level+".bms";
	File dir = new File(path+File.separator+dirName);
	File file = new File(dir, name);
	dir.mkdirs();
	file.createNewFile();
	System.out.println(file.getAbsolutePath());
	
	BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
        
	makeHeader(buffer, chart);
	writeEvents(buffer, chart);
	
	buffer.close();
	
	if(chart.hasCover()) {
	    String cover_name = chart.getCoverName();
	    if(cover_name == null) cover_name = "NAME_NOT_FOUND";
	    File image = new File(dir, cover_name+".png");
	    if(!image.exists())
		ImageIO.write(chart.getCover(), "png", image);
	}
	
	chart.copySampleFiles(dir);
	chart.copyBgaFiles(dir);
    }
    
    private static void makeHeader(BufferedWriter buffer, Chart chart) throws IOException
    {
	buffer.write("*----HEADER----*");
	buffer.newLine();
	
	buffer.write(String.format("#PLAYER 1")); //TODO Add player x support
	buffer.newLine();
	buffer.write(String.format("#TITLE %s",chart.getTitle()));
	buffer.newLine();
	buffer.write(String.format("#ARTIST %s",chart.getArtist()));
	buffer.newLine();
	buffer.write(String.format("#GENRE %s",chart.getGenre()));
	buffer.newLine();
	buffer.write(String.format("#PLAYLEVEL %d",chart.getLevel()));
	buffer.newLine();
	buffer.write(String.format(locale,"#BPM %.2f",chart.getBPM()));
	buffer.newLine();
	if(chart.hasCover()) {
	    String cover_name = chart.getCoverName();
	    if(cover_name == null) cover_name = "NAME_NOT_FOUND";
	    buffer.write(String.format("#STAGEFILE %s", cover_name+".png"));
	    buffer.newLine();
	}
	buffer.write(String.format("#LNTYPE 1"));
	buffer.newLine();
	
	buffer.newLine();
	
	Map<Integer, String> sampleIndex = chart.getSampleIndex();
	
	//There is 00, in bms is nothing, move everything 1 step
	if(sampleIndex.containsKey(0)) 
	    sampleStart = 1;
	
	buffer.write("*----WAV LIST----*");
	buffer.newLine();
	for(Entry<Integer, String> entry : sampleIndex.entrySet())
	{
	    int value = entry.getKey() + sampleStart;
	    buffer.write(String.format("#WAV%s %s", toBase36(value), entry.getValue()));
	    buffer.newLine();
	}
	buffer.newLine();
	
	Map<Integer, String> imageIndex = chart.getBgaIndex();
	
	if(!imageIndex.isEmpty()) {
	    buffer.write("*----BMP LIST----*");
	    buffer.newLine();
	    for(Entry<Integer, String> entry : imageIndex.entrySet())
	    {
		int value = entry.getKey();
		buffer.write(String.format("#BMP%s %s", toBase36(value), entry.getValue()));
		buffer.newLine();
	    }
	    buffer.newLine();
	}
        
        bpmChanges = new HashMap<Event, Integer>();
        int i = 1;
        for(Event e : event_list.getEventsFromThisChannel(Channel.BPM_CHANGE))
            bpmChanges.put(e, i++);

	if(!bpmChanges.isEmpty()) {
	    buffer.write("*----BPM LIST----*");
	    buffer.newLine();

	    for(Entry<Event, Integer> entry : bpmChanges.entrySet()) {
		buffer.write(String.format(locale,"#BPM%s %f", toBase36(entry.getValue()), entry.getKey().getValue()));
		buffer.newLine();
	    }    
	}
        buffer.newLine();
    }
    
    private static void writeEvents(BufferedWriter buffer, Chart chart) throws IOException
    {
	buffer.write("*----EVENTS----*");
	buffer.newLine();
	
	Iterator<Entry<Integer, EventList>> measure_iterator = 
		event_list.getEventsPerMeasure().entrySet().iterator();
	
	while(measure_iterator.hasNext()) {
	    Entry<Integer, EventList> measure = measure_iterator.next();
	    String measure_start = String.format("#%03d", measure.getKey());
	    
	    Iterator<Entry<Channel, EventList>> channel_iterator =
		    measure.getValue().getEventsPerChannel().entrySet().iterator();
	    
	    while(channel_iterator.hasNext()) {
		Entry<Channel, EventList> channel = channel_iterator.next();
		int chan = getChannel(channel.getKey());
		if(chan == 0) continue;
		
		ArrayList<BMSLine> lines = new ArrayList<BMSLine>();
		
		int lastPosition = MAX_POSITIONS;
		BMSLine line;
		line = new BMSLine(chan);
		for(Event e : channel.getValue()) {
		    int c = chan;
		    if(c > 10 && e.getFlag() != Flag.NONE) {
			c += 40;
		    }
		    int p = (int) Math.round(MAX_POSITIONS * e.getPosition());
		    //we need another line if the channel isn't the same or the position of 2 events overlaps
		    if(line.channel != c || lastPosition == p) {
			if(!line.isEmpty() && !lines.contains(line))
			    lines.add(line);
			boolean needNewLine = true;
			//we will try to find an used line with an empty position
			for(BMSLine l : lines) {
			    if(l.get(p) == null && l.channel == c) {
				line = l;
				needNewLine = false;
				break;
			    }
			}
			if(needNewLine) {
			    line = new BMSLine(c);
			    lines.add(line);
			}
		    }
		    lastPosition = p;
		    line.add(e, p);
		}
		
		if(!lines.contains(line) && !line.isEmpty())
		    lines.add(line);
		
		for(BMSLine bl : lines)
		    bl.write(buffer, measure_start);
	    }
	    
	    buffer.newLine();
	}
    }

    private static int getChannel(Event.Channel c) {
	switch(c) {
	    case AUTO_PLAY: return 1;
	    case TIME_SIGNATURE: return 2;
	    case BGA: return 4;
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
    
    public static int gcd(int x, int y) {
	if (y == 0) return x;
	return gcd(y, x % y);
    }
    
    private static String toBase36(int i) {
	String key = Integer.toString(i, 36).toUpperCase();
	if(key.length() < 2) 
	    key = "0" + key;
	return key;
    }
}

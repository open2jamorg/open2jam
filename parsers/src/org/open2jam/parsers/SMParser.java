package org.open2jam.parsers;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.open2jam.parsers.utils.CharsetDetector;
import org.open2jam.parsers.utils.Filters;
import org.open2jam.parsers.utils.Logger;
import org.open2jam.parsers.utils.SampleData;


class SMParser
{
    public static Pattern key_value = Pattern.compile("(,|;)?( *(\\d+\\.\\d+) *= *(\\d+\\.\\d+) *)?(,|;)?");
    public static Pattern note_line = Pattern.compile("^(,|;)?([01234ML]+)?(,|;)?.*$", Pattern.CASE_INSENSITIVE);
      
    public static boolean canRead(File f)
    {
	return f.getName().toLowerCase().endsWith(".sm");
    }

    public static ChartList parseFile(File file)
    {
        ChartList list = new ChartList();
        list.source_file = file;

	try {
	    list = parseSMheader(file);
	} catch (IOException ex) {
	    Logger.global.log(Level.WARNING, "{0}", ex);
	}
	Collections.sort(list);
        if (list.isEmpty()) return null;
        return list;
    }

    private static ChartList parseSMheader(File file) throws IOException
    {
        ChartList list = new ChartList();
        list.source_file = file;

	String charset = CharsetDetector.analyze(file);
        BufferedReader r;
        try{
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
        }catch(FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", file.getName());
            return null;
        }
	
        HashMap<Integer, String> sample_files = new HashMap<Integer, String>();

	String title = "", subtitle = "", artist = "";
	double bpm = 130;
	
	String cover_name = null;
	File image_cover = null;
	
	String line;
        StringTokenizer st;

        try{
        while((line = r.readLine()) != null)
        {
            line = line.trim();
            if(!line.startsWith("#"))continue;
            st = new StringTokenizer(line, ":;");

            String cmd = st.nextToken().toUpperCase();
	    
            try{
                if(cmd.equals("#TITLE")){
		    title = st.nextToken().trim();
                    continue;
                }
		if(cmd.equals("#SUBTITLE")){
		    subtitle = st.nextToken().trim();
		    continue;
		}
                if(cmd.equals("#ARTIST")){
		    artist = st.nextToken().trim();
		    continue;
                }
		if(cmd.equals("#BPMS")){ //first bpm, others bpms will be readed when the parse of the events
		    StringTokenizer sb = new StringTokenizer(st.nextToken().trim(), "=,");
		    if(Double.parseDouble(sb.nextToken().trim()) == 0)
			bpm = Double.parseDouble(sb.nextToken().trim());
		    continue;
		}
                if(cmd.equals("#BANNER")){
		    File cover = new File(file.getParent(), st.nextToken().trim());
		    if(cover.exists()) {
			cover_name = cover.getName();
			image_cover = cover;			
		    } else {
			String target = cover.getName();
			int idx = target.lastIndexOf('.');
			if(idx > 0) {
			    target = target.substring(0, idx);
			}
			for(File ff : file.getParentFile().listFiles(Filters.imageFilter)) {
			    String s = ff.getName();
			    idx = s.lastIndexOf('.');
			    if (idx > 0) {
				s = s.substring(0, idx);
			    }
			    if (target.equalsIgnoreCase(s)) {
				cover_name = ff.getName();
				image_cover = ff;
				break;
			    }
			}
		    }
                }
                if(cmd.startsWith("#MUSIC")){
		    int id = 1;
		    String name = st.nextToken().trim();
		    sample_files.put(id, name);
		    continue;
                }
		if(cmd.startsWith("#NOTES")){
		    SMChart chart = new SMChart();
		    chart.source = file;
		    chart.title = title+" "+subtitle;
		    chart.artist = artist;
		    chart.bpm = bpm;
		    chart.cover_name = cover_name;
		    chart.image_cover = image_cover;
		    chart.sample_index = sample_files;
		    
		    for(int i = 0; i<5;i++)
		    {
			String s;
			if((s = r.readLine()) != null) {
			    s = s.replace(":", "").trim();
			    switch(i)
			    {
				case 0:
				    chart.keys = getKeys(s);
				break;
				case 3:
				    chart.level = Integer.parseInt(s);
				break;
			    }
			}
		    }
		    
		    list.add(chart);
		    continue;
		}
		
            }catch(NoSuchElementException ignored){}
             catch(NumberFormatException e){ 
                 Logger.global.log(Level.WARNING, "unparsable number @ {0} on file {1}", new Object[]{cmd, file.getName()});
             }
        }
        }catch(IOException e){
            Logger.global.log(Level.WARNING, "IO exception on file parsing ! {0}", e.getMessage());
        }
	
        return list;
    }

    public static EventList parseChart(SMChart chart)
    {
        BufferedReader r;
        try{
            r = new BufferedReader(new FileReader(chart.source));
        }catch(FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", chart.source);
            return null;
        }

	EventList event_list = new EventList();	
	
	String line;
	StringTokenizer st;
	boolean founded = false;
	boolean parsed = false;
	int startMeasure = 0;
	double offset = 0;
        try {
            while ((line = r.readLine()) != null && !parsed) {
		line = line.trim();
		if(!line.startsWith("#")) continue;
		st = new StringTokenizer(line, ":;");
		
		String cmd = st.nextToken().toUpperCase().trim();
		
                if(cmd.equals("#OFFSET")){
		    offset = Double.parseDouble(st.nextToken().trim()) * 1000d;
//		    if(offset < 0) startMeasure = 1;
		    continue;
                }
		
		if(cmd.equals("#BPMS")){
		    if(!st.hasMoreTokens()) continue;
		    
		    r.mark(8192);
		    StringTokenizer sb = new StringTokenizer(st.nextToken().trim(), "=,");

		    setBPM(sb, event_list); //same line bpm
		    
		    //now, other lines bpm
		    String s;
		    while((s = r.readLine()) != null && !s.trim().startsWith("#"))
		    {
			s = s.trim();
			if(s.endsWith(";")) s = s.replace(";", "").trim();
			if(s.isEmpty()) continue;
			sb = new StringTokenizer(s, "=,");
			
			setBPM(sb, event_list); //same line bpm
		    }
		    r.reset();
		    continue;
		}
		
		if(cmd.equals("#STOPS")){
		    if(!st.hasMoreTokens()) continue;
		    
		    r.mark(8192);
		    StringTokenizer sb = new StringTokenizer(st.nextToken().trim(), "=,");

		    setStop(sb, event_list); //same line bpm
		    
		    //now, other lines stops
		    String s;
		    while((s = r.readLine()) != null && !s.trim().startsWith("#"))
		    {
			s = s.trim();
			if(s.endsWith(";")) s = s.replace(";", "").trim();
			if(s.isEmpty()) continue;
			sb = new StringTokenizer(s, "=,");
			
			setStop(sb, event_list); //same line bpm
		    }
		    r.reset();
		    continue;
		}
		
                if(cmd.startsWith("#NOTES"))
		{
		    String s;
		    for(int i = 0; i<5;i++)
		    {	
			if((s = r.readLine()) != null) {
			    s = s.replace(":", "").trim();
			    if(i == 3 && chart.level == Integer.parseInt(s)) {
				founded = true;
			    }
			}
		    }

		    if(!founded) continue;
		    int measure = startMeasure;
		    List<String> notes = new ArrayList<String>();
		    while((s = r.readLine()) != null)
		    {
			s = s.trim().toUpperCase();
			if(s.isEmpty()) continue;
			
			Matcher match = note_line.matcher(s);
			
			if(match.find()) {
			    String front = match.group(1);
			    String nline = match.group(2);
			    String tail = match.group(3);

			    if(front != null) {
				//It's a , or a ; dump the events
				if(!notes.isEmpty()) {
				    fillEvents(event_list, notes, measure);
				}
				measure++;
				if(front.equals(";")) {
				    //line start with a ; end of parsing
				    parsed = true;
				    break;
				}
			    }

			    //add line if any
			    if(nline != null) {
				notes.add(nline);
			    }

			    if(tail != null) {
				//It's a , or a ; dump the events
				if(!notes.isEmpty()) {
				    fillEvents(event_list, notes, measure);
				}
				measure++;
				if(tail.equals(";")) {
				    //line end with a ; end of parsing
				    parsed = true;
				    break;
				}
			    }
			}
		    }
		}
            }
        } catch (IOException ex) {
            Logger.global.log(Level.SEVERE, "{0}", ex);
        } catch(NoSuchElementException ignored) {}
	
	//add the music
	event_list.add(new Event(Event.Channel.AUTO_PLAY, startMeasure, 0, 1, offset, Event.Flag.NONE));
	
        Collections.sort(event_list);
        return event_list;
    }

    public static HashMap<Integer, SampleData> getSamples(SMChart chart)
    {
	HashMap<Integer, SampleData> samples = new HashMap<Integer, SampleData>();
	
	File[] files = chart.source.getParentFile().listFiles(Filters.sampleFilter);
	
	for(Map.Entry<Integer, String> entry : chart.sample_index.entrySet()) {
	    try {
		for(File f : files) {
		    String sn = entry.getValue().toLowerCase();
		    String fn = f.getName().toLowerCase();
		    String ext = fn.substring(fn.lastIndexOf("."), fn.length());
		    sn = sn.substring(0, sn.lastIndexOf("."));
		    fn = fn.substring(0,fn.lastIndexOf("."));

		    if(sn.equals(fn)) {
			SampleData.Type t;
			if      (ext.equals(".wav")) t = SampleData.Type.WAV;
			else if (ext.equals(".ogg")) t = SampleData.Type.OGG;
			else if (ext.equals(".mp3")) t = SampleData.Type.MP3;
			else { //not a music file so continue
			    continue;
			}
			samples.put(entry.getKey(), new SampleData(new FileInputStream(f), t, f.getName()));
		    }
		}
	    } catch (IOException ex) {
		Logger.global.log(Level.SEVERE, "{0}", ex);
	    }
	}
	return samples;
    }
    
    private static void fillEvents(EventList event_list, List<String> notes, int measure)
    {
	int size = notes.size();
	for(int pos=0; pos<size; pos++)
	{
	    String[] n = notes.get(pos).split("(?<=\\G.)");
	    double position = (double)pos/size;
	    for(int i=0; i<n.length;i++)
	    {
		if(n[i].equals("0")) continue;

		Event.Flag flag;
		if(n[i].equals("1")) 
		    flag = Event.Flag.NONE;
		else if(n[i].equals("2"))
		    flag = Event.Flag.HOLD;
		else if(n[i].equals("3"))
		    flag = Event.Flag.RELEASE;
		else if(n[i].equals("4"))
		    flag = Event.Flag.ROLL;
		else if(n[i].equals("M"))
		    flag = Event.Flag.MINE;
		else if(n[i].equals("L"))
		    flag = Event.Flag.LIFT;
		else {
		    Logger.global.log(Level.WARNING, "{0} not supported :/", n[i]);
		    continue;
		}
		
		event_list.add(new Event(getChannel(i), measure, position, 0, flag));
	    }
	}
	notes.clear();	
    }
    
    private static void setStop(StringTokenizer sb, EventList event_list)
    {
	while(sb.hasMoreTokens())
	{
	    double beat = Double.parseDouble(sb.nextToken().trim());
	    double stop = Double.parseDouble(sb.nextToken().trim()) * 1000;
	    double measure = beat/4;
	    double position = Math.abs(((int)measure)-measure);
	    event_list.add(new Event(Event.Channel.STOP, (int)measure, position, stop, Event.Flag.NONE));
	}
    }
    
    private static void setBPM(StringTokenizer sb, EventList event_list)
    {
	while(sb.hasMoreTokens())
	{
	    double beat = Double.parseDouble(sb.nextToken().trim());
	    double bpm = Double.parseDouble(sb.nextToken().trim());
	    double measure = beat/4;
	    double position = Math.abs(((int)measure)-measure);
	    event_list.add(new Event(Event.Channel.BPM_CHANGE, (int)measure, position, bpm, Event.Flag.NONE));
	}
    }
       
    private static int getKeys(String s)
    {
	s = s.toLowerCase();
	if(s.equals("dance-single")) 
	    return 4;
	if(s.equals("pump-single") || s.equals("ez2-single")  || s.equals("para-single"))
	    return 5;
	if(s.equals("dance-solo"))
	    return 6;
	if(s.equals("ez2-real"))
	    return 7;
	if(s.equals("dance-double") || s.equals("dance-couple"))
	    return 8;
	if(s.equals("pump-double") || s.equals("pump-couple")  || s.equals("ez2-double"))
	    return 10;
	
	Logger.global.log(Level.WARNING, "Trying to get the key numbers from '{0}' is not supported", s);
	return 0;
    }
    
    private static Event.Channel getChannel(int i)
    {
	switch(i)
	{
	    default: return Event.Channel.NONE;
	    case 0: return Event.Channel.NOTE_1;
	    case 1: return Event.Channel.NOTE_2;
	    case 2: return Event.Channel.NOTE_3;
	    case 3: return Event.Channel.NOTE_4;
	    case 4: return Event.Channel.NOTE_5;
	    case 5: return Event.Channel.NOTE_6;
	    case 6: return Event.Channel.NOTE_7;
	    case 7: return Event.Channel.NOTE_8;
	    case 8: return Event.Channel.NOTE_9;
	    case 9: return Event.Channel.NOTE_10;
	}
    }
}

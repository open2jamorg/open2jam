package org.open2jam.parser;

import java.util.logging.Level;
import org.open2jam.util.OggInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.open2jam.util.Logger;
import org.open2jam.render.lwjgl.SoundManager;

class SMParser
{

    private static final FileFilter sm_filter = new FileFilter(){
        public boolean accept(File f){
            String s = f.getName().toLowerCase();
            return (!f.isDirectory()) && (s.endsWith(".sm"));
        }
    };

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

        BufferedReader r;
        try{
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        }catch(FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", file.getName());
            return null;
        }
	
        HashMap<String, Integer> sample_files = new HashMap<String, Integer>();

	String title = "", subtitle = "", artist = "";
	double bpm = 130;
	
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
                    image_cover = new File(file.getParent(),st.nextToken().trim());
                    if(!image_cover.exists())
                    {
                        String target = image_cover.getName();
                        int idx = target.lastIndexOf('.');
                        if(idx > 0)target = target.substring(0, idx);
                        for(File ff : file.getParentFile().listFiles())
                        {
                            String s = ff.getName();
                            idx = s.lastIndexOf('.');
                            if(idx > 0)s = s.substring(0, idx);
                            if(target.equalsIgnoreCase(s)){
                                image_cover = ff;
                                break;
                            }
                        }
			if(!image_cover.exists()) image_cover = null;
                    }
                }
                if(cmd.startsWith("#MUSIC")){
		    int id = 1;
		    String name = st.nextToken().trim();
		    int idx = name.lastIndexOf('.');
		    if(idx > 0)name = name.substring(0, idx);
		    sample_files.put(name, id);
		    continue;
                }
		if(cmd.startsWith("#NOTES")){
		    SMChart chart = new SMChart();
		    chart.source = file;
		    chart.title = title+" "+subtitle;
		    chart.artist = artist;
		    chart.bpm = bpm;
		    chart.image_cover = image_cover;
		    chart.sample_files = sample_files;
		    
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

    public static List<Event> parseChart(SMChart chart)
    {
        BufferedReader r;
        try{
            r = new BufferedReader(new FileReader(chart.source));
        }catch(FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", chart.source);
            return null;
        }

	ArrayList<Event> event_list = new ArrayList<Event>();	
	
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
		    offset = Double.parseDouble(st.nextToken().trim());
		    if(offset < 0) startMeasure = 1;
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
			
			if(!s.startsWith(",") && !parsed)
			{
			    if(s.contains(";")) {
				//we have finished
				s = s.replace(";", "").trim();
				parsed = true;
				if(s.isEmpty()) continue;
			    }
			    notes.add(s);
			}
			else
			{ //new measure, time to fill the events
			    int size = notes.size();
			    for(int pos=0; pos<size; pos++)
			    {
				String[] n = notes.get(pos).split("(?<=\\G.)");
				double position = (double)pos/size;
				for(int i=0; i<n.length;i++)
				{
				    if(n[i].equals("0")) continue;
			    
				    if(n[i].equals("1")) 
					event_list.add(new Event(getChannel(i), measure, position, 0, Event.Flag.NONE));
				    else if(n[i].equals("2"))
					event_list.add(new Event(getChannel(i), measure, position, 0, Event.Flag.HOLD));
				    else if(n[i].equals("3"))
					event_list.add(new Event(getChannel(i), measure, position, 0, Event.Flag.RELEASE));
				    else if(n[i].equals("4"))
					Logger.global.log(Level.WARNING, "Roll not supported :/");
				    else if(n[i].equals("M"))
					Logger.global.log(Level.WARNING, "Mines not supported :/");
				    else if(n[i].equals("L"))
					Logger.global.log(Level.WARNING, "Lift not supported :/");
				    else
					Logger.global.log(Level.WARNING, "{0} not supported :/", n[i]);
				}
			    }
			    notes.clear();
			    measure++;
			    continue;
			}
		    }	    
		}   
            }
        } catch (IOException ex) {
            Logger.global.log(Level.SEVERE, null, ex);
        } catch(NoSuchElementException ignored) {}
	
	//add the music
	event_list.add(new Event(Event.Channel.AUTO_PLAY, 0, 0, 1, Event.Flag.NONE));
	
        Collections.sort(event_list);
        return event_list;
    }

    public static HashMap<Integer,Integer> loadSamples(SMChart h)
    {
        HashMap<Integer,Integer> samples = new HashMap<Integer,Integer>();
        for(File f : h.source.getParentFile().listFiles())
        {
            try {
                String s = f.getName();
                String st = s;
                int idx = s.lastIndexOf('.');
                if (idx > 0) {
                    s = s.substring(0, idx);
                    st = st.substring(idx).toLowerCase();
                }
                Integer id = h.sample_files.get(s);
                if (id == null) {
                    continue;
                }
                int buffer = 0;
                if      (st.equals(".wav")) buffer = SoundManager.newBuffer(f.toURI().toURL());
                else if (st.equals(".ogg")) buffer = SoundManager.newBuffer(new OggInputStream(new FileInputStream(f)));
                else if (st.equals(".mp3")) Logger.global.log(Level.WARNING, "File {0} not supported", f.getName());
                samples.put(id, buffer);
            } catch (IOException ex) {
                Logger.global.log(Level.SEVERE, null, ex);
            }
        }
        return samples;
    }
    
    private static void setStop(StringTokenizer sb, ArrayList<Event> event_list)
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
    
    private static void setBPM(StringTokenizer sb, ArrayList<Event> event_list)
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

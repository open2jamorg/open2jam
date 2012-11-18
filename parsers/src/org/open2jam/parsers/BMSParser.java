package org.open2jam.parsers;

import java.io.*;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.open2jam.parsers.utils.CharsetDetector;
import org.open2jam.parsers.utils.Filters;
import org.open2jam.parsers.utils.Logger;
import org.open2jam.parsers.utils.SampleData;


class BMSParser
{

    private static final Pattern note_line = Pattern.compile("^#(\\d\\d\\d)(\\d\\d):(.+)$");
    private static final Pattern bpm_line = Pattern.compile("^#BPM(\\w\\w)\\s+(.+)$");
    private static final Pattern stop_line = Pattern.compile("^#STOP(\\w\\w)\\s+(.+)$");
    
    private static final FileFilter bms_filter = new FileFilter(){
        public boolean accept(File f){
            String s = f.getName().toLowerCase();
            return (!f.isDirectory()) && (s.endsWith(".bms") || s.endsWith(".bme") || 
                    s.endsWith(".bml") || s.endsWith(".pms"));
        }
    };

    public static boolean canRead(File f)
    {
        if(!f.isDirectory())return false;

        File[] bms = f.listFiles(bms_filter);
        return bms.length > 0;
    }

    public static ChartList parseFile(File file)
    {
        ChartList list = new ChartList();
        list.source_file = file;

        File[] bms_files = file.listFiles(bms_filter);

        for (File bms_file : bms_files) {
            try {
                BMSChart chart = parseBMSHeader(bms_file);
                if (chart != null) list.add(chart);
            } catch (Exception e) {
                Logger.global.log(Level.WARNING, "{0}", e);
            }
        }
        Collections.sort(list);
        if (list.isEmpty()) return null;
        return list;
    }

    private static BMSChart parseBMSHeader(File f) throws IOException
    {
        String charset = CharsetDetector.analyze(f);

        BMSChart chart = new BMSChart();
        chart.source = f;
        BufferedReader r;
        try{
            r = new BufferedReader(new InputStreamReader(new FileInputStream(f),charset));
        }catch(FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", f.getName());
            return null;
        }catch(UnsupportedEncodingException e2){
            Logger.global.log(Level.WARNING, "Encoding [{0}] not supported !", charset);
            r = new BufferedReader(new FileReader(f));
        }
        
        String line;
        StringTokenizer st;
        chart.sample_index = new HashMap<Integer, String>();

        int max_key = 0, max_measure = 0, total_notes = 0, scratch_notes = 0;

        try{
        while((line = r.readLine()) != null)
        {
            line = line.trim();
            if(!line.startsWith("#"))continue;
            st = new StringTokenizer(line);

            String cmd = st.nextToken().toUpperCase();

            try{
                if(cmd.equals("#PLAYLEVEL")){
                        chart.level = Integer.parseInt(st.nextToken());
                        continue;
                }
                if(cmd.equals("#RANK")){
                        //int rank = Integer.parseInt(st.nextToken());
                        continue;
                }
                if(cmd.equals("#TITLE")){
                        chart.title = st.nextToken("").trim();
                        continue;
                }
                if(cmd.equals("#ARTIST")){
                        chart.artist = st.nextToken("").trim();
                        continue;
                }
                if(cmd.equals("#GENRE")){
                        chart.genre = st.nextToken("").trim();
                        continue;
                }
                if(cmd.equals("#PLAYER")){ //TODO Add player x support
                        int player = Integer.parseInt(st.nextToken());
                        if(player != 1)
                        {
                            Logger.global.log(Level.WARNING, "#PLAYER{0} not supported @ {1}",new Object[] {player, f.getName()});
                            return null;
                        }
                        continue;
                }
                if(cmd.equals("#BPM")){
                        chart.bpm = Double.parseDouble(st.nextToken());
                        continue;
                }
                if(cmd.equals("#LNTYPE")){
                        chart.lntype = Integer.parseInt(st.nextToken());
                        continue;
                }
                if(cmd.equals("#LNOBJ")){
                        chart.lnobj = Integer.parseInt(st.nextToken(), 36);
                }
                if(cmd.equals("#STAGEFILE")){
		    chart.image_cover = null;
		    File cover = new File(f.getParent(), st.nextToken("").trim());
		    if(cover.exists()) {
			chart.cover_name = cover.getName();
			chart.image_cover = cover;			
		    } else {
			String target = cover.getName();
			int idx = target.lastIndexOf('.');
			if(idx > 0) {
			    target = target.substring(0, idx);
			}
			for(File ff : chart.source.getParentFile().listFiles(Filters.imageFilter)) {
			    String s = ff.getName();
			    idx = s.lastIndexOf('.');
			    if (idx > 0) {
				s = s.substring(0, idx);
			    }
			    if (target.equalsIgnoreCase(s)) {
				chart.cover_name = ff.getName();
				chart.image_cover = ff;
				break;
			    }
			}
		    }
                }
                if(cmd.startsWith("#WAV")){
                        int id = Integer.parseInt(cmd.replaceFirst("#WAV",""), 36);
                        String name = st.nextToken("").trim();
                        chart.sample_index.put(id, name);
                        continue;
                }
		if(cmd.startsWith("#BMP")){
			int id = Integer.parseInt(cmd.replaceFirst("#BMP",""), 36);
                        String name = st.nextToken("").trim();
                        chart.bga_index.put(id, name);
                        continue;
		}
                Matcher note_match = note_line.matcher(cmd);
                if(note_match.find()){
                    int measure = Integer.parseInt(note_match.group(1));
                    int channel = Integer.parseInt(note_match.group(2));

                    if(channel > 50)channel -= 40;
                    if(channel > max_key)max_key = channel;
                    if(measure >= max_measure) max_measure = measure;

                    switch(channel){
                        case 16:case 11:case 12:case 13:
                        case 14:case 15:case 18:case 19:
                            String[] notes = note_match.group(3).split("(?<=\\G.{2})");
                            for(String n : notes){
                                if(!n.equals("00")){
                                    total_notes++;
                                    if(channel == 16)scratch_notes++;
                                }
                            }
                    }
                }

            }catch(NoSuchElementException ignored){}
             catch(NumberFormatException e){ 
                 Logger.global.log(Level.WARNING, "unparsable number @ {0} on file {1}", new Object[]{cmd, f.getName()});
             }
        }
        }catch(IOException e){
            Logger.global.log(Level.WARNING, "IO exception on file parsing ! {0}", e.getMessage());
        }

        chart.notes = total_notes;
        chart.duration = (int) Math.round((240 * max_measure)/chart.bpm);
        if(max_key == 18 && scratch_notes > 0){
            chart.o2mania_style = true;
        } else {
            chart.o2mania_style = false;
            chart.notes -= scratch_notes;
        }

        switch(max_key)
        {
            case 15:
            case 16:
                chart.keys = 5;
                break;
            case 18: // o2mania
            case 19:
                chart.keys = 7;
                break;
            case 25:
            case 26:
                chart.keys = 10;
                break;
            case 27:
            case 28:
                chart.keys = 14;
                break;
            default:
                Logger.global.log(Level.WARNING, "Unknown key number {0} on file {1}", new Object[]{max_key, f.getName()});
        }
        return chart;
    }

    public static EventList parseChart(BMSChart chart)
    {
        EventList event_list = new EventList();
        BufferedReader r;
        String line;
        try{
            r = new BufferedReader(new FileReader(chart.source));
        }catch(FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", chart.source);
            return null;
        }

        HashMap<Integer, Double> bpm_map = new HashMap<Integer, Double>();
	HashMap<Integer, Integer> stop_map = new HashMap<Integer, Integer>();
        HashMap<Integer, Boolean> ln_buffer = new HashMap<Integer, Boolean>();
        HashMap<Integer, Event> lnobj_buffer = new HashMap<Integer, Event>();
	
	//This will help us to sort the lines by measure
	Map<Integer, List<String>> lines = new TreeMap<Integer, List<String>>();
	
	try {
	    while ((line = r.readLine()) != null) {
		line = line.trim().toUpperCase();
		if (!line.startsWith("#")) {
		    continue;
		}

		Matcher matcher = note_line.matcher(line);
		if (!matcher.find()) {
		    if (line.startsWith("#BPM")) {
			Matcher bpm_match = bpm_line.matcher(line);
			if (bpm_match.find()) {
			    int code = Integer.parseInt(bpm_match.group(1), 36);
			    double value = Double.parseDouble(bpm_match.group(2).replace(",", "."));
			    bpm_map.put(code, value);
			}
		    } else if (line.startsWith(("#STOP"))) {
			Matcher stop_match = stop_line.matcher(line);
			if (stop_match.find()) {
			    int code = Integer.parseInt(stop_match.group(1), 36);
			    int value = Integer.parseInt(stop_match.group(2));
			    stop_map.put(code, value);
			}
		    }

		    continue;
		}

		int measure = Integer.parseInt(matcher.group(1));
		//Let's add the line
		if (!lines.containsKey(measure)) {
		    lines.put(measure, new ArrayList<String>());
		}
		lines.get(measure).add(line);
	    }
	} catch (IOException ex) {
	    Logger.global.log(Level.SEVERE, "{0}", ex);
	}

	Iterator<List<String>> it = lines.values().iterator();
	//now iterate by all the lines and add the events
	while(it.hasNext()) {
	    List<String> lstr = it.next();
	    for(String l : lstr) {
		Matcher matcher = note_line.matcher(l);
		if(!matcher.find()) continue;
		int measure = Integer.parseInt(matcher.group(1));
		int channel = Integer.parseInt(matcher.group(2));
		if (channel == 2) {
		    // time signature
		    double value = Double.parseDouble(matcher.group(3).replace(",", "."));
		    event_list.add(new Event(Event.Channel.TIME_SIGNATURE, measure, 0, value, Event.Flag.NONE));
		    continue;
		}
		String[] events = matcher.group(3).split("(?<=\\G.{2})");
		
		if (channel == 3) { //INLINE BPM CHANGE
		    for (int i = 0; i < events.length; i++) {
			int value = Integer.parseInt(events[i], 16);
			if (value == 0) {
			    continue;
			}
			double p = ((double) i) / events.length;
			event_list.add(new Event(Event.Channel.BPM_CHANGE, measure, p, value, Event.Flag.NONE));
		    }
		    continue;
		} else if (channel == 8) { //BPM TAG BPM CHANGE
		    for (int i = 0; i < events.length; i++) {
			if (events[i].equals("00")) {
			    continue;
			}
			double value = bpm_map.get(Integer.parseInt(events[i], 36));
			double p = ((double) i) / events.length;
			event_list.add(new Event(Event.Channel.BPM_CHANGE, measure, p, value, Event.Flag.NONE));
		    }
		    continue;
		} else if (channel == 4) { //BGA DATA
		    for (int i = 0; i < events.length; i++) {
			int value = Integer.parseInt(events[i], 36);
			if (value == 0) {
			    continue;
			}
			double p = ((double) i) / events.length;
			event_list.add(new Event(Event.Channel.BGA, measure, p, value, Event.Flag.NONE));
		    }
		    continue;
		} else if (channel == 9) { //STOP DATA
		    for (int i = 0; i < events.length; i++) {
			if (events[i].equals("00")) {
			    continue;
			}
			double value = stop_map.get(Integer.parseInt(events[i], 36));
			double p = ((double) i) / events.length;
			event_list.add(new Event(Event.Channel.STOP, measure, p, value, Event.Flag.NONE));
		    }
		    continue;
		}
		Event.Channel ec;
		if (chart.o2mania_style) {
		    switch (channel)
		    {
			// normal notes
			case 16: channel = 11; break;
			case 11: channel = 12; break;
			case 12: channel = 13; break;
			case 13: channel = 14; break;
			case 14: channel = 15; break;
			case 15: channel = 18; break;
			case 18: channel = 19; break;
			// long notes
			case 56: channel = 51; break;
			case 51: channel = 52; break;
			case 52: channel = 53; break;
			case 53: channel = 54; break;
			case 54: channel = 55; break;
			case 55: channel = 58; break;
			case 58: channel = 59; break;
		    }
		}
		switch (channel)
		{
		    case 1:
			ec = Event.Channel.AUTO_PLAY;
			break;
		    case 11:
		    case 51:
			ec = Event.Channel.NOTE_1;
			break;
		    case 12:
		    case 52:
			ec = Event.Channel.NOTE_2;
			break;
		    case 13:
		    case 53:
			ec = Event.Channel.NOTE_3;
			break;
		    case 14:
		    case 54:
			ec = Event.Channel.NOTE_4;
			break;
		    case 15:
		    case 55:
			ec = Event.Channel.NOTE_5;
			break;
		    case 18:
		    case 58:
			ec = Event.Channel.NOTE_6;
			break;
		    case 19:
		    case 59:
			ec = Event.Channel.NOTE_7;
			break;
		    case 16:
		    case 56:
			ec = Event.Channel.NOTE_SC;
			break;
		    default:
			continue;
		}
		for (int i = 0; i < events.length; i++) {
		    int value = Integer.parseInt(events[i], 36);
		    double p = ((double) i) / events.length;

		    if (channel > 50) {
			Boolean b = ln_buffer.get(channel);
			if (b != null && b) {
			    if (chart.lntype == 2) {
				if (value == 0) {
				    event_list.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
				    ln_buffer.put(channel, false);
				}
			    } else {
				if (value > 0) {
				    event_list.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
				    ln_buffer.put(channel, false);
				}
			    }
			} else {
			    if (value > 0) {
				ln_buffer.put(channel, true);
				event_list.add(new Event(ec, measure, p, value, Event.Flag.HOLD));
			    }
			}
		    } else {
			if (value == 0) {
			    continue;
			}
			Event e = new Event(ec, measure, p, value, Event.Flag.NONE);
			if(chart.lnobj != 0){
			    if(value == chart.lnobj){
				e.flag = Event.Flag.RELEASE;
				lnobj_buffer.get(channel).flag = Event.Flag.HOLD;
				lnobj_buffer.put(channel, null);
			    }else{
				lnobj_buffer.put(channel, e);
			    }
			}
			event_list.add(e);
		    }
		} 
	    }
	}

        Collections.sort(event_list);
        return event_list;
    }

    public static HashMap<Integer, SampleData> getSamples(BMSChart chart)
    {
	HashMap<Integer, SampleData> samples = new HashMap<Integer, SampleData>();
	
	List<File> files = Arrays.asList(chart.source.getParentFile().listFiles(Filters.sampleFilter));
	
	Iterator<Entry<Integer, String>> it_samples = chart.sample_index.entrySet().iterator();
	while(it_samples.hasNext()) {
	    Entry<Integer, String> entry = it_samples.next();
	    try {
		Iterator<File> it_files = files.iterator(); 
		while(it_files.hasNext()) {
		    File f = it_files.next();
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
			break;
		    }
		}
	    } catch (IOException ex) {
		Logger.global.log(Level.SEVERE, "{0}", ex);
	    }
	}
	return samples;
    }

    static Map<Integer, File> getImages(BMSChart chart) {
	HashMap<Integer, File> images = new HashMap<Integer, File>();
	
	List<File> files = Arrays.asList(chart.source.getParentFile().listFiles(Filters.imageFilter));
	
	Iterator<Entry<Integer, String>> it_images = chart.bga_index.entrySet().iterator();
	while(it_images.hasNext()) {
	    Entry<Integer, String> entry = it_images.next();
	    
	    Iterator<File> it_files = files.iterator(); 
	    while(it_files.hasNext()) {
		File f = it_files.next();
		String in = entry.getValue().toLowerCase();
		String fn = f.getName().toLowerCase();
		String ext = fn.substring(fn.lastIndexOf("."), fn.length());
		in = in.substring(0, in.lastIndexOf("."));
		fn = fn.substring(0,fn.lastIndexOf("."));
		if(in.equals(fn)) {
		    images.put(entry.getKey(), f);
		    break;
		}
	    }
	}
	return images;
    }
    
    static boolean hasVideo(BMSChart chart) {

	List<File> files = Arrays.asList(chart.source.getParentFile().listFiles(Filters.videoFilter));
	
	Iterator<Entry<Integer, String>> it_images = chart.bga_index.entrySet().iterator();
	while(it_images.hasNext()) {
	    Entry<Integer, String> entry = it_images.next();
	    
	    Iterator<File> it_files = files.iterator(); 
	    while(it_files.hasNext()) {
		File f = it_files.next();
		String in = entry.getValue().toLowerCase();
		String fn = f.getName().toLowerCase();
		String ext = fn.substring(fn.lastIndexOf("."), fn.length());
		in = in.substring(0, in.lastIndexOf("."));
		fn = fn.substring(0,fn.lastIndexOf("."));
		if(in.equals(fn)) {
		    chart.video = f;
		    return true;
		}
	    }
	}
	
	return false;
    }
}

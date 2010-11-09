package org.open2jam.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.NoSuchElementException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.open2jam.Util;
import org.open2jam.render.lwjgl.SoundManager;

public class BMSParser
{
	private static final FileFilter bms_filter = new FileFilter(){
		public boolean accept(File f){
			String s = f.getName().toLowerCase();
			return (!f.isDirectory()) && (s.endsWith(".bms") || s.endsWith(".bme"));
		}
	};

	public static boolean canRead(File f)
	{
		if(!f.isDirectory())return false;

		File[] bms = f.listFiles(bms_filter);
		return bms.length > 0;
	}

	public static BMSChart parseFile(File file)
	{
		BMSChart chart = new BMSChart();

		chart.source = file;
		chart.bms = file.listFiles(bms_filter);
		chart.level = new int[chart.bms.length];
		chart.bpm = new int[chart.bms.length];
		
		chart.rank_map = new HashMap<Integer, Integer>();
		for(int i=0;i<chart.bms.length;i++)
		{
			try{
				Integer rank = parseBMSHeader(chart.bms[i], chart, i);
				if(rank == null)rank = 3;
				chart.rank_map.put(3-rank, i);
			}catch(UnsupportedOperationException e){continue;}
		}
		return chart;
	}

	private static Integer parseBMSHeader(File f, BMSChart chart, int idx) throws BadFileException
	{
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(f));
		}catch(FileNotFoundException e){Util.warn(e);return null;}

		int playlevel = 0;
		Integer rank = null;
		int player = 1;
		int bpm = 130;
		int lntype = 0;
		String title = null, artist = null, genre = null;
		String line = null;
		StringTokenizer st = null;
		Map<Integer,File> sample_files = new HashMap<Integer, File>();
		try{
		while((line = r.readLine()) != null)
		{
			line = line.trim();
			if(!line.startsWith("#"))continue;
			st = new StringTokenizer(line);

			String cmd = st.nextToken().toUpperCase();

			try{
				if(cmd.equals("#PLAYLEVEL")){
					playlevel = Integer.parseInt(st.nextToken());
					continue;
				}
				if(cmd.equals("#RANK")){
					rank = Integer.parseInt(st.nextToken());
					continue;
				}
				if(cmd.equals("#TITLE")){
					title = st.nextToken("").trim();
					continue;
				}
				if(cmd.equals("#ARTIST")){
					artist = st.nextToken("").trim();
					continue;
				}
				if(cmd.equals("#GENRE")){
					genre = st.nextToken("").trim();
					continue;
				}
				if(cmd.equals("#PLAYER")){
					player = Integer.parseInt(st.nextToken());
                                        if(player != 1)throw new UnsupportedOperationException("Not supported yet.");
					continue;
				}
				if(cmd.equals("#BPM")){
					bpm = Integer.parseInt(st.nextToken());
					continue;
				}
				if(cmd.equals("#LNTYPE")){
					lntype = Integer.parseInt(st.nextToken());
					continue;
				}
				if(cmd.equals("#LNOBJ")){
					throw new UnsupportedOperationException("LNOBJ Not supported yet.");
				}
                                if(cmd.equals("#STAGEFILE")){
                                        chart.image_cover = new File(f.getParent(),st.nextToken("").trim());
				}
				if(cmd.startsWith("#WAV")){
					int id = Integer.parseInt(cmd.replaceFirst("#WAV",""), 36);
					sample_files.put(id, new File(f.getParent(),st.nextToken("").trim()));
					continue;
				}
			}catch(NoSuchElementException e){}
			 catch(NumberFormatException e){ throw new BadFileException("unparsable number @ "+cmd); }
		}
		}catch(IOException e){Util.log(e);}

		chart.level[idx] = playlevel;
		chart.title = title;
		chart.artist = artist;
		chart.genre = genre;
		chart.bpm[idx] = bpm;
		chart.sample_files = sample_files;
		chart.lntype = lntype;
		return rank;
	}

	public static List<Event> parseChart(BMSChart chart, int rank)
	{
                ArrayList<Event> event_list = new ArrayList<Event>();
		BufferedReader r = null;
		String line = null;
		try{
			r = new BufferedReader(new FileReader(chart.bms[rank]));
		}catch(FileNotFoundException e){Util.log(e);}

		HashMap<Integer, Double> bpm_map = new HashMap<Integer, Double>();
		HashMap<Integer, Boolean> ln_buffer = new HashMap<Integer, Boolean>();

		Pattern note_line = Pattern.compile("^#(\\d\\d\\d)(\\d\\d):(.+)$");
		Pattern bpm_line = Pattern.compile("^#BPM(\\w\\w)\\s+(.+)$");
		try{
		while((line = r.readLine()) != null)
		{
			line = line.trim();
			if(!line.startsWith("#"))continue;
			Matcher matcher = note_line.matcher(line);

			if(!matcher.find()){
				Matcher bpm_match = bpm_line.matcher(line);
				if(bpm_match.find()){
					int code = Integer.parseInt(bpm_match.group(1),36);
					double value = Double.parseDouble(bpm_match.group(2));
					bpm_map.put(code, value);
				}
				continue;
			}

			int measure = Integer.parseInt(matcher.group(1));
			int channel = Integer.parseInt(matcher.group(2));
			
			if(channel == 2){ // time signature
				double value = Double.parseDouble(matcher.group(3));
				event_list.add(new Event(Event.Channel.TIME_SIGNATURE,measure,0,value,Event.Flag.NONE));
				continue;
			}

			String[] events = matcher.group(3).split("(?<=\\G.{2})");

			if(channel == 3) // bpm change
			{
				for(int i=0;i<events.length;i++){
					int value = Integer.parseInt(events[i], 16);
					if(value == 0)continue;
					double p = ((double)i)/events.length;
					event_list.add(new Event(Event.Channel.BPM_CHANGE,measure,p,value,Event.Flag.NONE));
				}
				continue;
			}
			else if(channel == 8) // bpm change extended
			{
				for(int i=0;i<events.length;i++){
					if(events[i].equals("00"))continue;
					double value = bpm_map.get(Integer.parseInt(events[i], 36));
					double p = ((double)i)/events.length;
					event_list.add(new Event(Event.Channel.BPM_CHANGE,measure,p,value,Event.Flag.NONE));
				}
				continue;
			}
			
			Event.Channel ec;
			switch(channel) {
				case 1:ec = Event.Channel.AUTO_PLAY;break;
				case 11:case 51:ec = Event.Channel.NOTE_1;break;
				case 12:case 52:ec = Event.Channel.NOTE_2;break;
				case 13:case 53:ec = Event.Channel.NOTE_3;break;
				case 14:case 54:ec = Event.Channel.NOTE_4;break;
				case 15:case 55:ec = Event.Channel.NOTE_5;break;
				case 18:case 58:ec = Event.Channel.NOTE_6;break;
				case 19:case 59:ec = Event.Channel.NOTE_7;break;
				default:continue;
			}
			for(int i=0;i<events.length;i++) {
				int value = Integer.parseInt(events[i],36);
				double p = ((double)i)/events.length;
				if(channel > 50){
					Boolean b = ln_buffer.get(channel);
					if(b != null && b == true){
						if(chart.lntype == 2){
							if(value == 0){
							event_list.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
							ln_buffer.put(channel, false);
                                                    }
						}else{
							if(value > 0){
							event_list.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
							ln_buffer.put(channel, false);
                                                    }
						}
					}
					else{
						if(value > 0){
							ln_buffer.put(channel, true);
							event_list.add(new Event(ec, measure, p, value, Event.Flag.HOLD));
						}
					}
				}
				else{
					if(value == 0)continue;
					event_list.add(new Event(ec, measure, p, value, Event.Flag.NONE));
				}
			}
		}
		}catch(Exception e){Util.log(e);}
		return event_list;
	}

	public static HashMap<Integer,Integer> loadSamples(BMSChart h, int rank)
	{
		HashMap<Integer,Integer> samples = new HashMap<Integer,Integer>();
		for(Map.Entry<Integer,File> entry : h.sample_files.entrySet())
		{
			try{
				int id = SoundManager.newBuffer(new OggInputStream(new FileInputStream(entry.getValue())));
				samples.put(entry.getKey(), id);
			}catch(Exception e){Util.log(e);}
		}
		return samples;
	}
}
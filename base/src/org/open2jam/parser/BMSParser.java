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
import java.util.HashMap;
import java.util.Map;
import org.open2jam.Util;
import org.open2jam.render.lwjgl.SoundManager;

public class BMSParser
{

	private static FileFilter bms_filter = new FileFilter(){
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

	public static BMSHeader parseFileHeader(File file)
	{
		BMSHeader header = new BMSHeader();

		header.source = file;
		header.bms = file.listFiles(bms_filter);
		header.level = new int[header.bms.length];
		header.bpm = new int[header.bms.length];
		
		Integer[] ranks = new Integer[header.bms.length];
		int got = 0;
		for(int i=0;i<header.bms.length;i++)
		{
			try{
				Integer rank = parseBMSHeader(header.bms[i], header, i);
				ranks[got] = rank;
				got++;
			}catch(UnsupportedOperationException e){continue;}
		}
		return header;
	}

	private static Integer parseBMSHeader(File f, BMSHeader header, int idx) throws BadFileException
	{
		BufferedReader r = null;
		try{
			r = new BufferedReader(new FileReader(f));
		}catch(FileNotFoundException e){}

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
				if(cmd.startsWith("#WAV")){
					int id = Integer.parseInt(cmd.replaceFirst("#WAV",""), 36);
					sample_files.put(id, new File(f.getParent(),st.nextToken("").trim()));
					continue;
				}
			}catch(NoSuchElementException e){}
			 catch(NumberFormatException e){ throw new BadFileException("unparsable number @ "+cmd); }
		}
		}catch(java.io.IOException e){e.printStackTrace();}

		if(player != 1)throw new UnsupportedOperationException("Not supported yet.");

		header.level[idx] = playlevel;
		header.title = title;
		header.artist = artist;
		header.genre = genre;
		header.bpm[idx] = bpm;
		header.sample_files = sample_files;
		header.lntype = lntype;
		return rank;
	}

	public static Chart parseFile(BMSHeader header, int rank)
	{
		Chart chart = new Chart(header, rank);
		BufferedReader r = null;
		String line = null;
		try{
			r = new BufferedReader(new FileReader(header.bms[rank]));
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
				chart.add(new Event(Event.Channel.TIME_SIGNATURE,measure,0,value,Event.Flag.NONE));
				continue;
			}

			String[] events = matcher.group(3).split("(?<=\\G.{2})");

			if(channel == 3) // bpm change
			{
				for(int i=0;i<events.length;i++){
					int value = Integer.parseInt(events[i], 16);
					if(value == 0)continue;
					double p = ((double)i)/events.length;
					chart.add(new Event(Event.Channel.BPM_CHANGE,measure,p,value,Event.Flag.NONE));
				}
				continue;
			}
			else if(channel == 8) // bpm change extended
			{
				for(int i=0;i<events.length;i++){
					if(events[i].equals("00"))continue;
					double value = bpm_map.get(Integer.parseInt(events[i], 36));
					double p = ((double)i)/events.length;
					chart.add(new Event(Event.Channel.BPM_CHANGE,measure,p,value,Event.Flag.NONE));
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
						if(header.lntype == 2){
							if(value == 0)
							chart.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
							ln_buffer.put(channel, false);
						}else{
							if(value > 0)
							chart.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
							ln_buffer.put(channel, false);
						}
					}
					else{
						if(value > 0){
							ln_buffer.put(channel, true);
							chart.add(new Event(ec, measure, p, value, Event.Flag.HOLD));
						}
					}
				}
				else{
					if(value == 0)continue;
					chart.add(new Event(ec, measure, p, value, Event.Flag.NONE));
				}
			}
		}
		}catch(Exception e){Util.log(e);}
		return chart;
	}

	public static HashMap<Integer,Integer> loadSamples(BMSHeader h, int rank)
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

	public static void main(String[] args)
	{
		BMSHeader a = (BMSHeader)parseFileHeader(new File(args[0]));
		parseFile(a, Integer.parseInt(args[1]));
	}
}
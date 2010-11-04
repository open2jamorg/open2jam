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
import java.util.HashMap;
import java.util.Map;

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

	public static ChartHeader parseFileHeader(File file)
	{
		BMSHeader header = new BMSHeader();

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
		String title = null, artist = null, genre = null;
		String line = null;
		StringTokenizer st = null;
		Map<Integer,String> sample_names = new HashMap<Integer, String>();
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
				if(cmd.startsWith("#WAV")){
					int id = Integer.parseInt(cmd.replaceFirst("#WAV",""), 36);
					sample_names.put(id, st.nextToken("").trim());
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
		header.sample_names = sample_names;
		return rank;
	}

	public static Chart parseFile(BMSHeader header, int rank)
	{
		Chart chart = new Chart(header, rank);
		BufferedReader r = null;
		String line = null;
		try{
			r = new BufferedReader(new FileReader(header.bms[rank]));
		}catch(FileNotFoundException e){}

		Pattern p = Pattern.compile("^#(\\d\\d\\d)(\\d\\d):(\\w*)$");
		try{
		while((line = r.readLine()) != null)
		{
			line = line.trim();
			if(!line.startsWith("#"))continue;
			Matcher matcher = p.matcher(line);
			
			if(matcher.find()){
				System.out.print("m:["+matcher.group(1)+"],c:["+matcher.group(2)+"]");
				String[] k = matcher.group(3).split("(?<=\\G.{2})");
			}
		}
		}catch(java.io.IOException e){e.printStackTrace();}
		return null;
	}

	public static HashMap<Integer,Integer> loadSamples(BMSHeader h, int rank)
	{
		return null;
	}

	public static void main(String[] args)
	{
		BMSHeader a = (BMSHeader)parseFileHeader(new File(args[0]));
		parseFile(a, Integer.parseInt(args[1]));
	}
}
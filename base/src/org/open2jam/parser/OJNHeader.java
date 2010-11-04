package org.open2jam.parser;

import java.io.File;
import java.util.Map;

public class OJNHeader implements ChartHeader
{
	/** the internal format of this header */
	public ChartParser.Formats getSourceType() { return ChartParser.Formats.OJN; }

	/** full path to the source file of this header */
	protected File source;
	public File getSource() { return source; }

	/** an integer representing difficulty.
	*** this is the internal difficult level of the song
	*** for that rank **/
	protected short[] level;
	public int getLevel(int rank){ return level[rank]; }

	public int getMaxRank(){ return 2; }

	protected String title;
	public String getTitle() { return title; }

	protected String artist;
	public String getArtist() { return artist; }

	protected String genre;
	public String getGenre() { return genre; }

	protected String noter;
	public String getNoter(){ return noter; }

	protected String sample_file;
	public Map<Integer,Integer> getSamples(int rank){ return OJMParser.parseFile(sample_file); }

	/** the bpm as specified is the header */
	protected double bpm;
	public double getBPM(int rank) { return bpm; }

	/** the number of notes in the song */
	protected int[] note_count;
	public int getNoteCount(int rank) { return note_count[rank]; }

	/** the duration in seconds */
	protected int[] duration;
	public int getDuration(int rank) { return duration[rank]; }

	/** a image cover, representing the song */
	protected java.awt.Image cover;
	public java.awt.Image getCover() { return cover; }


	/******* OJN specific fields *******/

	protected int note_offsets[];
	public int[] getNoteOffsets() { return note_offsets; }
}
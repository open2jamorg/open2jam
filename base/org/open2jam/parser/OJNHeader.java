package org.open2jam.parser;


public class OJNHeader implements ChartHeader
{
	/** the rank of the song.
	*** 0 - easy, 1 - normal, 2 - hard */
	protected int rank;
	public int getRank() { return rank; }

	/** the internal format of this header */
	public ChartParser.Formats getSourceType() { return ChartParser.Formats.OJN; }

	/** full path to the source file of this header */
	protected String source_file;
	public String getSourceFile() { return source_file; }

	/** an integer representing difficulty.
	*** this is the internal difficult level of the song
	*** for that rank **/
	protected int level;
	public int getLevel(){ return level; }

	protected String title;
	public String getTitle() { return title; }

	protected String artist;
	public String getArtist() { return artist; }

	protected String genre;
	public String getGenre() { return genre; }

	/** the bpm as specified is the header */
	protected double bpm;
	public double getBPM() { return bpm; }

	/** the number of notes in the song */
	protected int note_count;
	public int getNoteCount() { return note_count; }

	/** the duration in seconds */
	protected int duration;
	public int getDuration() { return duration; }

	/** a image cover, representing the song */
	protected java.awt.Image cover;
	public java.awt.Image getCover() { return cover; }


	/******* OJN specific fields *******/

	protected int note_offsets[];
	public int[] getNoteOffsets() { return note_offsets; }
}
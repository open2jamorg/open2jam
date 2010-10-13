package org.open2jam.parser;


/** this encapsulates only the header of a file */ 
public class ChartHeader
{
	/** the rank of the song.
	*** 0 being the easiest, 1 normal, etc..
	*** there is no defined upper bound for this */
	protected int rank;

	/** a string representing the original file source type.
	*** 'OJN', 'BMS', etc */
	protected String source_type;

	/** full path to the source file of this header */
	protected String source_file;

	/** an integer representing difficulty.
	*** we _should_ have some standard here
	*** maybe we could use o2jam as the default
	*** and normalize the others to this rule
	**/
	public int level;

	public String title;
	public String artist;
	public String genre;

	/** a bpm representing the whole song.
	*** doesn't need to be exact, just for info */
	public double bpm;

	/** the number of notes in the song */
	public int noteCount;

	/** the duration in seconds */
	public int duration;

	/** a image cover, representing the song */
	public java.awt.Image cover;


	public ChartHeader(String file, int rank, String source_type)
	{
		this.source_file = file;
		this.rank = rank;
		this.source_type = source_type;
	}

	public String getSourceType() { return source_type; }
	public String getSourceFile() { return source_file; }
	public int getRank() { return rank; }
}
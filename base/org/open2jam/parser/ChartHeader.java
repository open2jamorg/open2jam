package org.open2jam.parser;


/** this encapsulates only the header of a file */ 
public interface ChartHeader
{
	/** the rank of the song.
	*** 0 being the easiest, 1 normal, etc..
	*** there is no defined upper bound for this */
	public int getRank();

	/** the internal format of this header */
	public ChartParser.Formats getSourceType();

	/** full path to the source file of this header */
	public String getSourceFile();

	/** an integer representing difficulty.
	*** we _should_ have some standard here
	*** maybe we could use o2jam as the default
	*** and normalize the others to this rule
	**/
	public int getLevel();


	public String getTitle();
	public String getArtist();
	public String getGenre();

	/** a bpm representing the whole song.
	*** doesn't need to be exact, just for info */
	public double getBPM();

	/** the number of notes in the song */
	public int getNoteCount();

	/** the duration in seconds */
	public int getDuration();

	/** a image cover, representing the song */
	public java.awt.Image getCover();
}

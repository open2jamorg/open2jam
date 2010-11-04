package org.open2jam.parser;

import java.io.File;
import java.util.Map;
/** this encapsulates only the header of a file.
*** in case there's more than one rank(difficulty)
*** for the song, the rank integer follows this pattern:
*** 0 - easy, 1 - normal, 2 - hard, 3 - very hard, ... 
*** there's no upper bound.
**/ 
public interface ChartHeader
{
	/** the internal format of this header */
	public ChartParser.Formats getSourceType();

	/** the File object to the source file of this header */
	public File getSource();

	/** an integer representing difficulty.
	*** we _should_ have some standard here
	*** maybe we could use o2jam as the default
	*** and normalize the others to this rule
	**/
	public int getLevel(int rank);

	/** returns the maximum rank this chart has.
	** in a way that getLevel(getMaxRank()) will give the hardest version.
	**/
	public int getMaxRank();

	public String getTitle();
	public String getArtist();
	public String getGenre();
	public String getNoter();
	public Map<Integer,Integer> getSamples(int rank);

	/** a bpm representing the whole song.
	*** doesn't need to be exact, just for info */
	public double getBPM(int rank);

	/** the number of notes in the song */
	public int getNoteCount(int rank);

	/** the duration in seconds */
	public int getDuration(int rank);

	/** a image cover, representing the song */
	public java.awt.Image getCover();
}

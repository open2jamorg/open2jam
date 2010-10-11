
/** this encapsulates only the header of a file */ 
public class ChartHeader
{
	/** the number of measures in the chart */
	protected long measures;

	/** the rank of the song.
	*** 0 being the easiest, 1 normal, etc..
	*** there is no upper bound for this */
	protected int rank;

	/** an integer representing difficulty */
	protected int level;

	/** a string representing the original file source type */
	protected String source_type;

	protected String title;
	protected String artist;
	protected String genre;
	protected double bpm;

	/** a image cover, representing the song */
	protected java.awt.Image cover;
}
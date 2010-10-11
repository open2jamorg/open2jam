package org.open2jam.parser;

/** Parsers must implement this interface.
*** the render will use this interface to get the ChartParser
**/
public interface ChartParser
{
	/** parse and returns a ChartHeader object */
	public static ChartHeader parseFileHeader(String file, int rank);

	/** parse and return the whole Chart representation, including header */
	public static Chart parseFile(String file, int rank);
}

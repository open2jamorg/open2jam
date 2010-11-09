package org.open2jam.parser;

import java.io.File;

/** this is the main parser class.
*** it has methods to find out the type of file
*** and delegate the job to the right parser
**/
public class ChartParser
{
	/** this is a static class */
	private ChartParser() {}

	/** parse and returns a ChartHeader object */
	public static Chart parseFile(File file)
	{
		if(OJNParser.canRead(file))return OJNParser.parseFile(file);
		if(BMSParser.canRead(file))return BMSParser.parseFile(file);

		throw new UnsupportedOperationException("File ["+file+"] not supported");
	}
}

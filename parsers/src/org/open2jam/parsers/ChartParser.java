package org.open2jam.parsers;

import java.io.File;

/** this is the main parser class.
*** it has methods to find out the type of file
*** and delegate the job to the right parser
**/
public abstract class ChartParser
{
	/** parse and returns a ChartHeader object */
	public static ChartList parseFile(File file)
	{
	    if(OJNParser.canRead(file))return OJNParser.parseFile(file);
	    if(BMSParser.canRead(file))return BMSParser.parseFile(file);
	    if(SMParser.canRead(file)) return SMParser.parseFile(file);
	    if(SNPParser.canRead(file)) return SNPParser.parseFile(file);
	    return null;
	}
}

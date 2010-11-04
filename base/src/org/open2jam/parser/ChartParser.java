package org.open2jam.parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/** this is the main parser class.
*** it has methods to find out the type of file
*** and delegate the job to the right parser
**/
public class ChartParser
{
	/** this is a static class */
	private ChartParser() {}

	/** the names of the extensions supported.
	*** UNKNOWN is a special one, for when no extension matches.
	**/
	public enum Formats { OJN, BMS, UNKNOWN };

	/** parse and returns a ChartHeader object */
	public static ChartHeader parseFileHeader(File file)
	{
		if(OJNParser.canRead(file))return OJNParser.parseFileHeader(file);
		if(BMSParser.canRead(file))return BMSParser.parseFileHeader(file);

		throw new RuntimeException("File ["+file+"] not supported");
	}

	/** parse and return the whole Chart representation */
	public static Chart parseFile(ChartHeader h, int rank)
	{
		if(h instanceof OJNHeader) {
			return OJNParser.parseFile((OJNHeader)h, rank);
		}
		throw new RuntimeException("File format ["+h.getSourceType()+"] not supported");
	}

	/** given a dir, this method returns supported files under it */
	public static List<File> findFiles(File dir) {
		File[] files = dir.listFiles();
		ArrayList<File> fs = new ArrayList<File>();
		for(File f : files)
		{
			if(OJNParser.canRead(f) || BMSParser.canRead(f))fs.add(f);
		}
		return fs;
	}
}

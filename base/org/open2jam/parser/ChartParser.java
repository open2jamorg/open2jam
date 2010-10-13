package org.open2jam.parser;

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
	public enum Formats { OJN, UNKNOWN };

	private static OJNParser ojn_parser = new OJNParser();

	/** parse and returns a ChartHeader object */
	public static ChartHeader parseFileHeader(String file, int rank)
	{
		String ext_str = file.toUpperCase().substring(file.lastIndexOf('.')+1);
		Formats ext = Formats.UNKNOWN;
		try{
			ext = Formats.valueOf(ext_str);
		}catch(Exception e){}
		switch(ext)
		{
			case OJN: return ojn_parser.parseFileHeader(file,rank);
		}
		throw new RuntimeException("File format ["+ext_str+"] not supported");
	}

	/** parse and return the whole Chart representation */
	public Chart parseFile(ChartHeader h)
	{
		if(h instanceof OJNHeader) {
			return ojn_parser.parseFile((OJNHeader)h);
		}
		throw new RuntimeException("File format ["+h.getSourceType()+"] not supported");
	}
}

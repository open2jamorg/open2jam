package org.open2jam.parser;

import java.util.List;
/** this is the main parser class.
*** it has methods to find out the type of file
*** and delegate the job to the right parser
**/
public class SampleParser
{
	/** this is a static class */
	private SampleParser() {}

	/** the names of the extensions supported.
	*** UNKNOWN is a special one, for when no extension matches.
	**/
	public enum Formats { OJM, UNKNOWN };

	/** parse and returns a list of samples */
	public static List<SampleID> parseFile(String file)
	{
		String ext_str = file.toUpperCase().substring(file.lastIndexOf('.')+1);
		Formats ext = Formats.UNKNOWN;
		try{
			ext = Formats.valueOf(ext_str);
		}catch(Exception e){}
		switch(ext)
		{
			case OJM: return OJMParser.parseFile(file);
		}
		throw new RuntimeException("File format ["+ext_str+"] not supported");
	}
}

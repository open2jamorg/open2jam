package org.open2jam.parser;

import java.util.List;
import java.util.ArrayList;

/** an abstract representation of the music chart.
*** all parsers should be able to convert the notes to the Chart format, 
*** the Chart will in turn be used by the render.
***
*** a Chart is just a list of Events ordered by the measure
**/
public class Chart
{
	protected List<Event> events = new ArrayList<Event>();
	int rank;

	/** the header of the source file */
	protected ChartHeader header;

	public Chart(ChartHeader h, int rank)
	{
		this.header = h;
		this.rank = rank;
	}
	
	public void add(Event e)
	{
		events.add(e);
	}

	public List<Event> getEvents() { return events; }

	public ChartHeader getHeader() { return header; }

	public int getRank() { return rank; }
}

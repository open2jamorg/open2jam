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

	/** the header of the source file */
	protected ChartHeader header;

	/** the number of measures in the chart */
	protected long measure_count;

	public Chart(ChartHeader h)
	{
		this.header = h;
	}
	
	public void add(Event e)
	{
		events.add(e);
	}

	/** this method should be called when the parser finishes
	*** adding events to this chart */
	public void finalize()
	{
		java.util.Collections.sort(events); // will sort by measure
		Event e = events.get(events.size()-1); // last event
		measure_count = (int) Math.ceil(e.getMeasure());
	}

	public long getMeasureCount() { return measure_count; }

	public List<Event> getEvents() { return events; }
}

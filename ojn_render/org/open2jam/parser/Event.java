package org.open2jam.parser;

public abstract class Event implements Comparable<Event>
{
	double beat;
	short channel;

	public int compareTo(Event e)
	{
		return (int) (beat - e.beat);
	}
}
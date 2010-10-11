package org.open2jam.parser;

public abstract class Event implements Comparable<Event>
{
	protected double measure;
	protected short channel;

	public int compareTo(Event e)
	{
		return (int) (measure - e.getMeasure());
	}

	public double getMeasure() { return measure; }
	public short getChannel() { return channel; }
}

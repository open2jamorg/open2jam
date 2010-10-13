package org.open2jam.parser;

public abstract class Event implements Comparable<Event>
{
	protected double measure;
	protected int channel;

	public int compareTo(Event e)
	{
		return (int) (measure - e.getMeasure());
	}

	public double getMeasure() { return measure; }
	public int getChannel() { return channel; }
}

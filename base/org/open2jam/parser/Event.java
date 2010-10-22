package org.open2jam.parser;

public class Event implements Comparable<Event>
{
	private int channel;
	private int measure;
	private double position;
	
	private double value;
	private int unk;
	private int type;

	public Event(int channel, int measure, double position,
			double value, int unk, int type) {
		this.channel = channel;
		this.measure = measure;
		this.position = position;
		this.value = value;
		this.unk = unk;
		this.type = type;
	}

	public int compareTo(Event e)
	{
		return (int) ((measure+position) - (e.getMeasure()+e.getPosition()));
	}

	public int getChannel() { return channel; }
	public int getMeasure() { return measure; }
	public double getPosition() { return position; }

	public double getValue() { return value; }
}

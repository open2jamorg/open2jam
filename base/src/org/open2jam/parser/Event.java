package org.open2jam.parser;

public class Event implements Comparable<Event>
{
// 	public enum Channel { };
	public enum Flag { NONE, HOLD, RELEASE };

	private int channel;
	private int measure;
	private double position;
	
	private double value;
	private int unk;
	private Flag flag;

	public Event(int channel, int measure, double position,
			double value, Flag flag) {
		this.channel = channel;
		this.measure = measure;
		this.position = position;
		this.value = value;
		this.flag = flag;
	}

	public int compareTo(Event e)
	{
		return (int) ((measure+position) - (e.getMeasure()+e.getPosition()));
	}

	public int getChannel() { return channel; }
	public int getMeasure() { return measure; }
	public double getPosition() { return position; }
	public Flag getFlag() { return flag; }
	public double getValue() { return value; }
}

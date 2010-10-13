package org.open2jam.parser;

public class NoteEvent extends Event
{
	protected short value;

	public NoteEvent(double measure, short channel, short value)
	{
		this.measure = measure;
		this.channel = channel;
		this.value = value;
	}

	public short getValue() { return value; }
}
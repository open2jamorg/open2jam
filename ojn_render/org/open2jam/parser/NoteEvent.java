package org.open2jam.parser;

public class NoteEvent extends Event
{
	short value;
	char unk;

	public NoteEvent(double beat, short channel, 
			short value, char unk)
	{
		this.beat = beat;
		this.channel = channel;
		this.value = value;
		this.unk = unk;
	}
}
package org.open2jam.parser;

public class LongNoteEvent extends NoteEvent
{
	double end_beat;

	public LongNoteEvent(double beat, short channel, 
			short value, char unk, double end_beat)
	{
		super(beat,channel,value,unk);
		this.end_beat = end_beat;
	}

	public LongNoteEvent(NoteEvent ne, double end_beat)
	{
		super(ne.beat,ne.channel,ne.value,ne.unk);
		this.end_beat = end_beat;
	}
}
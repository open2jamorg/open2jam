package org.open2jam.parser;

public class LongNoteEvent extends NoteEvent
{
	protected double end_beat;

	public LongNoteEvent(double measure, short channel, short value, double end_beat)
	{
		super(measure,channel,value);
		this.end_beat = end_beat;
	}

	public LongNoteEvent(NoteEvent ne, double end_beat)
	{
		super(ne.getMeasure(),ne.getChannel(),ne.getValue());
		this.end_beat = end_beat;
	}
}
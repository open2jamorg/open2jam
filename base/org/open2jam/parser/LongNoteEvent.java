package org.open2jam.parser;

public class LongNoteEvent extends NoteEvent
{
	protected double end_measure;

	public LongNoteEvent(double measure, int channel, short value, double end_measure)
	{
		super(measure,channel,value);
		this.end_measure = end_measure;
	}

	public LongNoteEvent(NoteEvent ne, double end_measure)
	{
		super(ne.getMeasure(),ne.getChannel(),ne.getValue());
		this.end_measure = end_measure;
	}

	public double getEndMeasure() { return end_measure; }
}
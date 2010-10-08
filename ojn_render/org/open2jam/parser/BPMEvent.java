package org.open2jam.parser;

public class BPMEvent extends Event
{
	float bpm;

	public BPMEvent(double beat, short channel, float bpm)
	{
		this.beat = beat;
		this.channel = channel;
		this.bpm = bpm;
	}
}
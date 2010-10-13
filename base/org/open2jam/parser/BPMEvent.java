package org.open2jam.parser;

public class BPMEvent extends Event
{
	protected double bpm;

	public BPMEvent(double measure, short channel, double bpm)
	{
		this.measure = measure;
		this.channel = channel;
		this.bpm = bpm;
	}

	public double getBPM(){ return bpm; }
}
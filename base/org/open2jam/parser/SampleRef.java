package org.open2jam.parser;

public class SampleRef
{
	private int ref;
	private int type;

	public SampleRef( int ref, int type)
	{
		this.ref = ref;
		this.type = type;
	}

	public int getRef(){ return ref; }
	public int getType(){ return type; }
}
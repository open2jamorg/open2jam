package org.open2jam.parser;

public class SampleID
{
	private final int ref;
	private final int buffer_id;

	public SampleID(int ref, int buffer_id)
	{
		this.ref = ref;
		this.buffer_id = buffer_id;
	}

	public int getRef(){ return ref; }
	public int getBuffer(){ return buffer_id; }

	/** we overwrite equals and hashCode.
	** this will make it usable as a HashMap key
	*/
	public boolean equals(Object o)
	{
		if(o == null)return false;
		if(!(o instanceof SampleID))return false;
		SampleID s = (SampleID) o;
		
		return s.getRef() == ref;
	}

	public int hashCode()
	{
		return ref;
	}

	public String toString()
	{
		return "SampleID<"+ref+">";
	}
}
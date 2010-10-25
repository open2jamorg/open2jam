package org.open2jam.parser;

public class SampleID
{
	private final int ref;
	private final int type;
	private final int buffer_id;

	public SampleID(int ref, int type, int buffer_id)
	{
		this.ref = ref;
		this.type = type;
		this.buffer_id = buffer_id;
	}

	public int getRef(){ return ref; }
	public int getType(){ return type; }
	public int getBuffer(){ return buffer_id; }

	/** we overwrite equals and hashCode.
	** this will make it usable as a HashMap key
	*/
	public boolean equals(Object o)
	{
		if(o == null)return false;
		if(!(o instanceof SampleID))return false;
		SampleID s = (SampleID) o;
		
		return s.getRef() == ref && s.getType() == type;
	}

	public int hashCode()
	{
		return 31 + ref + 31 * type;
	}

	public String toString()
	{
		return "ref: "+ref+", type: "+type;
	}
}
package org.open2jam.render;

import java.awt.Rectangle;
/** This class is an abstract representation of a sprite.
** it distinctly defines a texture on a determined image 
** on a file, it can be the whole image or a slice of it
** SpriteID can be used as a hash key
*/
public class SpriteID
{
	/** the file which contains this sprite */
	protected String file;

	/** the slice of the file, 
	** or null if the whole file is to be used */
	protected Rectangle slice;

	public SpriteID(String file)
	{
		this(file,null);
	}
	public SpriteID(String file, Rectangle r)
	{
		if(file == null)throw new NullPointerException("SpriteID file can't be null !");
		this.file = file;
		this.slice = r;
		System.out.println(r.x+", "+r.y+", "+r.width+", "+r.height);
	}

	public String getFile() { return file; }
	public Rectangle getSlice() { return slice; }

	/** we overwrite equals and hashCode.
	** this will make SpriteID usable as a HashMap key
	*/
	public boolean equals(Object o)
	{
		if(o == null)return false;
		if(!(o instanceof SpriteID))return false;
		SpriteID s = (SpriteID) o;
		
		return file.equals(s.getFile()) && (slice == null ? s.getSlice() == null : slice.equals(s.getSlice()));
	}

	public int hashCode()
	{
		int hash = 31 + file.hashCode();
		hash = hash * 31 + (slice == null ? 0 : slice.hashCode());
		return hash;
	}
}

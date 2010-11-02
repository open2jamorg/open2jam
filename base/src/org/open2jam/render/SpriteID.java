package org.open2jam.render;

import java.awt.Rectangle;
import java.net.URL;
/** This class is an abstract representation of a sprite.
** it distinctly defines a texture on a determined image 
** on a file, it can be the whole image or a slice of it
** SpriteID can be used as a hash key
*/
public class SpriteID
{
	/** the url which contains this sprite */
	protected URL url;

	/** the slice of the file, 
	** or null if the whole file is to be used */
	protected Rectangle slice;

	public SpriteID(URL url)
	{
		this(url,null);
	}
	public SpriteID(URL url, Rectangle r)
	{
		if(url == null)throw new NullPointerException("SpriteID url can't be null !");
		this.url = url;
		this.slice = r;
	}

	public URL getURL() { return url; }
	public Rectangle getSlice() { return slice; }

	/** we overwrite equals and hashCode.
	** this will make SpriteID usable as a HashMap key
	*/
	public boolean equals(Object o)
	{
		if(o == null)return false;
		if(!(o instanceof SpriteID))return false;
		SpriteID s = (SpriteID) o;
		
		return url.equals(s.getURL()) && (slice == null ? s.getSlice() == null : slice.equals(s.getSlice()));
	}

	public int hashCode()
	{
		int hash = 31 + url.hashCode();
		hash = hash * 31 + (slice == null ? 0 : slice.hashCode());
		return hash;
	}
}

package org.open2jam.render;

import java.util.ArrayList;
/** SpriteList is a container for sprites.
*** list of sprites with a framespeed attribute.
**/
public class SpriteList extends ArrayList<Sprite>
{
	/** the frame change speed in milliseconds */
	private final double framespeed;

	public SpriteList(double fs)
	{
		super();
		this.framespeed = fs;
	}

	public double getFrameSpeed(){ return framespeed; }
}
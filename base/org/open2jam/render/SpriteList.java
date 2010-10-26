package org.open2jam.render;

/** SpriteList is a container for sprites.
*** list of sprites with a framespeed attribute.
**/
public class SpriteList extends java.util.ArrayList<Sprite>
{
	private int framespeed;

	public SpriteList(int fs)
	{
		super();
		this.framespeed = fs;
	}

	public int getFrameSpeed(){ return framespeed; }
}
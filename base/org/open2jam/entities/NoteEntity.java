package org.open2jam.entities;

import java.awt.geom.Rectangle2D;

import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteID;
import org.open2jam.render.Render;
import org.open2jam.render.ResourceFactory;
import org.open2jam.parser.Chart;

/** a NoteEntity is a animated entity which moves down.
*** it moves according to a Chart */
public class NoteEntity extends AnimatedEntity
{

	public NoteEntity(SpriteID[] refs, double framespeed)
	{
		super(refs,framespeed);
	}

	protected NoteEntity(Sprite[] frames, double framespeed)
	{
		super(frames, 0, 0, framespeed);
	}

	protected Render render;

	public void setRender(Render r)
	{
		this.render = r;
	}

	public void move(long delta)
	{
		setYMove((render.getBPM()/240) * render.getMeasureSize());
		super.move(delta);
	}

	public void judgment()
	{
		alive = false;
	}

	public NoteEntity clone()
	{
		return new NoteEntity(frames, framespeed);
	}
}
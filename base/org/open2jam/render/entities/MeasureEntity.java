package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

public class MeasureEntity extends AnimatedEntity
{
	protected Render render;

	public MeasureEntity(Render r, SpriteList sl, double x, double y)
	{
		super(sl, x, y);
		this.render = r;
	}

	public void move(long delta)
	{
		setYMove(render.getNoteSpeed());
		super.move(delta);
	}

	public void judgment()
	{
		alive = false;
	}
}
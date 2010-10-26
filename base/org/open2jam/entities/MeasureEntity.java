package org.open2jam.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

public class MeasureEntity extends AnimatedEntity
{
	public MeasureEntity(SpriteList sl, double x, double y)
	{
		super(sl, x, y);
	}

	protected Render render;

	public void setRender(Render r)
	{
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
		render.measureEnd();
	}
}
package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

/** a NoteEntity is a animated entity which moves down.
**/
public class NoteEntity extends AnimatedEntity
{
	protected Render render;

	public NoteEntity(Render r, SpriteList sl, double x, double y)
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
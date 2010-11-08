package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

/** a NoteEntity is a animated entity which moves down.
**/
public class NoteEntity extends AnimatedEntity
{
	protected Render render;

        protected int sample_value;

	public NoteEntity(Render r, SpriteList sl, double x, double y, int sample_value)
	{
		super(sl, x, y);
		this.render = r;
                this.sample_value = sample_value;
	}

	public void move(long delta)
	{
		setYMove(render.getNoteSpeed());
		super.move(delta);
	}

	public void judgment()
	{
                render.queueSample(sample_value);
		alive = false;
	}
}
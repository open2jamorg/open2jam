package org.open2jam.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

public class LongNoteEntity extends NoteEntity
{
	protected SpriteList body_frames;

	protected boolean stop_defined = false;

	public LongNoteEntity(SpriteList head_refs, SpriteList body_refs, double x, double y)
	{
		super(head_refs,x,y);
		this.body_frames = body_refs;
		bounds.height = 0;
	}

	public void setHeight(double sh)
	{
		bounds.height = sh;
	}

	public void draw()
	{
		frames.get(nextFrame).draw(bounds.x,bounds.y);

		double p = bounds.y - frames.get(nextFrame).getHeight();

		for(; p > bounds.height+bounds.y; p-=body_frames.get(nextFrame).getHeight())
			body_frames.get(nextFrame).draw(bounds.x,p);

		p -= frames.get(nextFrame).getHeight();
		frames.get(nextFrame).draw(bounds.x,p);
	}
}
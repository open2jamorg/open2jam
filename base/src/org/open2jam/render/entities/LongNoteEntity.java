package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

public class LongNoteEntity extends NoteEntity
{
	protected SpriteList body_frames;

	protected Double end_y = null;

	public LongNoteEntity(Render r, SpriteList head_refs, SpriteList body_refs, double x, double y)
	{
		super(r,head_refs,x,y);
		this.body_frames = body_refs;
		bounds.height = 1000;
	}

	public void setEndY(double ey)
	{
		this.end_y = ey;
		bounds.height = bounds.y - end_y;
	}

    @Override
	public void move(long delta)
	{
		super.move(delta);
		if(end_y != null)end_y += delta * dy;
	}

    @Override
	public void draw()
	{
		frames.get(nextFrame).draw(bounds.x,bounds.y);

		if(end_y == null){
			double p = bounds.y - frames.get(nextFrame).getHeight();
			while(p > -5){
				body_frames.get(nextFrame).draw(bounds.x, p);
				p -= body_frames.get(nextFrame).getHeight();
			}
		}
		else{
			double p = bounds.y - frames.get(nextFrame).getHeight();
			while(p > end_y){
				body_frames.get(nextFrame).draw(bounds.x, p);
				p -= body_frames.get(nextFrame).getHeight();
			}
			frames.get(nextFrame).draw(bounds.x,bounds.y-bounds.height);
		}
	}
}
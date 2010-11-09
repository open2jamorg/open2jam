package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;
import org.open2jam.render.Sprite;

public class LongNoteEntity extends NoteEntity
{
	protected SpriteList body_frames;
	protected Sprite body_sprite;

	protected Double end_y = null;

        boolean played = false;

	public LongNoteEntity(Render r, SpriteList head_frames, SpriteList body_frames, double x, double y, int sample_value)
	{
		super(r,head_frames,x,y,sample_value);
		this.body_frames = body_frames;
		bounds.height = 0;
		body_sprite = body_frames.get(0);
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
		body_sprite = body_frames.get(nextFrame);
		if(end_y != null)end_y += delta * dy;
	}

	@Override
	public void draw()
	{
		sprite.draw(bounds.x,bounds.y);

		double end = (end_y == null) ? -10 : end_y;

		double p = bounds.y - body_sprite.getHeight();
		while(p > end){
			body_sprite.draw(bounds.x, p);
			p -= body_sprite.getHeight();
		}
		sprite.draw(bounds.x,end);
	}

	@Override
    	public void judgment()
	{
            if(!played){
                render.queueSample(sample_value);
                played = true;
            }
            if(end_y != null && end_y > render.getViewPort())alive = false;
	}
}
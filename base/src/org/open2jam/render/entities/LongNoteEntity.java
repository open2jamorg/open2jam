package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

public class LongNoteEntity extends NoteEntity
{
	protected SpriteList body_frames;

	protected Double end_y = null;

        boolean played = false;

	public LongNoteEntity(Render r, SpriteList head_refs, SpriteList body_refs, double x, double y, int sample_value)
	{
		super(r,head_refs,x,y,sample_value);
		this.body_frames = body_refs;
		bounds.height = 0;
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

                double end = (end_y == null) ? -10 : end_y;

                double p = bounds.y - body_frames.get(nextFrame).getHeight();
                while(p > end){
                        body_frames.get(nextFrame).draw(bounds.x, p);
                        p -= body_frames.get(nextFrame).getHeight();
                }
                frames.get(nextFrame).draw(bounds.x,end);
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
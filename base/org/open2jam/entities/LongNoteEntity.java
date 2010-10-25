package org.open2jam.entities;

import java.awt.geom.Rectangle2D;

import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteID;
import org.open2jam.render.Render;
import org.open2jam.render.ResourceFactory;
import org.open2jam.parser.Chart;


public class LongNoteEntity extends NoteEntity
{
	protected Sprite body_frames[];

	protected boolean stop_defined = false;

	public LongNoteEntity(SpriteID head_refs[], SpriteID body_refs[], double framespeed)
	{
		super(head_refs,framespeed);
		this.body_frames = ResourceFactory.get().getSprites(body_refs);
		bounds.height = 0;
	}

	protected LongNoteEntity(Sprite[] head_frames, Sprite[] body_frames, double framespeed)
	{
		super(head_frames,framespeed);
		this.body_frames = body_frames;
		bounds.height = 0;
	}

	public void setHeight(double sh)
	{
		bounds.height = sh;
	}

	public void draw()
	{
		frames[nextFrame].draw((int) Math.round(bounds.x),(int) Math.round(bounds.y));

		double p = bounds.y - frames[nextFrame].getHeight();

		for(; p > bounds.height+bounds.y; p-=body_frames[nextFrame].getHeight())
			body_frames[nextFrame].draw((int) Math.round(bounds.x),(int) Math.round(p));

		p -= frames[nextFrame].getHeight();
		frames[nextFrame].draw((int) Math.round(bounds.x),(int) Math.round(p));
	}

	public LongNoteEntity clone()
	{
		return new LongNoteEntity(frames, body_frames, framespeed);
	}
}
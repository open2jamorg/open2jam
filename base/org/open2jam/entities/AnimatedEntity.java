package org.open2jam.entities;

import java.awt.geom.Rectangle2D;

import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteID;
import org.open2jam.render.Render;
import org.open2jam.render.ResourceFactory;

/** an entity with multiple frames.
*** it loop over frames at a determined framespeed */
public class AnimatedEntity extends Entity
{
	protected Sprite frames[];

	protected int nextFrame;

	protected double sub_frame;
	protected double framespeed;

	protected AnimatedEntity() {}

	public AnimatedEntity(SpriteID refs[], double framespeed)
	{
		this(ResourceFactory.get().getSprites(refs), 0, 0, framespeed);
	}

	public AnimatedEntity(Sprite frames[], double x, double y, double framespeed)
	{
		this.frames = frames;
		this.framespeed = framespeed;
		nextFrame = 0;
		this.sprite = frames[0];

		// assuming all frames have the same dimensions
		bounds = new Rectangle2D.Double(x, y, sprite.getWidth(), sprite.getHeight());
	}

	/**
	 * Request that this entity move itself based on a certain ammount
	 * of time passing. will also change the frame accordingly
	 * 
	 * @param delta The ammount of time that has passed in milliseconds
	 */
	public void move(long delta)
	{
		super.move(delta);
		sub_frame += delta * framespeed/1000;
		int over = (int) sub_frame;
		nextFrame += over;
		sub_frame -= over;
		nextFrame %= frames.length;
		sprite = frames[nextFrame];
	}

	/** the constructor cares about deep copy of frames[] */
	public AnimatedEntity clone()
	{
		return new AnimatedEntity(frames, bounds.x, bounds.y, framespeed);
	}
}

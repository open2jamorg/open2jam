package org.open2jam.entities;

import org.open2jam.render.SpriteID;
import org.open2jam.render.Render;

/** an EffectEntity is an animated entity which animates only once.
 ** after the animation it removes itself from the render 
 */
public class EffectEntity extends AnimatedEntity
{
	protected int lastFrame;

	public EffectEntity(SpriteID refs[], double x, double y, double framespeed)
	{
		super(refs, x, y, framespeed);
		lastFrame = 0;
	}

	public void move(long delta)
	{
		super.move(delta);
		if(nextFrame < lastFrame)alive = false; // we already looped over, now we die
		else lastFrame = nextFrame;
	}
	
	public EffectEntity clone()
	{
		return new EffectEntity(frames_id, bounds.x, bounds.y, framespeed);
	}
}
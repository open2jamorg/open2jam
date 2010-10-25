package org.open2jam.entities;

import org.open2jam.render.SpriteID;
import org.open2jam.render.Sprite;
import org.open2jam.render.Render;
import org.open2jam.render.ResourceFactory;

/** an EffectEntity is an animated entity which animates only once.
 ** after the first loop it declares itself dead, 
 ** which in turn will trigger it's removal from the render
 */
public class EffectEntity extends AnimatedEntity
{
	protected int lastFrame;

	public EffectEntity(SpriteID[] refs, double x, double y, double framespeed)
	{
		this(ResourceFactory.get().getSprites(refs), x, y, framespeed);
	}

	public EffectEntity(Sprite[] sprites, double x, double y, double framespeed)
	{
		super(sprites, x, y, framespeed);
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
		return new EffectEntity(frames, bounds.x, bounds.y, framespeed);
	}
}
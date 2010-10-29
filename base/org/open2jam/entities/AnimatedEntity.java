package org.open2jam.entities;

import org.open2jam.render.SpriteList;

/** an animated entity.
*** it loop over the frames with framespeed 
*** determined by the SpriteList 
*/
public class AnimatedEntity extends Entity
{
	/** the list of frames */
	protected SpriteList frames;

	/** index of the next frame to be rendered */
	protected int nextFrame;

	/** accumulated time delta.
	** when reaches 1 it's time to change frames
	**/
	protected double sub_frame;

	public AnimatedEntity(SpriteList sl)
	{
		this(sl, 0, 0);
	}

	public AnimatedEntity(SpriteList frames, double x, double y)
	{
		super(frames.get(0),x,y);
		this.frames = frames;
		nextFrame = 0;
	}

	/** move the entity and change frame if necessary **/
	public void move(long delta)
	{
		super.move(delta);
		sub_frame += delta * frames.getFrameSpeed();
		int over = (int) sub_frame;
		nextFrame += over;
		sub_frame -= over;
		nextFrame %= frames.size(); // loops over
		sprite = frames.get(nextFrame);
	}
}

package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;

/** an EffectEntity is an animated entity which animates only once.
 ** after the first loop it declares itself dead, 
 ** which in turn will trigger it's removal from the render
 */
public class EffectEntity extends AnimatedEntity
{
    /** keeps the last frame used. */
    protected double last_frame = 0;

    public EffectEntity(SpriteList refs, double x, double y)
    {
        super(refs, x, y);
    }

    protected EffectEntity(EffectEntity org) {
        super(org);
        this.last_frame = org.last_frame;
    }

    @Override
    public void move(long delta)
    {
        super.move(delta);
        if(sub_frame < last_frame)
	    alive = false; // we already looped over, now we die
        else
	    last_frame = sub_frame;
    }

    @Override
    public EffectEntity copy(){
        return new EffectEntity(this);
    }
}
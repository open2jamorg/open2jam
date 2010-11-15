package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;

/** an EffectEntity is an animated entity which animates only once.
 ** after the first loop it declares itself dead, 
 ** which in turn will trigger it's removal from the render
 */
public class EffectEntity extends AnimatedEntity
{
    /** keeps the last frame used. */
    protected int lastFrame;

    public EffectEntity(SpriteList refs, Event.Channel ch, double x, double y)
    {
            super(refs, ch, x, y);
    }

    private EffectEntity(EffectEntity org) {
        super(org);
        this.lastFrame = org.lastFrame;
    }

    public void move(long delta)
    {
            super.move(delta);
            if(nextFrame < lastFrame)alive = false; // we already looped over, now we die
            else lastFrame = nextFrame;
    }

    @Override
    public EffectEntity copy(){
        return new EffectEntity(this);
    }
}
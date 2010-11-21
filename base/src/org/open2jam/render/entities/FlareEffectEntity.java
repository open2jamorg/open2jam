package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;

/**
 *
 * @author fox
 */
public class FlareEffectEntity extends AnimatedEntity
{
    private Entity lne;

    public FlareEffectEntity(SpriteList refs, Event.Channel ec, double x, double y)
    {
            super(refs, ec, x, y);
    }
    
    protected FlareEffectEntity(FlareEffectEntity org) {
        super(org);
        this.lne = org.lne;
    }

    public void setEntityWatch(Entity lne)
    {
        this.lne = lne;
    }

    @Override
    public void move(long delta)
    {
            super.move(delta);
            if(lne.isAlive())alive = true;
            else alive = false;
    }


    @Override
    public FlareEffectEntity copy(){
         return new FlareEffectEntity(this);
    }
}

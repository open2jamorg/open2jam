package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

public class MeasureEntity extends AnimatedEntity
{
    protected Render render;

    public MeasureEntity(Render r, SpriteList sl, Event.Channel ch, double x, double y)
    {
            super(sl, ch, x, y);
            this.render = r;
    }

    protected MeasureEntity(MeasureEntity org) {
        super(org);
        this.render = org.render;
    }

    @Override
    public void move(long delta)
    {
            setYMove(render.getNoteSpeed());
            super.move(delta);
    }

    @Override
    public void judgment()
    {
            alive = false;
    }

    public MeasureEntity copy(){
        return new MeasureEntity(this);
    }
}
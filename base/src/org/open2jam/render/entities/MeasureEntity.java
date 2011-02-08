package org.open2jam.render.entities;


import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

public class MeasureEntity extends AnimatedEntity implements TimeEntity
{
    protected Render render;
    
    private long time_to_hit;

    public MeasureEntity(Render r, SpriteList sl, double x, double y)
    {
        super(sl, x, y);
        this.render = r;
    }

    protected MeasureEntity(MeasureEntity org) {
        super(org);
        this.render = org.render;
    }

//    @Override
//    public void move(long delta)
//    {
//	setYMove(render.getNoteSpeed());
//	super.move(delta);
//    }

    @Override
    public void judgment()
    {
        alive = false;
    }

    @Override
    public MeasureEntity copy(){
        return new MeasureEntity(this);
    }

    public void setTime(long t) {
        time_to_hit = t;
    }

    public long getTime() {
        return time_to_hit;
    }
}
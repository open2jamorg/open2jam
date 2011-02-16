package org.open2jam.render.entities;


import org.open2jam.render.SpriteList;

public class MeasureEntity extends AnimatedEntity implements TimeEntity
{
    
    private long time_to_hit;

    public MeasureEntity(SpriteList sl, double x, double y)
    {
        super(sl, x, y);
    }

    protected MeasureEntity(MeasureEntity org) {
        super(org);
    }
    
    @Override
    public void move(long delta)
    {
	super.move(delta);
    }

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
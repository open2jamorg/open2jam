package org.open2jam.render.entities;


import org.open2jam.render.SpriteList;

public class MeasureEntity extends AnimatedEntity implements TimeEntity
{
    
    private double time_to_hit;

    public MeasureEntity(SpriteList sl, double x, double y)
    {
        super(sl, x, y);
    }

    private MeasureEntity(MeasureEntity org) {
        super(org);
    }

    @Override
    public void judgment()
    {
        dead = true;
    }

    @Override
    public MeasureEntity copy(){
        return new MeasureEntity(this);
    }

    @Override
    public void setTime(double t) {
        time_to_hit = t;
    }

    @Override
    public double getTime() {
        return time_to_hit;
    }
}
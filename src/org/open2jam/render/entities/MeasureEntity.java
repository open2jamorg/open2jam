package org.open2jam.render.entities;


import org.open2jam.render.SpriteList;

public class MeasureEntity extends AnimatedEntity implements TimeEntity, SoundEntity
{
    
    private double time_to_hit;
    private Runnable onJudge;

    public MeasureEntity(SpriteList sl, double x, double y)
    {
        super(sl, x, y);
    }

    public void setOnJudge(Runnable onJudge) {
        this.onJudge = onJudge;
    }

    private MeasureEntity(MeasureEntity org) {
        super(org);
    }

    @Override
    public void judgment()
    {
        dead = true;
        if (onJudge != null) onJudge.run();
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
package org.open2jam.render.entities;


import org.open2jam.render.Render;
/** this is a special type of entity.
*** it is invisible, 
*** it's only action is changing the bpm
*** when it reaches the judgment
***/
public class BPMEntity extends Entity implements TimeEntity
{
    private double bpm;
    protected Render render;
    private long time_to_hit;

    public BPMEntity(Render r, double bpm, double y)
    {
        this.render = r;
        this.bpm = bpm;
        this.x = 0;
        this.y = y;
        this.width = 0;
        this.height = 0;
    }

    protected BPMEntity(BPMEntity org) {
        super(org);
        this.bpm = org.bpm;
        this.render = org.render;
    }

    @Override
    public void judgment()
    {
        render.setBPM(bpm);
        alive = false;
    }

    @Override
    public void draw() {}

    @Override
    public BPMEntity copy(){
        return new BPMEntity(this);
    }

    public void setTime(long t) {
        this.time_to_hit = t;
    }

    public long getTime() {
        return time_to_hit;
    }
}
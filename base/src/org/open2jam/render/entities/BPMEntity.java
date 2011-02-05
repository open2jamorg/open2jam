package org.open2jam.render.entities;


import org.open2jam.render.Render;
/** this is a special type of entity.
*** it is invisible, 
*** it's only action is changing the bpm
*** when it reaches the judgment
***/
public class BPMEntity extends Entity
{
    private double bpm;
    protected Render render;

    public BPMEntity(Render r, double bpm, double y, double t)
    {
        this.render = r;
        this.bpm = bpm;
        this.x = 0;
        this.y = y;
        this.width = 0;
        this.height = 0;
        this.time = t;
    }

    protected BPMEntity(BPMEntity org) {
        super(org);
        this.bpm = org.bpm;
        this.render = org.render;
    }

    @Override
    public void move(long delta)
    {
        y += delta * render.getNoteSpeed();
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
}
package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

/** a NoteEntity is a animated entity which moves down.
**/
public class NoteEntity extends AnimatedEntity
{
    protected Render render;

    protected int sample_value;
    private boolean played = false;

    public NoteEntity(Render r, SpriteList sl, Event.Channel ch, double x, double y)
    {
            super(sl, ch, x, y);
            this.render = r;
    }

    protected NoteEntity(NoteEntity org) {
        super(org);
        this.render = org.render;
        this.sample_value = org.sample_value;
        this.played = org.played;
    }
    
    public void setSample(int sample){
        this.sample_value = sample;
    }

    public int getSample(){ return sample_value; }

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

    public NoteEntity copy(){
        return new NoteEntity(this);
    }

    public double getStartY(){
        return y + height;
    }
}
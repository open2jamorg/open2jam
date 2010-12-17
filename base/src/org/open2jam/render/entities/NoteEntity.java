package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;

/** a NoteEntity is a animated entity which moves down.
**/
public class NoteEntity extends AnimatedEntity
{
    protected Render render;

    protected Event.SoundSample sample_value;

    private int played = 0;

    protected Event.Channel channel = Event.Channel.NONE;

    private double hit = 0;

    public NoteEntity(Render r, SpriteList sl, Event.Channel ch, double x, double y)
    {
            super(sl, x, y);
            this.channel = ch;
            this.render = r;
    }

    protected NoteEntity(NoteEntity org) {
        super(org);
        this.channel = org.channel;
        this.render = org.render;
        this.sample_value = org.sample_value;
        this.played = org.played;
    }
    
    public void setSample(Event.SoundSample sample){
        this.sample_value = sample;
    }

    public Event.SoundSample getSample(){ return sample_value; }

    @Override
    public void move(long delta)
    {
        dy = render.getNoteSpeed();
        y += delta * dy;
    }

    public void setHit(double hit) { this.hit = hit; }
    public double getHit() { return hit; }

    public void setPlayed(int value) { played = value; }
    public int getPlayed() { return played; }

    public double testHit(double jy1, double jy2)
    {
        return testHit(y, y + height, jy1, jy2);
    }

    /**
     * y1 < y2 < jy1 < jy2 - no intersection
     * y1 < jy1 < y2 < jy2 - half intersection of y2 - jy1
     * jy1 < y1 < y2 < jy2 - full intersection
     * jy1 < y1 < jy2 < y2 - half intersection of jy2 - y1
     * jy1 < jy2 < y1 < y2 - no intersection
     */
//    protected double testHit(double y1, double y2, double jy1, double jy2)
//    {
//        if(y1 < jy1){
//            if(jy1 < y2)return (y2 - jy1)/height;
//        }
//        else{
//            if(y2 < jy2)return 1;
//            else
//            if(y1 < jy2)return (jy2 - y1)/height;
//        }
//        return 0;
//    }

    protected double testHit(double y1, double y2, double jy1, double jy2)
    {
        if(y2 < jy1)return 0;
        double p = (y2 - jy1)/render.judgmentArea();
        if(p > 2)return 0;
        else if(p > 1)p = Math.max(0, 2 - p);
        return p;
    }

    @Override
    public void judgment()
    {
        alive = false;
    }

    @Override
    public NoteEntity copy(){
        return new NoteEntity(this);
    }

    public double getStartY(){
        return y + height;
    }


    public Event.Channel getChannel() {
        return channel;
    }
}
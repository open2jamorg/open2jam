package org.open2jam.render.entities;

import org.open2jam.parsers.Event;
import org.open2jam.render.SpriteList;

/** a NoteEntity is a animated entity which moves down.
**/
public class NoteEntity extends AnimatedEntity implements TimeEntity
{
    private Event.SoundSample sample_value;

    private Event.Channel channel = Event.Channel.NONE;

    State state = State.NOT_JUDGED;

    private double hit = 0;

    double time_to_hit;

    public enum State {
        NOT_JUDGED,
        LN_HEAD_JUDGE,
        JUDGE,
        TO_KILL,
        LN_HOLD
    }

    public NoteEntity(SpriteList sl, Event.Channel ch, double x, double y)
    {
        super(sl, x, y);
        this.channel = ch;
    }

    NoteEntity(NoteEntity org) {
        super(org);
        this.channel = org.channel;
        this.sample_value = org.sample_value;
        this.state = org.state;
    }
    
    public void setSample(Event.SoundSample sample){
        this.sample_value = sample;
    }

    public Event.SoundSample getSample(){ return sample_value; }

    public void setHit(double hit) { this.hit = hit; }
    public double getHit() { return hit; }

    public void setState(State value) { state = value; }
    public State getState() { return state; }

    public double testHit(double jy1, double jy2)
    {
        return testHit(y, y + height, jy1, jy2);
    }

    public double testTimeHit(double now)
    {
        return Math.abs(time_to_hit-now);
    }

    static double testHit(double y1, double y2, double jy1, double jy2)
    {
        if(y2 < jy1)return 0;
        double p = (y2 - jy1)/(jy2 - jy1);
        if(p > 2)return 0;
        else if(p > 1)p = Math.max(0, 2 - p);
        return p;
    }
    
    @Override
    public void setPos(double x, double y)
    {
        this.x = x;
        this.y = y - height;
    }

    @Override
    public void setTime(double time){
        this.time_to_hit = time;
    }
    @Override
    public double getTime() {
        return time_to_hit;
    }
    
    @Override
    public void judgment() {}

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
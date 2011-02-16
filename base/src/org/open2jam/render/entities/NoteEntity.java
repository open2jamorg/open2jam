package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;

/** a NoteEntity is a animated entity which moves down.
**/
public class NoteEntity extends AnimatedEntity implements TimeEntity
{
    protected Event.SoundSample sample_value;

    protected Event.Channel channel = Event.Channel.NONE;

    protected State state = State.NOT_JUDGED;

    protected double hit = 0;

    protected long time_to_hit;

    public enum State {
        NOT_JUDGED,
        LN_HEAD_JUDGE,
        JUDGE,
        TO_KILL,
        LN_HOLD
    };

    public NoteEntity(SpriteList sl, Event.Channel ch, double x, double y)
    {
        super(sl, x, y);
        this.channel = ch;
    }

    protected NoteEntity(NoteEntity org) {
        super(org);
        this.channel = org.channel;
        this.sample_value = org.sample_value;
        this.state = org.state;
    }
    
    public void setSample(Event.SoundSample sample){
        this.sample_value = sample;
    }

    public Event.SoundSample getSample(){ return sample_value; }

    @Override
    public void move(long delta)
    {
	super.move(delta);
    }

    public void setHit(double hit) { this.hit = hit; }
    public double getHit() { return hit; }

    public void setState(State value) { state = value; }
    public State getState() { return state; }

    public double testHit(double jy1, double jy2)
    {
        return testHit(y, y + height, jy1, jy2);
    }

    public long testTimeHit(long now)
    {
        long p = Math.abs(time_to_hit-now);
        return p;
    }

    protected static double testHit(double y1, double y2, double jy1, double jy2)
    {
        if(y2 < jy1)return 0;
        double p = (y2 - jy1)/(jy2 - jy1);
        if(p > 2)return 0;
        else if(p > 1)p = Math.max(0, 2 - p);
        return p;
    }

    public void setTime(long time){
        this.time_to_hit = time;
    }
    public long getTime() {
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
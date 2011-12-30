/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.entities;

import org.open2jam.screen2d.Actors.EGroup;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.open2jam.utils.FrameList;
import org.open2jam.parsers.Event;

/**
 *
 * @author CdK
 */
public class NoteEntity extends AnimatedEntity implements TimeEntity {

    protected boolean isPressed;
    
    private final AnimatedEntity psprite;
    
    private Event.SoundSample sample_value;
    private Event.Channel channel = Event.Channel.NONE;
    State state = State.NOT_JUDGED;
    private float hit = 0;
    long time_to_hit;

    public enum State {
	NOT_JUDGED,
	HEAD_JUDGE,
	HOLD,
	JUDGE,
	TO_KILL,
    }

    public NoteEntity(FrameList note, Event.Channel channel) {
	super(channel.name(), note);
	this.psprite = new AnimatedEntity("PSPRT", note);
	this.channel = channel;
	
    }
    
    public NoteEntity(String name, EGroup group, Event.Channel channel) {
	super(name, ((Entity)group.findActor("SPRT")).frameList);
	this.psprite = (AnimatedEntity) group.findActor("PSPRT");
	this.channel = channel;
    }
    
    public NoteEntity(String name, FrameList note, Event.Channel channel){
	super(name, note);
	this.psprite = new AnimatedEntity(name, note);
	this.channel = channel;
    }
    
    NoteEntity(NoteEntity org) {
        super(org);
	this.psprite = org.psprite;
        this.channel = org.channel;
        this.sample_value = org.sample_value;
        this.state = org.state;
    }

    public void setSample(Event.SoundSample sample) {
	this.sample_value = sample;
    }

    public Event.SoundSample getSample() {
	return sample_value;
    }

    public void setHit(float hit) {
	this.hit = hit;
    }

    public float getHit() {
	return hit;
    }

    public void setState(State value) {
	state = value;
    }

    public State getState() {
	return state;
    }

    public float testHit(float jy1, float jy2) {
	return testHit(y, y + height, jy1, jy2);
    }

    public float testTimeHit(float now) {
	return Math.abs(time_to_hit - now);
    }

    static float testHit(float y1, float y2, float jy1, float jy2) {
	if (y2 < jy1) { return 0; }
	
	float p = (y2 - jy1) / (jy2 - jy1);
	if (p > 2) { return 0; }
	else if (p > 1) { p = Math.max(0, 2 - p); }
	
	return p;
    }
    
    public void setPressed(boolean p)
    {
	isPressed = p;
    }

    @Override
    public void act(float delta) {
	super.act(delta);
	psprite.act(delta);
	
	psprite.x = this.x;
	psprite.y = this.y;
    }
    
    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
	if(isPressed)
	{
	    psprite.draw(batch, parentAlpha);
	    return;
	}

	super.draw(batch, parentAlpha);
    }
    
    @Override
    public void judgment() {}

    @Override
    public void setTime(long time) {
	this.time_to_hit = time;
    }

    @Override
    public long getTime() {
	return time_to_hit;
    }

    public float getStartY() {
	return y + height;
    }

    public Event.Channel getChannel() {
	return channel;
    }

    @Override
    public NoteEntity copy() {
	return new NoteEntity(this);
    }
}

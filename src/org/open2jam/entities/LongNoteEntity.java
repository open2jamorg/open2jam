/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.entities;

import org.open2jam.screen2d.Actors.EGroup;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.open2jam.parsers.Event.Channel;

/**
 *
 * @author CdK
 */
public class LongNoteEntity extends NoteEntity {

    private final AnimatedEntity head, body, tail;
    private final AnimatedEntity phead, pbody, ptail;
    
    /** the size of the long note, this is constant once defined end_time */
    private Float end_dist = null;
    private Long end_time = null;
    
    public LongNoteEntity(EGroup group, Channel channel) {
	super(group.name, ((Entity)group.findActor("SPRT")).frameList, channel);
	head = (AnimatedEntity) group.findActor("HEAD");
	body = (AnimatedEntity) group.findActor("BODY");
	tail = (AnimatedEntity) group.findActor("TAIL");
	phead = (AnimatedEntity) group.findActor("PHEAD");
	pbody = (AnimatedEntity) group.findActor("PBODY");
	ptail = (AnimatedEntity) group.findActor("PTAIL");
	x = group.x;
	y = group.y;
    }
    
    public LongNoteEntity(String name, EGroup group, Channel channel)
    {
	super(name, ((Entity)group.findActor("SPRT")).frameList, channel);
	head = (AnimatedEntity) group.findActor("HEAD");
	body = (AnimatedEntity) group.findActor("BODY");
	tail = (AnimatedEntity) group.findActor("TAIL");
	phead = (AnimatedEntity) group.findActor("PHEAD");
	pbody = (AnimatedEntity) group.findActor("PBODY");
	ptail = (AnimatedEntity) group.findActor("PTAIL");
	x = group.x;
	y = group.y;
    }
    
    public LongNoteEntity(LongNoteEntity org)
    {
	super(org.name+"_"+copy++, org.frameList, org.getChannel());
	head = org.head.copy();
	body = org.body.copy();
	tail = org.tail.copy();
	phead = org.phead.copy();
	pbody = org.pbody.copy();
	ptail = org.ptail.copy();
	x = org.x;
	y = org.y;
	//System.out.println(((Entity)head).frameList.getFrameSpeed()+"  "+((Entity)body).frameList.getFrameSpeed());
    }
    
    public void setEndTime(long time, float note_size){
        this.end_time = time;
        setEndDistance(note_size);
    }

    public void setEndDistance(float note_size){
        end_dist = note_size;
    }
    
    public float getEndTime() {
        return end_time == null ? -10 : end_time;
    }

//    @Override
//    public float getStartY()
//    {
//        return y + frame.getRegionHeight();
//    }

    @Override
    public float getY()
    {
        if(end_time != null)
            return y + end_dist;
        else
            return (float) this.parent.height;
    }

    @Override
    public float getEndY() {
	return tail.y + tail.height;
    }

    @Override
    public float testHit(float jy1, float jy2)
    {
        float y1, y2;
        if(state == State.NOT_JUDGED){
            y1 = y;
        }else{
            if(end_time == null)return 0;
            y1 = y - end_dist;
        }
        y2 = y1 + frame.getRegionHeight();
        return testHit(y1, y2, jy1, jy2);
    }

    @Override
    public float testTimeHit(float now)
    {
        if(state == State.NOT_JUDGED)return Math.abs(time_to_hit-now);
        else if(end_time != null)return Math.abs(end_time-now);
        return 1000;
    }

    @Override
    public void act(float delta) {
	super.act(delta);
	int f = (int) super.sub_frame;
	head.frame = head.frameList.get(f);
	body.frame = body.frameList.get(f);
	tail.frame = tail.frameList.get(f);
	
	tail.x = body.x = head.x = ptail.x = pbody.x = phead.x = this.x;
	
	float end = getY() + (super.height-tail.height);
	float sy = (float) ((end - y - tail.height) / body.height);
	tail.y = end;
	body.scaleY = sy;
	body.y = this.y+head.height;
	head.y = this.y;
	
	if(!isPressed) return;
	
	phead.sub_frame = f;
	pbody.sub_frame = f;
	ptail.sub_frame = f;
	
	end = getY() + (super.height-ptail.height);
	sy = (float) ((end - y) / pbody.height);
	ptail.y = end;
	pbody.scaleY = sy;
	pbody.y = this.y + phead.height;
	phead.y = this.y;
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
	if(isPressed)
	{
	    pbody.draw(batch, parentAlpha);
	    ptail.draw(batch, parentAlpha);
	    phead.draw(batch, parentAlpha);
	    return;
	}
	body.draw(batch, parentAlpha);
	tail.draw(batch, parentAlpha);
	head.draw(batch, parentAlpha);
    }

    @Override
    public LongNoteEntity copy() {
	return new LongNoteEntity(this);
    }
    
}

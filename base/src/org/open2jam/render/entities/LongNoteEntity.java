package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;
import org.open2jam.render.Sprite;

public class LongNoteEntity extends NoteEntity
{
    protected SpriteList body_frames;
    protected Sprite body_sprite;

    /** the size of the long note, this is constant once defined end_time */
    protected Double end_dist = null;
    protected Long end_time = null;

    public LongNoteEntity(SpriteList head_frames, SpriteList body_frames, Event.Channel ch, double x, double y)
    {
            super(head_frames,ch, x,y);
            this.body_frames = body_frames;
            height = 0;
            body_sprite = body_frames.get(0);
    }

    protected LongNoteEntity(LongNoteEntity org) {
        super(org);
        this.body_frames = org.body_frames;
        this.body_sprite = org.body_sprite;
        this.end_time = org.end_time;
    }

    public void setEndTime(long time, double note_size){
        this.end_time = time;
        end_dist = note_size;
    }

    public long getEndTime() {
        return end_time == null ? -10 : end_time;
    }

    @Override
    public double getStartY()
    {
        return y + sprite.getHeight();
    }

    @Override
    public double getY()
    {
        if(end_time != null)
            return y - end_dist;//render.velocity_integral(time_to_hit,end_time);
        else
            return -10;
    }

    @Override
    public double testHit(double jy1, double jy2)
    {
        double y1, y2;
        if(state == State.NOT_JUDGED){
            y1 = y;
        }else{
            if(end_time == null)return 0;
            y1 = y - end_dist;
        }
        y2 = y1 + sprite.getHeight();
        double p = testHit(y1, y2, jy1, jy2);
        return p;
    }

    @Override
    public long testTimeHit(long now)
    {
        long p = 1000;
        if(state == State.NOT_JUDGED)
            p = Math.abs(time_to_hit-now);
        else
            if(end_time != null)p = Math.abs(end_time-now);
        return p;
    }

    @Override
    public void setAlpha(float alpha)
    {
        sprite.setAlpha(alpha);
        body_sprite.setAlpha(alpha);
    }

    @Override
    public void move(long delta)
    {
            super.move(delta);
            body_sprite = body_frames.get((int)sub_frame);
    }

    @Override
    public void draw()
    {
        double end = getY();
        double local_y = y;
	float sy = (float) ((local_y - end) / body_sprite.getHeight());
        body_sprite.draw(x, end, body_sprite.getScaleX(), sy);
        sprite.draw(x,local_y);
        sprite.draw(x,end);
    }

    @Override
    public LongNoteEntity copy(){
        return new LongNoteEntity(this);
    }
}
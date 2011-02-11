package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;
import org.open2jam.render.Sprite;

public class LongNoteEntity extends NoteEntity
{
    protected SpriteList body_frames;
    protected Sprite body_sprite;

    protected Double end_y = null;
    protected Long end_time = null;

    public LongNoteEntity(Render r, SpriteList head_frames, SpriteList body_frames, Event.Channel ch, double x, double y)
    {
            super(r,head_frames,ch, x,y);
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

//    public void setEndY(double ey)
//    {
//            this.end_y = ey;
//            height = y - end_y;
//    }

    public void setEndTime(long time){
        this.end_time = time;
        end_y = -10d;
//        System.out.println(end_y);
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
        if(end_y == null)
           return -10;
        else
        {
            if(end_time != null)
                return end_y = y - render.velocity_integral(time_to_hit,end_time);
            else
                return -10;
        }
    }

    @Override
    public double testHit(double jy1, double jy2)
    {
        double y1, y2;
        if(state == State.NOT_JUDGED){
            y1 = y;
        }else{
            if(end_y == null)return 0;
            y1 = end_y;
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
        if(local_y > render.getViewport())local_y = render.getViewport();
	float sy = (float) ((local_y - end) / body_sprite.getHeight());
        body_sprite.draw(x, end, body_sprite.getScaleX(), sy);
        if(local_y < render.getViewport())sprite.draw(x,local_y);
        sprite.draw(x,end);
    }

    @Override
    public void judgment()
    {
        alive = false;
    }

    @Override
    public LongNoteEntity copy(){
        return new LongNoteEntity(this);
    }
}
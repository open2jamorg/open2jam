package org.open2jam.render.entities;

import org.open2jam.parsers.Event;
import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteList;

public class LongNoteEntity extends NoteEntity
{
    private final SpriteList body_frames;
    private Sprite body_sprite;
    private final SpriteList tail_frames;
    private Sprite tail_sprite;
    private final SpriteList normal_frames;
    private Sprite normal_sprite;

    /** the size of the long note, this is constant once defined end_time */
    private Double end_dist = null;
    private Double end_time = null;

    public LongNoteEntity(SpriteList head_frames, SpriteList body_frames, SpriteList tail_frames, SpriteList normal_frames, Event.Channel ch, double x, double y)
    {
            super(head_frames,ch, x,y);
            this.body_frames = body_frames;
            this.body_sprite = body_frames.get(0);
            this.tail_frames = tail_frames;
            this.tail_sprite = tail_frames.get(0);
            this.normal_frames = normal_frames;
            this.normal_sprite = normal_frames.get(0);
            height = 0;
            
    }

    private LongNoteEntity(LongNoteEntity org) {
        super(org);
        this.body_frames = org.body_frames;
        this.body_sprite = org.body_sprite;
        this.tail_frames = org.tail_frames;
        this.tail_sprite = org.tail_sprite;
        this.normal_frames = org.normal_frames;
        this.normal_sprite = org.normal_sprite;
        this.end_time = org.end_time;
    }

    public void setEndTime(double time){
        this.end_time = time;
    }

    public void setEndDistance(double note_size){
        end_dist = note_size;
    }
    
    public double getEndTime() {
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
            return y - end_dist;
        else
            return -10;
    }

    @Override
    public double getTimeToJudge() {
        if (state == State.NOT_JUDGED || state == State.LN_HEAD_JUDGE) return time_to_hit;
        if (end_time != null) return end_time;
        return Double.MAX_VALUE;
    }

    @Override
    public void setAlpha(float alpha)
    {
        sprite.setAlpha(alpha);
        body_sprite.setAlpha(alpha);
        tail_sprite.setAlpha(alpha);
    }

    @Override
    public void move(double delta)
    {
            super.move(delta);
            body_sprite = body_frames.get((int)sub_frame);
            tail_sprite = tail_frames.get((int)sub_frame);
    }

    @Override
    public void draw()
    {
        double end = getY() - normal_sprite.getHeight();
        double local_y = y;
	float sy = (float) ((local_y - end) / body_sprite.getHeight());
        
        body_sprite.draw(x, end, body_sprite.getScaleX(), sy);                  //the middle
        tail_sprite.draw(x, local_y-tail_sprite.getHeight());                 // the bottom
        sprite.draw     (x, end);                            // the top
        
    }

    @Override
    public LongNoteEntity copy(){
        return new LongNoteEntity(this);
    }
}
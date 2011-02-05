package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.SpriteList;
import org.open2jam.render.Render;
import org.open2jam.render.Sprite;

public class LongNoteEntity extends NoteEntity
{
    protected SpriteList body_frames;
    protected Sprite body_sprite;

    //protected Double end_y = null;
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
    }

    @Override
    public double getStartY()
    {
        return y + sprite.getHeight();
    }

    @Override
    public void draw()
    {
        double end = -10;
        if(end_time != null)
        {
            end = y - ((end_time - time_to_hit) * (render.getMeasureSize() / 1000.0));
        }
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
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

    boolean played = false;

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
        this.end_y = org.end_y;
        this.played = org.played;
    }

    public void setEndY(double ey)
    {
            this.end_y = ey;
            height = y - end_y;
    }

    @Override
    public void move(long delta)
    {
            super.move(delta);
            body_sprite = body_frames.get(nextFrame);
            if(end_y != null)end_y += delta * dy;
    }

    @Override
    public void draw()
    {
            sprite.draw(x,y);

            double end = (end_y == null) ? -10 : end_y;

            double p = y - body_sprite.getHeight();
            while(p > end){
                    body_sprite.draw(x, p);
                    p -= body_sprite.getHeight();
            }
            sprite.draw(x,end);
    }

    @Override
    public void judgment()
    {
        if(!played){
            render.queueSample(sample_value);
            played = true;
        }
        if(end_y != null && end_y > render.getViewPort())alive = false;
    }

    @Override
    public LongNoteEntity copy(){
        return new LongNoteEntity(this);
    }
}
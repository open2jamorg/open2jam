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
    }

    public void setEndY(double ey)
    {
            this.end_y = ey;
            height = y - end_y;
    }


    protected boolean head_hit = false;
    @Override
    public double testHit(double jy1, double jy2)
    {
        double y1, y2;
        if(!head_hit){
            y1 = y;
        }else{
            if(end_y == null)return 0;
            y1 = end_y;
        }
        y2 = y1 + sprite.getHeight();
        double p = testHit(y1, y2, jy1, jy2);
        if(p > 0)head_hit = true;
        return p;
    }

    @Override
    public double getY()
    {
        return end_y == null ? -10 : end_y;
    }

    @Override
    public double getStartY()
    {
        return y + sprite.getHeight();
    }

    @Override
    public void move(long delta)
    {
            super.move(delta);
            body_sprite = body_frames.get((int)sub_frame);
            if(end_y != null)end_y += delta * dy;
    }

    @Override
    public void draw()
    {
        double end = getY();
        double local_y = this.y;
        if(local_y > render.getViewport())local_y = render.getViewport();
	float sx = body_sprite.getScaleX();
	float sy = (float) ((local_y - end) / (body_sprite.getHeight()));
        body_sprite.draw(x, end, sx, sy * body_sprite.getScaleY());
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
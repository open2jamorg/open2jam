package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;

/** an animated entity.
*** it loop over the frames with framespeed 
*** determined by the SpriteList 
*/
public class AnimatedEntity extends Entity
{
    double sub_frame;

    public AnimatedEntity(SpriteList sl)
    {
            this(sl, 0, 0);
    }

    public AnimatedEntity(SpriteList frames, double x, double y)
    {
            super(frames, x,y);
            sub_frame = 0;
    }

    AnimatedEntity(AnimatedEntity org) {
        super(org);
        this.frames = org.frames;
        this.sub_frame = org.sub_frame;
    }

    /** move the entity and change frame if necessary **/
    @Override
    public void move(double delta)
    {
            super.move(delta);
            sub_frame += delta * frames.getFrameSpeed();
            sub_frame %= frames.size(); // loops over
            sprite = frames.get((int)sub_frame);
    }

    @Override
    public AnimatedEntity copy(){
        return new AnimatedEntity(this);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.Render;

/**
 *
 * @author fox
 */
public class SampleEntity extends Entity
{
    private Event.SoundSample value;
    private Render render;

    public SampleEntity(Render r, Event.SoundSample value, double y)
    {
        this.render = r;
        this.value = value;
        this.x = 0;
        this.y = y;
        this.width = 0;
        this.height = 0;
    }

    private SampleEntity(SampleEntity org) {
        super(org);
        this.value = org.value;
        this.render = org.render;
    }

    @Override
    public void move(long delta)
    {
            setYMove(render.getNoteSpeed());
            super.move(delta);
    }

    @Override
    public void judgment()
    {
         render.queueSample(value);
         alive = false;
    }

    
    @Override
    public void draw() {}

    public SampleEntity copy(){
        return new SampleEntity(this);
    }
}

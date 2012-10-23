/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.open2jam.render.entities;

import org.open2jam.parsers.Event;
import org.open2jam.parsers.Event.SoundSample;
import org.open2jam.render.Render;

/**
 *
 * @author fox
 */
public class SampleEntity extends Entity implements TimeEntity, SoundEntity
{
    private final Event.SoundSample value;
    private final Render render;

    private double time_to_hit;
    private boolean note = false;
    private boolean played = false;
    
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

    public void setNote(boolean note) {
        this.note = note;
    }

    @Override
    public void move(double delta) {}

    @Override
    public void judgment() {
        judgment(true);
    }
    
    public void judgment(boolean auto)
    {
        boolean play = !auto;
        if (!dead && (!note || !render.isDisableAutoSound())) {
            play = true;
        }
        if (play && !played) {
            this.play();
            played = true;
        }
        if (!dead) {
            dead = true;
        }
    }

    public void play() {
        render.queueSample(value);
    }
    
    @Override
    public void draw() {}

    @Override
    public SampleEntity copy(){
        return new SampleEntity(this);
    }

    @Override
    public void setTime(double t) {
        this.time_to_hit = t;
    }

    @Override
    public double getTime() {
        return time_to_hit;
    }

    
}

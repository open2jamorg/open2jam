/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.entities;

import com.badlogic.gdx.Gdx;
import org.open2jam.parsers.Event;
import org.open2jam.sound.SoundManager;

/**
 *
 * @author fox
 */
public class SampleEntity extends Entity implements TimeEntity
{
    private final Event.SoundSample value;

    private long time_to_hit;

    public SampleEntity(Event.SoundSample value)
    {
	super("SAMPLE_"+copy++);
        this.value = value;
        this.x = 0;
        this.width = 0;
        this.height = 0;
    }

    private SampleEntity(SampleEntity org) {
        super(org);
        this.value = org.value;
    }

    @Override
    public void judgment()
    {
	SoundManager.play(1, value.sample_id);
        this.markToRemove(true);
    }

    @Override
    public SampleEntity copy(){
        return new SampleEntity(this);
    }

    @Override
    public void setTime(long t) {
        this.time_to_hit = t;
    }

    @Override
    public long getTime() {
        return time_to_hit;
    }
}

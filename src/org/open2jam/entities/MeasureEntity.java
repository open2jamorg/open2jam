/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.entities;

import org.open2jam.utils.FrameList;

/**
 *
 * @author CdK
 */
public class MeasureEntity extends AnimatedEntity implements TimeEntity {
    
    private long time_to_hit;

    public MeasureEntity(String name, FrameList list) {
	super(name, list);
    }

    private MeasureEntity(MeasureEntity m) {
	super(m.name+"_"+copy++, m.frameList);
	x = m.x;
	y = m.y;
    }
    
    @Override
    public void setTime(long t) {
	time_to_hit = t;
    }

    @Override
    public long getTime() {
	return time_to_hit;
    }

    @Override
    public void judgment() {
	
    }
    
    @Override
    public MeasureEntity copy() {
	return new MeasureEntity(this);
    }
    
}

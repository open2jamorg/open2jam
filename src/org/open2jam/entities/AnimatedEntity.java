package org.open2jam.entities;

import org.open2jam.utils.FrameList;
import org.open2jam.utils.SystemTimer;

/**
 *
 * @author CdK
 */
public class AnimatedEntity extends Entity {
    
    float sub_frame;
    float last_frame;
    
    public boolean loop = true;

    public AnimatedEntity(String name) {
	super(name);
    }

    public AnimatedEntity(String name, FrameList list) {
	super(name, list);
	sub_frame = 0;
	this.loop = list.getLoop();
    }
    
    AnimatedEntity(AnimatedEntity org) {
        super(org);
        this.sub_frame = org.sub_frame;
	this.last_frame = org.last_frame;
        this.loop = org.loop;
    }

    @Override
    public void act(float delta) {
	super.act(delta);
	
	sub_frame = SystemTimer.getFrameTime() * frameList.getFrameSpeed();
	sub_frame %= frameList.size();
	
//	sub_frame += delta * frameList.getFrameSpeed();
//	sub_frame %= frameList.size();
	if(!loop && sub_frame < last_frame)
	    this.remove();
	else
	    last_frame = sub_frame;
	
	frame = frameList.get((int)sub_frame);
    }
    
    @Override
    public AnimatedEntity copy(){
        return new AnimatedEntity(this);
    }
    
}

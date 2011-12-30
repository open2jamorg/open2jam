package org.open2jam.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;

/**
 *
 * @author CdK
 */
public class FrameList extends ArrayList<TextureRegion> {

    /** the frame change speed */
    private float framespeed = 0;
    private boolean loop = true;
    
    private boolean atBeat = false;

    public FrameList() {
	super();
    }
    
    public FrameList(float fs) {
	super();
	this.framespeed = fs;
    }
    
    public FrameList(float fs, boolean loop, boolean atBeat)
    {
	super();
	this.framespeed = fs;
	this.loop = loop;
	this.atBeat = atBeat;
    }

    public boolean add(Texture e) {
	return super.add(new TextureRegion(e));
    }
    
    public void setLoop(boolean loop){
	this.loop = loop;
    }
    
    public boolean getLoop(){
	return loop;
    }

    public void setFrameSpeed(float fs) {
	this.framespeed = fs;
    }
    
    public void setFrameSpeed(float fs, boolean atBeat) {
	this.framespeed = fs;
	this.atBeat = atBeat;
    }
    
    public float getFrameSpeed() {
	return framespeed;
    }
}

package org.open2jam.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import org.open2jam.utils.Copyable;
import org.open2jam.utils.FrameList;

/**
 * Based on the Image actor. The only thing I've added is the FrameList
 *
 * @author CdK
 */
public class Entity extends Actor implements Copyable<Entity> {

    FrameList frameList;
    TextureRegion frame;
    
    static long copy = 0;
    
    public int z_index = 0;
    
    public String group_name;

    public Entity(String name) {
	super(name);
	this.frame = null;
	this.frameList = null;
    }
    
    public Entity(String name, FrameList list) {
	super(name);
	this.frame = list.get(0);
	width = Math.abs(frame.getRegionWidth());
	height = Math.abs(frame.getRegionHeight());
	
	frameList = list;
    }

    public Entity(String name, Texture texture) {
	super(name);
	this.width = texture.getWidth();
	this.height = texture.getHeight();
	this.frame = new TextureRegion(texture);
	
	frameList.add(frame);
    }

    public Entity(String name, TextureRegion region) {
	super(name);
	this.frame = new TextureRegion(region);
	width = Math.abs(region.getRegionWidth());
	height = Math.abs(region.getRegionHeight());
	
	frameList.add(this.frame);
    }
    
    protected Entity(Entity e)
    {
	super(e.name+"_"+copy++);
	this.frameList = e.frameList;
	this.z_index = e.z_index;
	this.frame = e.frame;
	
	this.width = e.width;
	this.height = e.height;
	this.x = e.x;
	this.y = e.y;
    }
    
    public float getX(){ return x;}
    public float getY(){ return y;}
    public float getEndY(){ return y+this.height; }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
	if(frame == null) return;
	
	if (frame.getTexture() != null) {
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (scaleX == 1 && scaleY == 1 && rotation == 0)
			batch.draw(frame, x, y, width, height);
		else
			batch.draw(frame, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
	}
    }
    
    public Texture getTexture() {
	return frame.getTexture();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer) {
	return x > 0 && y > 0 && x < width && y < height;
    }

    @Override
    public void touchUp(float x, float y, int pointer) {
    }

    @Override
    public void touchDragged(float x, float y, int pointer) {
    }

    @Override
    public Actor hit(float x, float y) {
	if (x > 0 && x < width) if (y > 0 && y < height) return this;

	return null;
    }
    
    
    
    public void setOrigin(float x, float y)
    {
	originX = x;
	originY = y;
    }
    
    public void centerOrigin()
    {
	originX = width / 2.0f;
	originY = height / 2.0f;
    }

    @Override
    public Entity copy() {
	return new Entity(this);
    }
}

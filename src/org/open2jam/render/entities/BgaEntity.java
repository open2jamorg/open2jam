/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.render.entities;

import java.util.LinkedList;
import org.open2jam.render.Sprite;

/**
 *
 * @author CdK
 */
public class BgaEntity extends Entity implements TimeEntity {

    private LinkedList<Double> times;
    
    private LinkedList<Sprite> next_sprites;
    
    private double scale_w = 0, scale_h = 0;

    public BgaEntity(Sprite s, double x, double y) {
	super(s, x, y);
	scale_w = width;
	scale_h = height;
	next_sprites = new LinkedList<Sprite>();
	times = new LinkedList<Double>();
    }
    
    public BgaEntity(BgaEntity org) {
	this.sprite = org.sprite;
	this.x = org.x;
	this.y = org.y;
	this.scale_w = org.scale_w;
	this.scale_h = org.scale_h;
	next_sprites = org.next_sprites;
	times = org.times;
    }
    
    public void draw() {
	if(sprite != null)
	    super.draw();
    }
    
    public void setSprite(Sprite s) {
	float w = (float) (scale_w/s.getWidth());
	float h = (float) (scale_h/s.getHeight());
	s.setScale(w, h);
	next_sprites.push(s);
    }
    
    @Override
    public void setTime(double t) {
	times.push(t);
    }

    @Override
    public double getTime() {
	if(times.isEmpty()) return -1;
	return times.getLast();
    }

    @Override
    public void judgment() {
	if(next_sprites.isEmpty()) return;
	if(times.isEmpty()) return;
	
	times.removeLast();
	this.sprite = next_sprites.removeLast();
	this.width = sprite.getWidth();
	this.height = sprite.getHeight();
    }
    
    @Override
    public BgaEntity copy(){
        return new BgaEntity(this);
    }
}

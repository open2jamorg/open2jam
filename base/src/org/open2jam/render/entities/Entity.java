package org.open2jam.render.entities;

import org.open2jam.util.Copyable;
import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteList;

public class Entity implements Copyable<Entity>
{
    protected SpriteList frames;
    protected Sprite sprite;

    /** The current speed of this entity horizontally (pixels/millisecs) */
    protected double dx;
    /** The current speed of this entity vertically (pixels/millisecs) */
    protected double dy;

    /** this object stores the position(x,y) and dimensions (width,height) */
    protected double x, y, width, height;

    protected double time = 0;

    /** when a entity die the render removes it */
    protected boolean alive = true;

    protected int layer = 0;

    /** allows constructor extensions */
    protected Entity() {}

    public Entity(SpriteList sp, double x, double y)
    {
        this.frames = sp;
        this.sprite = sp.get(0);
        this.x = x;
        this.y = y;
        width = sprite.getWidth();
        height = sprite.getHeight();
    }

    protected Entity(Entity org) {
        this.alive = org.alive;
        this.sprite = org.sprite;
        this.frames = org.frames;
        this.dx = org.dx;
        this.dy = org.dy;
        this.x = org.x;
        this.y = org.y;
        this.width = org.width;
        this.height = org.height;
        this.layer = org.layer;
    }

    public boolean isAlive() { return alive; }

    public void setAlive(boolean state){
        alive = state;
    }

    /**
     * Request that this entity move itself based on a certain ammount
     * of time passing.
     *
     * @param delta The ammount of time that has passed in milliseconds
     */
    public void move(long delta) {
        // update the location of the entity based on move speeds
        x += delta * dx;
        y += delta * dy;
    }

    /**
     * Sets the moving speed of this entity
     */
    public void setXMove(double dx) { this.dx = dx; }
    public void setYMove(double dy) { this.dy = dy; }

    /**
     * Gets the moving speed of this entity
     */
    public double getXMove() { return dx; }
    public double getYMove() { return dy; }

    public void setAlpha(float alpha)
    {
	sprite.setAlpha(alpha);
    }

    public void setTime(double t) { time = t; }
    public double getTime() { return time; }

    /**
     * Draw this entity to the graphics context provided
     */
    public void draw() {
        sprite.draw(x,y, sprite.getScaleX(), sprite.getScaleY());
    }

    /**
     * judgment time.
     * this will be called once, when it hits judgment.
     */
    public void judgment() {}

    public double getX(){ return x;}
    public double getY(){ return y;}

    public void setPos(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getWidth(){ return width;}
    public double getHeight(){ return height;}

    public void setLayer(int layer){ this.layer = layer; }
    public int getLayer(){ return layer; }

    public SpriteList getFrames(){
        return frames;
    }

    public Entity copy() {
        return new Entity(this);
    }
}

package org.open2jam.render.entities;

import org.open2jam.Copyable;
import org.open2jam.parser.Event;
import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteList;

public class Entity implements Copyable
{
    protected SpriteList frames;
    protected Sprite sprite;

    protected Event.Channel channel = Event.Channel.NONE;

    /** The current speed of this entity horizontally (pixels/millisecs) */
    protected double dx;
    /** The current speed of this entity vertically (pixels/millisecs) */
    protected double dy;

    /** this object stores the position(x,y) and dimensions (width,height) */
    protected double x, y, width, height;

    /** when a entity die the render removes it */
    protected boolean alive = true;

    /** entity scale */
    private float scale_x = 1, scale_y = 1;

    /** allows constructor extensions */
    protected Entity() {}

    public Entity(SpriteList s, Event.Channel ch)
    {
            this(s,ch, 0,0);
    }

    public Entity(SpriteList sp, Event.Channel ch, double x, double y)
    {
        this.frames = sp;
        this.sprite = sp.get(0);
        this.channel = ch;
        this.x = x;
        this.y = y;
        width = sprite.getWidth();
        height = sprite.getHeight();
    }

    protected Entity(Entity org) {
        this.alive = org.alive;
        this.channel = org.channel;
        this.sprite = org.sprite;
        this.frames = org.frames;
        this.dx = org.dx;
        this.dy = org.dy;
        this.x = org.x;
        this.y = org.y;
        this.width = org.width;
        this.height = org.height;
        this.scale_x = org.scale_x;
        this.scale_y = org.scale_y;
    }

    public boolean isAlive() { return alive; }

    public void setAlive(boolean state){
        alive = state;
    }

    public void setScale(float sx, float sy)
    {
        this.scale_x = sx;
        this.scale_y = sy;
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

    /**
     * Draw this entity to the graphics context provided
     */
    public void draw() {
            sprite.draw(x,y, scale_x, scale_y);
    }

    /**
     * judgment time.
     * this will be called once, when it hits judgment.
     */
    public void judgment() {}


    public double getX(){ return x;}
    public double getY(){ return y;}
    public void setX(double x){ this.x = x;}
    public void setY(double y){ this.y = y;}

    public double getWidth(){ return width*scale_x;}
    public double getHeight(){ return height*scale_y;}

    public SpriteList getFrames(){
        return frames;
    }


    public Event.Channel getChannel() {
        return channel;
    }

    public Entity copy() {
        return new Entity(this);
    }


}

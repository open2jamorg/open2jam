package org.open2jam.render.entities;

import java.awt.geom.Rectangle2D;
import org.open2jam.render.Sprite;

public class Entity
{
	protected Sprite sprite;

	/** The current speed of this entity horizontally (pixels/millisecs) */
	protected double dx;
	/** The current speed of this entity vertically (pixels/millisecs) */
	protected double dy;

	/** this object stores the position(x,y) and dimensions (width,height) */
	protected Rectangle2D.Double bounds;

	/** when a entity die the render removes it */
	protected boolean alive = true;

	/** allows contructor extensions */
	protected Entity() {}

	public Entity(Sprite s)
	{
		this(s,0,0);
	}

	public Entity(Sprite sp, double x, double y)
	{
		this.sprite = sp;
		bounds = new Rectangle2D.Double(x,y,sprite.getWidth(),sprite.getHeight());
	}

	public boolean isAlive() { return alive; }
	
	
	/**
	 * Request that this entity move itself based on a certain ammount
	 * of time passing.
	 * 
	 * @param delta The ammount of time that has passed in milliseconds
	 */
	public void move(long delta) {
		// update the location of the entity based on move speeds
		bounds.x += delta * dx;
		bounds.y += delta * dy;
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
		sprite.draw(bounds.x,bounds.y);
	}
	
	/**
	 * judgment time.
	 * this will be called once, when it hits judgment.
	 */
	public void judgment() {}
	
	/** return the rectangle object representing the bounds */
	public Rectangle2D.Double getBounds(){ return bounds; }
}

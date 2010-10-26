package org.open2jam.entities;

import java.awt.geom.Rectangle2D;
import org.open2jam.render.Sprite;

public class Entity
{
	protected Sprite sprite;

	/** The current speed of this entity horizontally (pixels/sec) */
	protected double dx;
	/** The current speed of this entity vertically (pixels/sec) */
	protected double dy;

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
		bounds.x += (delta * dx) / 1000;
		bounds.y += (delta * dy) / 1000;
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
	

	public void setX(double x) {
		bounds.x = x;
	}

	public void setY(double y) {
		bounds.y = y;
	}

	public double getY() {
		return bounds.y;
	}

	public double getX() {
		return bounds.x;
	}

	public double getHeight() {
		return bounds.height;
	}

	public double getWidth() {
		return bounds.width;
	}
}
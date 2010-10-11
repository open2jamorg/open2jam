package org.open2jam.entities;

import java.awt.geom.Rectangle2D;
import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteID;
import org.open2jam.render.Render;
import org.open2jam.render.ResourceFactory;

public class Entity implements Cloneable
{
	protected Sprite sprite;

	/** The current speed of this entity horizontally (pixels/sec) */
	protected double dx;
	/** The current speed of this entity vertically (pixels/sec) */
	protected double dy;

	protected Rectangle2D.Double bounds;

	/** when a entity die the render removes it */
	protected boolean alive = true;

	public Entity(SpriteID ref)
	{
		this(ref,0,0);
	}

	public Entity(SpriteID ref, double x, double y)
	{
		this.sprite = ResourceFactory.get().getSprite(ref);
		bounds = new Rectangle2D.Double(x,y,sprite.getWidth(),sprite.getHeight());
	}

	/** enable extensions and clones */
	protected Entity() {}

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
	 * Set the horizontal speed of this entity
	 * 
	 * @param dx The horizontal speed of this entity (pixels/sec)
	 */
	public void setXMove(double dx) {
		this.dx = dx;
	}

	/**
	 * Set the vertical speed of this entity
	 * 
	 * @param dy The vertical speed of this entity (pixels/sec)
	 */
	public void setYMove(double dy) {
		this.dy = dy;
	}
	
	/**
	 * Get the horizontal speed of this entity
	 * 
	 * @return The horizontal speed of this entity (pixels/sec)
	 */
	public double getXMove() {
		return dx;
	}

	/**
	 * Get the vertical speed of this entity
	 * 
	 * @return The vertical speed of this entity (pixels/sec)
	 */
	public double getYMove() {
		return dy;
	}
	
	/**
	 * Draw this entity to the graphics context provided
	 */
	public void draw() {
		sprite.draw((int) Math.round(bounds.x),(int) Math.round(bounds.y));
	}
	
	/**
	 * Do the logic associated with this entity. This method
	 * will be called periodically based on game events
	 */
	public void doLogic() {}
	
	/**
	 * Get the x location of this entity
	 * 
	 * @return The x location of this entity
	 */
	public int getX() {
		return (int) bounds.x;
	}

	public void setX(double x) {
		bounds.x = x;
	}

	public void setY(double y) {
		bounds.y = y;
	}

	/**
	 * Get the y location of this entity
	 * 
	 * @return The y location of this entity
	 */
	public int getY() {
		return (int) bounds.y;
	}

	public int getHeight() {
		return (int) bounds.height;
	}

	public int getWidth() {
		return (int) bounds.width;
	}

	/** theres nothing that needs to be deep copied on Entity */
	public Entity clone()
	{
		return new Entity(sprite.getID(), bounds.x, bounds.y);
	}
}
package org.open2jam.render.java2d;

import java.awt.Image;

import org.open2jam.render.Sprite;

/**
 * A sprite to be displayed on the screen. Note that a sprite
 * contains no state information, i.e. its just the image and 
 * not the location. This allows us to use a single sprite in
 * lots of different places without having to store multiple 
 * copies of the image.
 * 
 * @author Kevin Glass
 */
public class Java2DSprite implements Sprite {
	/** The image to be drawn for this sprite */
	private Image image;
	/** The game window to which this sprite is going to be drawn */
	private Java2DGameWindow window;
	
	/**
	 * Create a new sprite based on an image
	 * 
	 * @param window The game window to which this sprite is going to be drawn
	 * @param image The image that is this sprite
	 */
	public Java2DSprite(Java2DGameWindow window,Image image) {
		this.image = image;
		this.window = window;
	}
	
	/**
	 * Get the width of the drawn sprite
	 * 
	 * @return The width in pixels of this sprite
	 */
	public int getWidth() {
		return image.getWidth(null);
	}

	/**
	 * Get the height of the drawn sprite
	 * 
	 * @return The height in pixels of this sprite
	 */
	public int getHeight() {
		return image.getHeight(null);
	}
	
	/**
	 * Draw the sprite onto the graphics context provided
	 * 
	 * @param x The x location at which to draw the sprite
	 * @param y The y location at which to draw the sprite
	 */
	public void draw(int x,int y) {
		window.getDrawGraphics().drawImage(image,x,y,null);
	}
}
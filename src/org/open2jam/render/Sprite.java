package org.open2jam.render;

import java.nio.ByteBuffer;
import org.open2jam.render.lwjgl.Texture;

/**
 * A sprite to be displayed on the screen. Note that a sprite
 * contains no state information, i.e. its just the image and 
 * not the location. This allows us to use a single sprite in
 * lots of different places without having to store multiple 
 * copies of the image.
 * 
 * @author Kevin Glass
 */
public interface Sprite {

	/**
	 * Get the width of the drawn sprite
	 * 
	 * @return The width in pixels of this sprite
	 */
	public double getWidth();

	/**
	 * Get the height of the drawn sprite
	 * 
	 * @return The height in pixels of this sprite
	 */
	public double getHeight();
        
        /**
         *  Set the Blend alpha opengl effect on the sprite
         */
        public void setBlendAlpha(boolean b);

        /**
         * set the scale of the image
         * which will be used on the draw(x,y) call
         * the default is 1,1
         * 
         */
        public void setScale(float x, float y);
        public void setSlice(float x, float y);

        public float getScaleX();
        public float getScaleY();

	public void setAlpha(float alpha);
	
	public Texture getTexture();

	/**
	 * Draw the sprite onto the graphics context provided
	 * 
	 * @param x The x location at which to draw the sprite
	 * @param y The y location at which to draw the sprite
     */
	public void draw(double x, double y);
	public void draw(double x, double y, int w, int h, ByteBuffer buffer);
        public void draw(double x, double y, float scale_x, float scale_y);
	public void draw(double x, double y, float scale_x, float scale_y, int w, int h, ByteBuffer buffer);
}
package org.open2jam.render;

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
         * set the scale of the image
         * which will be used on the draw(x,y) call
         * the default is 1,1
         * 
         */
        public void setScale(float x, float y);

        /** to set the screen scale */
        public void setScreenScale(float x, float y);

        public float getScaleX();
        public float getScaleY();

	public void setAlpha(float alpha);

	/**
	 * Draw the sprite onto the graphics context provided
	 * 
	 * @param x The x location at which to draw the sprite
	 * @param y The y location at which to draw the sprite
         * @param scale_x, scale_y re-scale the sprite, 0 ~ 1(normal)
	 */
	public void draw(double x, double y);
        public void draw(double x, double y, float scale_x, float scale_y);
}
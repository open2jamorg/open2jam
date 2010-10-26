package org.open2jam.render.lwjgl;

import java.io.IOException;
import org.lwjgl.opengl.GL11;
import org.open2jam.render.Sprite;
import org.open2jam.render.SpriteID;

/**
 * Implementation of sprite that uses an OpenGL quad and a texture
 * to render a given image to the screen.
 * 
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class LWJGLSprite implements Sprite {
	/** The texture that stores the image for this sprite */
	private Texture texture;
  
	/** The width in pixels of this sprite */
	private int width;
  
	/** The height in pixels of this sprite */
	private int height;

	/** the id which describes this sprite */
	private SpriteID spriteID;
	
	/**
	 * Create a new sprite from a specified image.
	 * 
	 * @param window The window in which the sprite will be displayed
	 * @param ref A reference to the image on which this sprite should be based
	 */
	public LWJGLSprite(LWJGLGameWindow window,SpriteID ref) {
		try {
			texture = window.getTextureLoader().getTexture(ref);
			
			width = texture.getImageWidth();
			height = texture.getImageHeight();
		} catch (IOException e) {
			// a tad abrupt, but our purposes if you can't find a 
			// sprite's image you might as well give up.
			System.err.println("Unable to load texture: "+ref.getFile());
			System.exit(0);
		}
		this.spriteID = ref;
	}

	public SpriteID getID() {
		return spriteID;
	}
	
	/**
	 * Get the width of this sprite in pixels
	 * 
	 * @return The width of this sprite in pixels
	 */
	public int getWidth() {
		return texture.getImageWidth();
	}

	/**
	 * Get the height of this sprite in pixels
	 * 
	 * @return The height of this sprite in pixels
	 */
	public int getHeight() {
		return texture.getImageHeight();
	}

	/**
	 * Draw the sprite at the specified location
	 * 
	 * @param x The x location at which to draw this sprite
	 * @param y The y location at which to draw this sprite
	 */
	public void draw(int x, int y) {
		// store the current model matrix
		GL11.glPushMatrix();
		
		// bind to the appropriate texture for this sprite
		texture.bind();
    
		// translate to the right location and prepare to draw
		GL11.glTranslatef(x, y, 0);
		GL11.glColor3f(1,1,1);
		
		// draw a quad textured to match the sprite
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(0, 0);
			GL11.glTexCoord2f(0, texture.getHeight());
			GL11.glVertex2f(0, height);
			GL11.glTexCoord2f(texture.getWidth(), texture.getHeight());
			GL11.glVertex2f(width,height);
			GL11.glTexCoord2f(texture.getWidth(), 0);
			GL11.glVertex2f(width,0);
		}
		GL11.glEnd();
		
		// restore the model view matrix to prevent contamination
		GL11.glPopMatrix();
	}

	/** draw the sprite.
	** the same as draw(int,int)
	** but attempts to draw at the closest point
	*/
	public void draw(double x, double y)
	{
		this.draw((int)Math.round(x),(int)Math.round(y));
	}
}
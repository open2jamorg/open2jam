package org.open2jam.render.jogl;

import java.io.IOException;

import net.java.games.jogl.GL;

import org.open2jam.render.Sprite;

/**
 * Implementation of sprite that uses an OpenGL quad and a texture
 * to render a given image to the screen.
 * 
 * @author Kevin Glass
 */
public class JoglSprite implements Sprite {
	/** The texture that stores the image for this sprite */
	private Texture texture;
	/** The window that this sprite can be drawn in */
	private JoglGameWindow window;
	/** The width in pixels of this sprite */
	private int width;
	/** The height in pixels of this sprite */
	private int height;
	
	/**
	 * Create a new sprite from a specified image.
	 * 
	 * @param window The window in which the sprite will be displayed
	 * @param ref A reference to the image on which this sprite should be based
	 */
	public JoglSprite(JoglGameWindow window,String ref) {
		try {
			this.window = window;
			texture = window.getTextureLoader().getTexture(ref);
			
			width = texture.getImageWidth();
			height = texture.getImageHeight();
		} catch (IOException e) {
			// a tad abrupt, but our purposes if you can't find a 
			// sprite's image you might as well give up.
			System.err.println("Unable to load texture: "+ref);
			System.exit(0);
		}
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
		// get hold of the GL content from the window in which we're drawning
		GL gl = window.getGL();
		
		// store the current model matrix
		gl.glPushMatrix();
		
		// bind to the appropriate texture for this sprite
		texture.bind(gl);		
		// translate to the right location and prepare to draw
		gl.glTranslatef(x, y, 0);		
		gl.glColor3f(1,1,1);
		
		// draw a quad textured to match the sprite
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(0, 0);
			gl.glVertex2f(0, 0);
			gl.glTexCoord2f(0, texture.getHeight());
			gl.glVertex2f(0, height);
			gl.glTexCoord2f(texture.getWidth(), texture.getHeight());
			gl.glVertex2f(width,height);
			gl.glTexCoord2f(texture.getWidth(), 0);
			gl.glVertex2f(width,0);
		}
		gl.glEnd();
		
		// restore the model view matrix to prevent contamination
		gl.glPopMatrix();
	}
	
}
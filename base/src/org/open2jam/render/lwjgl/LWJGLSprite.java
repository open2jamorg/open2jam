package org.open2jam.render.lwjgl;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import org.lwjgl.opengl.GL11;
import org.open2jam.Util;
import org.open2jam.render.Sprite;

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

	/** the position inside the texture of the sprite */
	private int x, y;
  
	/** The width in pixels of this sprite */
	private int width;
  
	/** The height in pixels of this sprite */
	private int height;

	/** the coordinates for the texture */
	private float u, v, w, z;
	
	/**
	 * Create a new sprite from a specified image.
	 * 
	 * @param window The window in which the sprite will be displayed
	 * @param ref A reference to the image on which this sprite should be based
	 */
	public LWJGLSprite(LWJGLGameWindow window,URL ref, Rectangle slice) {
		try {
			texture = window.getTextureLoader().getTexture(ref);
		} catch (IOException e) {
                    Util.warn(e);
		}
                if(slice == null){
                        x = 0;
                        y = 0;
                        width = texture.getWidth();
                        height = texture.getHeight();
                }else{
                        x = slice.x;
                        y = slice.y;
                        width = slice.width;
                        height = slice.height;
                }
                init();
	}

    public LWJGLSprite(LWJGLGameWindow window, BufferedImage image) {
        try{
            texture = window.getTextureLoader().createTexture(image);
        }catch(IOException e){
            Util.warn(e);
        }
        x = 0; y = 0;
        width = texture.getWidth();
        height = texture.getHeight();
        init();
    }

    private void init(){
        u = ((float)x/texture.getWidth()); // top-left x
        v = ((float)y/texture.getHeight()); // top-left y

        w = ((float)(x+width)/texture.getWidth()); // bottom-right x
        z = ((float)(y+height)/texture.getHeight()); // bottom-right y
    }
	
	/**
	 * Get the width of this sprite in pixels
	 * 
	 * @return The width of this sprite in pixels
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height of this sprite in pixels
	 * 
	 * @return The height of this sprite in pixels
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Draw the sprite at the specified location
	 * 
	 * @param px The x location at which to draw this sprite
	 * @param py The y location at which to draw this sprite
	 */
	public void draw(int px, int py) {
		// store the current model matrix
		GL11.glPushMatrix();
		
		// bind to the appropriate texture for this sprite
		texture.bind();
    
		// translate to the right location and prepare to draw
		GL11.glTranslatef(px, py, 0);
		GL11.glColor3f(1,1,1);
		
		// draw a quad textured to match the sprite
		GL11.glBegin(GL11.GL_QUADS);

		GL11.glTexCoord2f(u, v);
		GL11.glVertex2f(0, 0);

		GL11.glTexCoord2f(u, z);
		GL11.glVertex2f(0, height);

		GL11.glTexCoord2f(w, z);
		GL11.glVertex2f(width,height);

		GL11.glTexCoord2f(w, v);
		GL11.glVertex2f(width,0);

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
package org.open2jam.render.lwjgl;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.open2jam.GameOptions;
import org.open2jam.render.Sprite;


public class LWJGLSprite implements Sprite {


    /** The texture that stores the image for this sprite */
    private final Texture texture;

    /** the position inside the texture of the sprite */
    private final int x, y;

    /** The width and height in pixels of this sprite */
    private final int width, height;

    private final int list_id = GL11.glGenLists(1);

    /** the scale of the image */
    private float scale_x = 1f, scale_y = 1f;

    /** the alpha */
    private float alpha = 1f;
    
    /** do blend alpha */
    private boolean blend_alpha = false;

    /**
     * Create a new sprite from a specified image.
     *
     * @param window The window in which the sprite will be displayed
     * @param ref A reference to the image on which this sprite should be based
     */
    public LWJGLSprite(LWJGLGameWindow window,URL ref, Rectangle slice) throws IOException
    {
            texture = window.getTextureLoader().getTexture(ref);
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
            init(x,y,width,height);
    }

    public LWJGLSprite(LWJGLGameWindow window, BufferedImage image)
    {
        texture = window.getTextureLoader().createTexture(image);
        x = 0; y = 0;
        width = texture.getWidth();
        height = texture.getHeight();
        init(x,y,width,height);
    }

    public LWJGLSprite(int w, int h, GameOptions.VisibilityMod type)
    {
        texture = null;
        x = 0; y = 0;
        this.width = w;
        this.height = h;
        createRectangle(width, height, type);
    }

    private void init(int x, int y, int width, int height)
    {
        float u = ((float)x/texture.getWidth()); // top-left x
        float v = ((float)y/texture.getHeight()); // top-left y
        float w = ((float)(x+width)/texture.getWidth()); // bottom-right x
        float z = ((float)(y+height)/texture.getHeight()); // bottom-right y

        GL11.glNewList(list_id, GL11.GL_COMPILE);
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
        GL11.glEndList();
    }

    private void createRectangle(int width, int height, GameOptions.VisibilityMod type)
    {
        float split = height/4f;

        GL11.glNewList(list_id, GL11.GL_COMPILE);
            GL11.glBegin(GL11.GL_QUAD_STRIP);
                switch(type)
                {
                    case Hidden: //hidden (hidden lower part)
                    GL11.glColor4f(0f,0f,0f,0f); // middle alpha
                    GL11.glVertex2f(0,split*1.9f);
                    GL11.glVertex2f(width, split*1.9f);
                    GL11.glColor3f(0f,0f,0f); // second black rec
                    GL11.glVertex2f(0,split*2f);
                    GL11.glVertex2f(width, split*2f);
                    GL11.glVertex2f(0,height);
                    GL11.glVertex2f(width, height);
                    break;
                    case Sudden: //sudden (only shows the lower part)
                    GL11.glColor3f(0f,0f,0f); // first black rec
                    GL11.glVertex2f(0, 0);
                    GL11.glVertex2f(width,0);
                    GL11.glVertex2f(0,split*1.9f);
                    GL11.glVertex2f(width, split*1.9f);
                    GL11.glColor4f(0f,0f,0f,0f); // middle alpha
                    GL11.glVertex2f(0,split*2f);
                    GL11.glVertex2f(width, split*2f);
                    break;
                    case Dark: //dark (only shows the middle part)
                    GL11.glColor3f(0f,0f,0f); // first black rec
                    GL11.glVertex2f(0, 0);
                    GL11.glVertex2f(width,0);
                    GL11.glVertex2f(0,split*1.3f);
                    GL11.glVertex2f(width, split*1.3f);
                    GL11.glColor4f(0f,0f,0f,0f); // 1st middle alpha
                    GL11.glVertex2f(0,split*1.5f);
                    GL11.glVertex2f(width, split*1.5f);
                    GL11.glColor4f(0f,0f,0f,0f); // 2nd middle alpha
                    GL11.glVertex2f(0,split*2.5f);
                    GL11.glVertex2f(width, split*2.5f);
                    GL11.glColor3f(0.0f,0.0f,0.0f); // second black rec
                    GL11.glVertex2f(0,split*2.7f);
                    GL11.glVertex2f(width, split*2.7f);
                    GL11.glVertex2f(0,height);
                    GL11.glVertex2f(width, height);
                    break;
                    default: //none
                    break;
                }
            GL11.glEnd();
        GL11.glEndList();
    }

    /**
     * Get the width of this sprite in pixels
     *
     * @return The width of this sprite in pixels
     */
    public double getWidth() {
        return width * scale_x;
    }

    /**
     * Get the height of this sprite in pixels
     *
     * @return The height of this sprite in pixels
     */
    public double getHeight() {
        return height * scale_y;
    }
    
    public Texture getTexture() {
	return texture;
    }

    /** set the width and height percentage
     * to be draw of the sprite
     */
    public void setSlice(float sx, float sy)
    {
        int px = x;
        int py = y;
        if(sx < 0) px += width;
        if(sy < 0) py += height;
        init(px,py,Math.round(sx*width),Math.round(sy*height));
    }

    public void setAlpha(float alpha)
    {
	this.alpha = alpha;
    }

    /**
     * Draw the sprite at the specified location
     *
     * @param px The x location at which to draw this sprite
     * @param py The y location at which to draw this sprite
     * @param sx the scale of the image width
     * @param sy the scale of the image height
     */
    void draw(float px, float py, float sx, float sy, int w, int h, ByteBuffer buffer)
    {                
        // store the current model matrix
        GL11.glPushMatrix();
        
        // set to blend if necessary
        if(blend_alpha)GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);

        // bind to the appropriate texture for this sprite
        if(texture != null)texture.bind();
        else GL11.glDisable(GL11.GL_TEXTURE_2D);

        // translate to the right location and prepare to draw
        GL11.glTranslatef(px, py, 0);
        
        GL11.glColor4f(1,1,1,this.alpha);
	
	
	if(buffer != null) {
	    GL11.glTexSubImage2D(
		    texture.target, 0, 0, 0,
		    w, h,
		    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
		    buffer);
	}

        GL11.glScalef(sx, sy, 1);

        // draw a quad textured to match the sprite
        GL11.glCallList(list_id);
        
        // undo the blend
        if(blend_alpha)GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if(texture == null)GL11.glEnable(GL11.GL_TEXTURE_2D);

        // restore the model view matrix to prevent contamination
        GL11.glPopMatrix();
    }
    
    public void draw(float x, float y, float scale_x, float scale_y)
    {
	this.draw(x, y, scale_x, scale_y, 0, 0, null);
    }

    public void draw(double x, double y, float scale_x, float scale_y)
    {
         this.draw((float)x,(float)y, scale_x, scale_y);
    }

    public void draw(double x, double y)
    {
        this.draw((float)x,(float)y, scale_x, scale_y);
    }

    public void setScale(float x, float y) {
        this.scale_x = x;
        this.scale_y = y;
    }

    public float getScaleX() {
       return scale_x;
    }

    public float getScaleY() {
        return scale_y;
    }

    @Override
    public void draw(double x, double y, int w, int h, ByteBuffer buffer) {
	this.draw((float)x, (float)y, scale_x, scale_y, w, h, buffer);
    }

    @Override
    public void draw(double x, double y, float scale_x, float scale_y, int w, int h, ByteBuffer buffer) {
	this.draw((float)x, (float)y, scale_x, scale_y, w, h, buffer);
    }
    
    @Override
    public void setBlendAlpha(boolean b) {
        blend_alpha = b;
    }
}
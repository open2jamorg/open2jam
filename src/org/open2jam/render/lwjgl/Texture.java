package org.open2jam.render.lwjgl;

import org.lwjgl.opengl.GL11;

/**
 * A texture to be bound within JOGL. This object is responsible for 
 * keeping track of a given OpenGL texture and for calculating the
 * texturing mapping coordinates of the full image.
 * 
 * Since textures need to be powers of 2 the actual texture may be
 * considerably bigged that the source image and hence the texture
 * mapping coordinates need to be adjusted to matchup drawing the
 * sprite against the texture.
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class Texture {
    /** The GL target type */
    public final int target;
    /** The GL texture ID */
    private final int textureID;

    /** The height of the texture */
    private final int height;
    /** The width of the texture */
    private final int width;


    /**
     * Create a new texture
     *
     * @param target The GL target 
     * @param textureID The GL texture ID
     */
    public Texture(int target,int textureID, int width, int height) {
        this.target = target;
        this.textureID = textureID;
	this.width = width;
	this.height = height;
    }
    
    /**
     * Bind the specified GL context to a texture
     *
     */
    public void bind() {
      GL11.glBindTexture(target, textureID); 
    }
    
    /**
     * Get the height of the physical texture
     *
     * @return The height of physical texture
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Get the width of the physical texture
     *
     * @return The width of physical texture
     */
    public int getWidth() {
        return width;
    }

}

package org.open2jam.render.lwjgl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;
import org.open2jam.render.SpriteID;

/**
 * A utility class to load textures for JOGL. This source is based
 * on a texture that can be found in the Java Gaming (www.javagaming.org)
 * Wiki. It has been simplified slightly for explicit 2D graphics use.
 * 
 * OpenGL uses a particular image format. Since the images that are 
 * loaded from disk may not match this format this loader introduces
 * a intermediate image which the source image is copied into. In turn,
 * this image is used as source for the OpenGL texture.
 *
 * @author Kevin Glass
 * @author Brian Matzon
 * @author chaosfox
 */
public class TextureLoader {
    /** The table of textures that have been loaded in this loader */
    private HashMap<SpriteID,Texture> table = new HashMap<SpriteID,Texture>();

    /** The colour model including alpha for the GL image */
    private ColorModel glAlphaColorModel;
    
    /** The colour model for the GL image */
    private ColorModel glColorModel;
    
    /** 
     * Create a new texture loader based on the game panel
     *
     * @param gl The GL content in which the textures should be loaded
     */
    public TextureLoader() {
        glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                            new int[] {8,8,8,8},
                                            true,
                                            false,
                                            ComponentColorModel.TRANSLUCENT,
                                            DataBuffer.TYPE_BYTE);
                                            
        glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                            new int[] {8,8,8,0},
                                            false,
                                            false,
                                            ComponentColorModel.OPAQUE,
                                            DataBuffer.TYPE_BYTE);
    }
    
    /**
     * Create a new texture ID 
     *
     * @return A new texture ID
     */
    private int createTextureID() 
    { 
       IntBuffer tmp = createIntBuffer(1); 
       GL11.glGenTextures(tmp); 
       return tmp.get(0);
    } 
    
    /**
     * Load a texture
     *
     * @param resourceName The location of the resource to load
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public Texture getTexture(SpriteID resource) throws IOException {
        Texture tex = table.get(resource);
        
        if (tex != null)return tex;
        
        tex = getTexture(resource,
                         GL11.GL_TEXTURE_2D, // target
                         GL11.GL_RGBA,     // dst pixel format
                         GL11.GL_LINEAR, // min filter (unused)
                         GL11.GL_LINEAR);
        
        table.put(resource,tex);
        
        return tex;
    }
    
    /**
     * Load a texture into OpenGL from a image reference on
     * disk.
     *
     * @param resourceName The location of the resource to load
     * @param target The GL target to load the texture against
     * @param dstPixelFormat The pixel format of the screen
     * @param minFilter The minimising filter
     * @param magFilter The magnification filter
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public Texture getTexture(SpriteID resource, 
                              int target, 
                              int dstPixelFormat, 
                              int minFilter, 
                              int magFilter) throws IOException 
    {
        int srcPixelFormat = 0;
        
        // create the texture ID for this texture 
        int textureID = createTextureID(); 
        Texture texture = new Texture(target,textureID); 
        
        // bind this texture 
        GL11.glBindTexture(target, textureID); 
 
        BufferedImage bufferedImage = loadImage(resource);

	java.awt.Rectangle slice = resource.getSlice();

	int texw, texh;
	if(slice != null) {
		texture.setWidth(slice.width);
		texture.setHeight(slice.height);
		texw = get2Fold(slice.width);
		texh = get2Fold(slice.height);
        }else{
		texture.setWidth(bufferedImage.getWidth());
		texture.setHeight(bufferedImage.getHeight());
		texw = get2Fold(bufferedImage.getWidth());
		texh = get2Fold(bufferedImage.getHeight());
	}
	texture.setTextureWidth(texw);
	texture.setTextureHeight(texh);

        if (bufferedImage.getColorModel().hasAlpha()) {
            srcPixelFormat = GL11.GL_RGBA;
        } else {
            srcPixelFormat = GL11.GL_RGB;
        }

        // convert that image into a byte buffer of texture data 
        ByteBuffer textureBuffer = convertImageData(bufferedImage, slice); 
        
        if (target == GL11.GL_TEXTURE_2D) 
        { 
            GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter); 
            GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter); 
        } 
 
        // produce a texture from the byte buffer
        GL11.glTexImage2D(target, 
                      0, 
                      dstPixelFormat, 
                      texw, 
                      texh,
                      0, 
                      srcPixelFormat, 
                      GL11.GL_UNSIGNED_BYTE, 
                      textureBuffer ); 
        
        return texture; 
    } 
    
    /**
     * Get the closest greater power of 2 to the fold number
     * 
     * @param fold The target number
     * @return The power of 2
     */
    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    } 
    
	/**
	* Convert the buffered image to a texture
	*
	* @param bufferedImage The image to convert to a texture
	* @param texture The texture to store the data into
	* @param slice specify only a part of the source image, can be null
	* @return A buffer containing the data
	*/
	private ByteBuffer convertImageData(BufferedImage bufferedImage, java.awt.Rectangle slice)
	{
		ByteBuffer imageBuffer = null; 
		WritableRaster raster;
		BufferedImage texImage;

		int srcx, srcy, srcw, srch;
		if(slice != null) {
			srcx = slice.x;
			srcy = slice.y;
			srcw = slice.width;
			srch = slice.height;
		}else{
			srcx = 0;
			srcy = 0;
			srcw = bufferedImage.getWidth();
			srch = bufferedImage.getHeight();
		}
		int texWidth = get2Fold(srcw);
		int texHeight = get2Fold(srch);

		// create a raster that can be used by OpenGL as a source
		// for a texture
		if (bufferedImage.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
			texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable());
		} else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,3,null);
			texImage = new BufferedImage(glColorModel,raster,false,new Hashtable());
		}
		
		// copy the source image into the produced image
		Graphics g = texImage.getGraphics();
		g.setColor(new Color(0f,0f,0f,0f));
		g.fillRect(0,0,texWidth,texHeight);
		g.drawImage(bufferedImage,
				0, 0,
				srcw, srch,
				srcx, srcy,
				srcx+srcw, srcy+srch,
			null);
		
		// build a byte buffer from the temporary image 
		// that be used by OpenGL to produce a texture.
		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData(); 

		imageBuffer = ByteBuffer.allocateDirect(data.length); 
		imageBuffer.order(ByteOrder.nativeOrder()); 
		imageBuffer.put(data, 0, data.length); 
		imageBuffer.flip();

		return imageBuffer; 
	}
    
	/** 
	* Load a given resource as a buffered image
	* 
	* @param ref The location of the resource to load
	* @return The loaded buffered image
	* @throws IOException Indicates a failure to find a resource
	*/
	private BufferedImage loadImage(SpriteID resource) throws IOException 
	{
		java.net.URL url = TextureLoader.class.getClassLoader().getResource(resource.getFile());
		
		if (url == null)throw new IOException("Cannot find: "+resource.getFile());

		//BufferedImage bufferedImage = ImageIO.read(new BufferedInputStream(is)); 
		// due to an issue with ImageIO and mixed signed code
		// we are now using good oldfashioned ImageIcon to load
		// images and the paint it on top of a new BufferedImage
		java.awt.Image img = new javax.swing.ImageIcon(url).getImage();
		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = bufferedImage.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return bufferedImage;
	}
    
    /**
     * Creates an integer buffer to hold specified ints
     * - strictly a utility method
     *
     * @param size how many int to contain
     * @return created IntBuffer
     */
    protected IntBuffer createIntBuffer(int size) {
      ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
      temp.order(ByteOrder.nativeOrder());

      return temp.asIntBuffer();
    }

    /** verify if a number is a power of 2.
     * just a helper function for getTexture3D */
    protected static boolean isPowerOfTwo(int n) {  
      return ((n & (n - 1)) == 0) && n > 0;  
    }
}

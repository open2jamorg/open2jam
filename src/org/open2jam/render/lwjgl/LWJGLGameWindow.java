package org.open2jam.render.lwjgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.logging.Level;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.open2jam.render.GameWindow;
import org.open2jam.render.GameWindowCallback;
import org.open2jam.util.Logger;

/**
 * An implementation of GameWindow that will use OPENGL (JOGL) to 
 * render the scene. Its also responsible for monitoring the keyboard
 * using AWT.
 * 
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class LWJGLGameWindow implements GameWindow {


	/** The callback which should be notified of window events */
	private GameWindowCallback callback;
  
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
  
	/** The width of the game display area */
	private int width;
  
	/** The height of the game display area */
	private int height;

        /** The bilinear filter */
        private boolean bilinear;
	/** The loader responsible for converting images into OpenGL textures */
	private TextureLoader textureLoader;
  
	/** Title of window, we get it before our window is ready, so store it till needed */
	private String title;

    private float scale_x = 1f, scale_y = 1f;
        private float scale_x2 = 1f, scale_y2 = 1f;

	/**
	 * Create a new game window that will use OpenGL to 
	 * render our game.
	 */
	public LWJGLGameWindow() {
	}
	
	/**
	 * Retrieve access to the texture loader that converts images
	 * into OpenGL textures. Note, this has been made package level
	 * since only other parts of the JOGL implementations need to access
	 * it.
	 * 
	 * @return The texture loader that can be used to load images into
	 * OpenGL textures.
	 */
	TextureLoader getTextureLoader() {
		return textureLoader;
	}
	
	/**
	 * Set the title of this window.
	 *
	 * @param title The title to set on this window
	 */
	public void setTitle(String title) {
	    this.title = title;
	    if(Display.isCreated()) {
	    	Display.setTitle(title);
	    }
	}

	/**
	 * Set the resolution of the game display area.
	 *
     */
	public void setDisplay(DisplayMode dm, boolean vsync, boolean fs, boolean bilinear) {
            try{
                Display.setDisplayMode(dm);
                Display.setVSyncEnabled(vsync);
                Display.setFullscreen(fs);
                width = dm.getWidth();
                height = dm.getHeight();
                this.bilinear = bilinear;
            }catch(LWJGLException e){
                Logger.global.log(Level.WARNING, "LWJGL Error: {0}", e.getMessage());
            }
        }

	public int getResolutionHeight(){ return height; }
        public int getResolutionWidth(){ return width; }
	
	/**
	 * Start the rendering process. This method will cause the display to redraw
	 * as fast as possible.
	 */
	public void startRendering()
        {
            if(callback == null)throw new RuntimeException(" Need callback to start rendering !");

            try {
                Display.create();
            } catch (LWJGLException ex) {
                Logger.global.log(Level.SEVERE, "{0}", ex);
                callback.windowClosed();
                return;
            }

            Display.setTitle(title);
            
            // center the display on the screen
            Display.setLocation(-1, -1);

            // grab the mouse, dont want that hideous cursor when we're playing!
            // only when in fullscreen mode
            Mouse.setGrabbed(Display.isFullscreen());

            // enable textures since we're going to use these for our sprites
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // disable the OpenGL depth test since we're rendering 2D graphics
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            
            //the color of the COLOR_BUFFER_BIT, to be changed by the skin... i guess
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            // enable apha blending
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
	    
	    //Enable scissor test
	    GL11.glEnable(GL11.GL_SCISSOR_TEST);
	    GL11.glScissor(0, 0, width, height);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();

            GL11.glOrtho(0, width, height, 0, -1, 1);

            textureLoader = new TextureLoader();
            
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            
            if(bilinear)
                bilinear = initFBO();

            callback.initialise();

            gameLoop();
	}

        public void update(){
            Display.update();
        }

	/**
	 * Register a callback that will be notified of game window
	 * events.
	 *
	 * @param callback The callback that should be notified of game
	 * window events. 
	 */
	public void setGameWindowCallback(GameWindowCallback callback) {
            this.callback = callback;
	}
	
	/**
	 * Check if a particular key is current held.
	 *
	 * @param keyCode The code associated with the key to check 
	 * @return True if the specified key is being held
	 */
	public boolean isKeyDown(int keyCode)
        {
            return Keyboard.isKeyDown(keyCode);
	}
        
        public void initScales(double w, double h){
            scale_x = (float) (width/w);
            scale_y = (float) (height/h);
        //TODO idk :/ it works so... needs a refactor
            scale_x2 = (float) (w/width);
            scale_y2 = (float) (h/height);
        }

	/**
	 * Run the main game loop. This method keeps rendering the scene
	 * and requesting that the callback update its screen.
	 */
	private void gameLoop()
        {
            gameRunning = true;
            while (gameRunning) {
                    // clear screen
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                    GL11.glLoadIdentity();
                    if(bilinear)
                    {
                        //first we draw everything in the fbo
                        drawToFBO();
                        //then scale
                        GL11.glScalef(scale_x >= 1f ? scale_x : scale_x2, scale_y >= 1f ? scale_y : scale_y2, 1);
                        //then draw back the fbo texture
                        drawFBO();
                    }
                    else
                    {
                        GL11.glScalef(scale_x, scale_y, 1);
                        callback.frameRendering();
                    }

                    Display.update();
                    
                    if(Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                            destroy();
                    }
            }
            Display.destroy();
	}

    public void destroy() {
        destroyFBO();
        gameRunning = false;
        callback.windowClosed();
    }

    int fboID;
    int texID;
    public boolean initFBO()
    {
        //create the framebuffer object
        IntBuffer tmp = ByteBuffer.allocateDirect(1*4).order(ByteOrder.nativeOrder()).asIntBuffer();
        EXTFramebufferObject.glGenFramebuffersEXT(tmp);
        fboID = tmp.get();

        //now the texture
        tmp = ByteBuffer.allocateDirect(1*4).order(ByteOrder.nativeOrder()).asIntBuffer();
        GL11.glGenTextures(tmp);
        texID = tmp.get();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                          GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,(IntBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        //attach the texture to the fbo
        EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID );

        EXTFramebufferObject.glFramebufferTexture2DEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                                                        EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,
                                                        GL11.GL_TEXTURE_2D, texID, 0);

        // check if everything is ok
        int framebuffer = EXTFramebufferObject.glCheckFramebufferStatusEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT );

        if(framebuffer != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT){
            Logger.global.log(Level.WARNING, "FBO wasn't initialized!!!");
            return false;
        }

        EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0 );

        return true;
    }

    public void drawToFBO()
    {
        EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID );

            GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
            GL11.glViewport( 0, 0, width, height );

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GL11.glLoadIdentity();

            GL11.glScalef(scale_x < 1f ? scale_x : 1f, scale_y < 1f ? scale_y : 1f, 1);

            callback.frameRendering();

            GL11.glPopAttrib();
        EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0 );
    }

    public void drawFBO()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
            GL11.glBegin(GL11.GL_QUADS);

            GL11.glTexCoord2f(0f, 1f);
            GL11.glVertex2f(0, 0);

            GL11.glTexCoord2f(0f, 0f);
            GL11.glVertex2f(0, height);

            GL11.glTexCoord2f(1f, 0f);
            GL11.glVertex2f(width,height);

            GL11.glTexCoord2f(1f, 1f);
            GL11.glVertex2f(width,0);

            GL11.glEnd();
    }

    public void destroyFBO()
    {
        EXTFramebufferObject.glDeleteFramebuffersEXT(fboID);
    }
}

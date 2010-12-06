package org.open2jam.render.lwjgl;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.open2jam.render.GameWindow;
import org.open2jam.render.GameWindowCallback;

/**
 * An implementation of GameWindow that will use OPENGL (JOGL) to 
 * render the scene. Its also responsible for monitoring the keyboard
 * using AWT.
 * 
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class LWJGLGameWindow implements GameWindow {

        static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        
        private static final HashMap<Integer,Integer> key_map = new HashMap<Integer,Integer>();

	/** The callback which should be notified of window events */
	private GameWindowCallback callback;
  
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
  
	/** The width of the game display area */
	private int width;
  
	/** The height of the game display area */
	private int height;

	/** The loader responsible for converting images into OpenGL textures */
	private TextureLoader textureLoader;
  
	/** Title of window, we get it before our window is ready, so store it till needed */
	private String title;

        static {
            key_map.put(KeyEvent.VK_S, Keyboard.KEY_S);
            key_map.put(KeyEvent.VK_D, Keyboard.KEY_D);
            key_map.put(KeyEvent.VK_F, Keyboard.KEY_F);
            key_map.put(KeyEvent.VK_SPACE, Keyboard.KEY_SPACE);
            key_map.put(KeyEvent.VK_J, Keyboard.KEY_J);
            key_map.put(KeyEvent.VK_K, Keyboard.KEY_K);
            key_map.put(KeyEvent.VK_L, Keyboard.KEY_L);
            key_map.put(KeyEvent.VK_ESCAPE, Keyboard.KEY_ESCAPE);
            key_map.put(KeyEvent.VK_UP, Keyboard.KEY_UP);
            key_map.put(KeyEvent.VK_DOWN, Keyboard.KEY_DOWN);
            key_map.put(KeyEvent.VK_LEFT, Keyboard.KEY_LEFT);
            key_map.put(KeyEvent.VK_RIGHT, Keyboard.KEY_RIGHT);
        }
	
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
	 * @param x The width of the game display area
	 * @param y The height of the game display area
	 */
	public void setDisplay(DisplayMode dm, boolean vsync, boolean fs) {
            try{
                Display.setDisplayMode(dm);
                Display.setVSyncEnabled(vsync);
                Display.setFullscreen(fs);
                width = dm.getWidth();
                height = dm.getHeight();
            }catch(LWJGLException e){
                logger.log(Level.WARNING, "LWJGL Error: {0}", e.getMessage());
            }
        }

	public int getResolutionHeight(){ return height; }
	
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
                Logger.getLogger(LWJGLGameWindow.class.getName()).log(Level.SEVERE, null, ex);
                callback.windowClosed();
                return;
            }

            setTitle(title);

            // grab the mouse, dont want that hideous cursor when we're playing!
            if(Display.isFullscreen())Mouse.setGrabbed(true);

            // enable textures since we're going to use these for our sprites
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // disable the OpenGL depth test since we're rendering 2D graphics
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            // enable apha blending
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();

            GL11.glOrtho(0, width, height, 0, -1, 1);

            textureLoader = new TextureLoader();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

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
            Integer code = key_map.get(keyCode);
            if(code == null)code = keyCode; // use raw key
            return Keyboard.isKeyDown(code);
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
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                    GL11.glLoadIdentity();

                    // let subsystem paint
                    callback.frameRendering();

                    // update window contents
                    Display.update();

                    if(Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                            destroy();
                    }
            }
            Display.destroy();
	}

    public void destroy() {
        gameRunning = false;
        callback.windowClosed();
    }
}

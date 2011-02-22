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

        private float screen_scale_x = 1f, screen_scale_y = 1f;

	/** The loader responsible for converting images into OpenGL textures */
	private TextureLoader textureLoader;
  
	/** Title of window, we get it before our window is ready, so store it till needed */
	private String title;


        private float scale_x = 1f, scale_y = 1f;

        static {
//            key_map.put(KeyEvent.VK_S, Keyboard.KEY_S);
//            key_map.put(KeyEvent.VK_D, Keyboard.KEY_D);
//            key_map.put(KeyEvent.VK_F, Keyboard.KEY_F);
//            key_map.put(KeyEvent.VK_SPACE, Keyboard.KEY_SPACE);
//            key_map.put(KeyEvent.VK_J, Keyboard.KEY_J);
//            key_map.put(KeyEvent.VK_K, Keyboard.KEY_K);
//            key_map.put(KeyEvent.VK_L, Keyboard.KEY_L);
//            key_map.put(KeyEvent.VK_CONTROL, Keyboard.KEY_LCONTROL);
//            key_map.put(KeyEvent.VK_ESCAPE, Keyboard.KEY_ESCAPE);
//            key_map.put(KeyEvent.VK_UP, Keyboard.KEY_UP);
//            key_map.put(KeyEvent.VK_DOWN, Keyboard.KEY_DOWN);
//            key_map.put(KeyEvent.VK_LEFT, Keyboard.KEY_LEFT);
//            key_map.put(KeyEvent.VK_RIGHT, Keyboard.KEY_RIGHT);
            
        /* Taken from KeyboardEventQueue.java from lwjgl project (yeah, it was done there u-u)
         *
         *      It's still lacks some keys like control because java is so awesome
         *      that the left and right control is called the same VK_CONTROL, you have to
         *      use KeyEvent.getKeyLocation() to get what control have the user pressed,
         *      and lwjgl uses the standard in this (i think) left control called KEY_LCONTROL
         *      and right called KEY_RCONTROL
         *
         *      Also I'm assuming that the commented lines are nonstandard ones too
         *
         * @author elias_naur
         */
		key_map.put(KeyEvent.VK_0, Keyboard.KEY_0);
		key_map.put(KeyEvent.VK_1, Keyboard.KEY_1);
		key_map.put(KeyEvent.VK_2, Keyboard.KEY_2);
		key_map.put(KeyEvent.VK_3, Keyboard.KEY_3);
		key_map.put(KeyEvent.VK_4, Keyboard.KEY_4);
		key_map.put(KeyEvent.VK_5, Keyboard.KEY_5);
		key_map.put(KeyEvent.VK_6, Keyboard.KEY_6);
		key_map.put(KeyEvent.VK_7, Keyboard.KEY_7);
		key_map.put(KeyEvent.VK_8, Keyboard.KEY_8);
		key_map.put(KeyEvent.VK_9, Keyboard.KEY_9);
		key_map.put(KeyEvent.VK_A, Keyboard.KEY_A);
//		key_map.put(KeyEvent.VK_ACCEPT, Keyboard.KEY_ACCEPT);
		key_map.put(KeyEvent.VK_ADD, Keyboard.KEY_ADD);
//		key_map.put(KeyEvent.VK_AGAIN, Keyboard.KEY_AGAIN);
//		key_map.put(KeyEvent.VK_ALL_CANDIDATES, Keyboard.KEY_ALL_CANDIDATES);
//		key_map.put(KeyEvent.VK_ALPHANUMERIC, Keyboard.KEY_ALPHANUMERIC);
//		key_map.put(KeyEvent.VK_ALT, Keyboard.KEY_LMENU); manually mapped
		key_map.put(KeyEvent.VK_ALT_GRAPH, Keyboard.KEY_RMENU);
//		key_map.put(KeyEvent.VK_AMPERSAND, Keyboard.KEY_AMPERSAND);
//		key_map.put(KeyEvent.VK_ASTERISK, Keyboard.KEY_ASTERISK);
		key_map.put(KeyEvent.VK_AT, Keyboard.KEY_AT);
		key_map.put(KeyEvent.VK_B, Keyboard.KEY_B);
//		key_map.put(KeyEvent.VK_BACK_QUOTE, Keyboard.KEY_BACK_QUOTE);
		key_map.put(KeyEvent.VK_BACK_SLASH, Keyboard.KEY_BACKSLASH);
		key_map.put(KeyEvent.VK_BACK_SPACE, Keyboard.KEY_BACK);
//		key_map.put(KeyEvent.VK_BRACELEFT, Keyboard.KEY_BRACELEFT);
//		key_map.put(KeyEvent.VK_BRACERIGHT, Keyboard.KEY_BRACERIGHT);
		key_map.put(KeyEvent.VK_C, Keyboard.KEY_C);
//		key_map.put(KeyEvent.VK_CANCEL, Keyboard.KEY_CANCEL);
		key_map.put(KeyEvent.VK_CAPS_LOCK, Keyboard.KEY_CAPITAL);
		key_map.put(KeyEvent.VK_CIRCUMFLEX, Keyboard.KEY_CIRCUMFLEX);
//		key_map.put(KeyEvent.VK_CLEAR, Keyboard.KEY_CLEAR);
		key_map.put(KeyEvent.VK_CLOSE_BRACKET, Keyboard.KEY_RBRACKET);
//		key_map.put(KeyEvent.VK_CODE_INPUT, Keyboard.KEY_CODE_INPUT);
		key_map.put(KeyEvent.VK_COLON, Keyboard.KEY_COLON);
		key_map.put(KeyEvent.VK_COMMA, Keyboard.KEY_COMMA);
//		key_map.put(KeyEvent.VK_COMPOSE, Keyboard.KEY_COMPOSE);
//		key_map.put(KeyEvent.VK_CONTROL, Keyboard.KEY_LCONTROL); manually mapped
		key_map.put(KeyEvent.VK_CONVERT, Keyboard.KEY_CONVERT);
//		key_map.put(KeyEvent.VK_COPY, Keyboard.KEY_COPY);
//		key_map.put(KeyEvent.VK_CUT, Keyboard.KEY_CUT);
		key_map.put(KeyEvent.VK_D, Keyboard.KEY_D);
//		key_map.put(KeyEvent.VK_DEAD_ABOVEDOT, Keyboard.KEY_DEAD_ABOVEDOT);
//		key_map.put(KeyEvent.VK_DEAD_ABOVERING, Keyboard.KEY_DEAD_ABOVERING);
//		key_map.put(KeyEvent.VK_DEAD_ACUTE, Keyboard.KEY_DEAD_ACUTE);
//		key_map.put(KeyEvent.VK_DEAD_BREVE, Keyboard.KEY_DEAD_BREVE);
//		key_map.put(KeyEvent.VK_DEAD_CARON, Keyboard.KEY_DEAD_CARON);
//		key_map.put(KeyEvent.VK_DEAD_CEDILLA, Keyboard.KEY_DEAD_CEDILLA);
//		key_map.put(KeyEvent.VK_DEAD_CIRCUMFLEX, Keyboard.KEY_DEAD_CIRCUMFLEX);
//		key_map.put(KeyEvent.VK_DEAD_DIAERESIS, Keyboard.KEY_DEAD_DIAERESIS);
//		key_map.put(KeyEvent.VK_DEAD_DOUBLEACUTE, Keyboard.KEY_DEAD_DOUBLEACUTE);
//		key_map.put(KeyEvent.VK_DEAD_GRAVE, Keyboard.KEY_DEAD_GRAVE);
//		key_map.put(KeyEvent.VK_DEAD_IOTA, Keyboard.KEY_DEAD_IOTA);
//		key_map.put(KeyEvent.VK_DEAD_MACRON, Keyboard.KEY_DEAD_MACRON);
//		key_map.put(KeyEvent.VK_DEAD_OGONEK, Keyboard.KEY_DEAD_OGONEK);
//		key_map.put(KeyEvent.VK_DEAD_SEMIVOICED_SOUND, Keyboard.KEY_DEAD_SEMIVOICED_SOUND);
//		key_map.put(KeyEvent.VK_DEAD_TILDE, Keyboard.KEY_DEAD_TILDE);
//		key_map.put(KeyEvent.VK_DEAD_VOICED_SOUND, Keyboard.KEY_DEAD_VOICED_SOUND);
		key_map.put(KeyEvent.VK_DECIMAL, Keyboard.KEY_DECIMAL);
		key_map.put(KeyEvent.VK_DELETE, Keyboard.KEY_DELETE);
		key_map.put(KeyEvent.VK_DIVIDE, Keyboard.KEY_DIVIDE);
//		key_map.put(KeyEvent.VK_DOLLAR, Keyboard.KEY_DOLLAR);
		key_map.put(KeyEvent.VK_DOWN, Keyboard.KEY_DOWN);
		key_map.put(KeyEvent.VK_E, Keyboard.KEY_E);
		key_map.put(KeyEvent.VK_END, Keyboard.KEY_END);
		key_map.put(KeyEvent.VK_ENTER, Keyboard.KEY_RETURN);
		key_map.put(KeyEvent.VK_EQUALS, Keyboard.KEY_EQUALS);
		key_map.put(KeyEvent.VK_ESCAPE, Keyboard.KEY_ESCAPE);
//		key_map.put(KeyEvent.VK_EURO_SIGN, Keyboard.KEY_EURO_SIGN);
//		key_map.put(KeyEvent.VK_EXCLAMATION_MARK, Keyboard.KEY_EXCLAMATION_MARK);
		key_map.put(KeyEvent.VK_F, Keyboard.KEY_F);
		key_map.put(KeyEvent.VK_F1, Keyboard.KEY_F1);
		key_map.put(KeyEvent.VK_F10, Keyboard.KEY_F10);
		key_map.put(KeyEvent.VK_F11, Keyboard.KEY_F11);
		key_map.put(KeyEvent.VK_F12, Keyboard.KEY_F12);
		key_map.put(KeyEvent.VK_F13, Keyboard.KEY_F13);
		key_map.put(KeyEvent.VK_F14, Keyboard.KEY_F14);
		key_map.put(KeyEvent.VK_F15, Keyboard.KEY_F15);
//		key_map.put(KeyEvent.VK_F16, Keyboard.KEY_F16);
//		key_map.put(KeyEvent.VK_F17, Keyboard.KEY_F17);
//		key_map.put(KeyEvent.VK_F18, Keyboard.KEY_F18);
//		key_map.put(KeyEvent.VK_F19, Keyboard.KEY_F19);
		key_map.put(KeyEvent.VK_F2, Keyboard.KEY_F2);
//		key_map.put(KeyEvent.VK_F20, Keyboard.KEY_F20);
//		key_map.put(KeyEvent.VK_F21, Keyboard.KEY_F21);
//		key_map.put(KeyEvent.VK_F22, Keyboard.KEY_F22);
//		key_map.put(KeyEvent.VK_F23, Keyboard.KEY_F23);
//		key_map.put(KeyEvent.VK_F24, Keyboard.KEY_F24);
		key_map.put(KeyEvent.VK_F3, Keyboard.KEY_F3);
		key_map.put(KeyEvent.VK_F4, Keyboard.KEY_F4);
		key_map.put(KeyEvent.VK_F5, Keyboard.KEY_F5);
		key_map.put(KeyEvent.VK_F6, Keyboard.KEY_F6);
		key_map.put(KeyEvent.VK_F7, Keyboard.KEY_F7);
		key_map.put(KeyEvent.VK_F8, Keyboard.KEY_F8);
		key_map.put(KeyEvent.VK_F9, Keyboard.KEY_F9);
//		key_map.put(KeyEvent.VK_FINAL, Keyboard.KEY_FINAL);
//		key_map.put(KeyEvent.VK_FIND, Keyboard.KEY_FIND);
//		key_map.put(KeyEvent.VK_FULL_WIDTH, Keyboard.KEY_FULL_WIDTH);
		key_map.put(KeyEvent.VK_G, Keyboard.KEY_G);
//		key_map.put(KeyEvent.VK_GREATER, Keyboard.KEY_GREATER);
		key_map.put(KeyEvent.VK_H, Keyboard.KEY_H);
//		key_map.put(KeyEvent.VK_HALF_WIDTH, Keyboard.KEY_HALF_WIDTH);
//		key_map.put(KeyEvent.VK_HELP, Keyboard.KEY_HELP);
//		key_map.put(KeyEvent.VK_HIRAGANA, Keyboard.KEY_HIRAGANA);
		key_map.put(KeyEvent.VK_HOME, Keyboard.KEY_HOME);
		key_map.put(KeyEvent.VK_I, Keyboard.KEY_I);
//		key_map.put(KeyEvent.VK_INPUT_METHOD_ON_OFF, Keyboard.KEY_INPUT_METHOD_ON_OFF);
		key_map.put(KeyEvent.VK_INSERT, Keyboard.KEY_INSERT);
//		key_map.put(KeyEvent.VK_INVERTED_EXCLAMATION_MARK, Keyboard.KEY_INVERTED_EXCLAMATION_MARK);
		key_map.put(KeyEvent.VK_J, Keyboard.KEY_J);
//		key_map.put(KeyEvent.VK_JAPANESE_HIRAGANA, Keyboard.KEY_JAPANESE_HIRAGANA);
//		key_map.put(KeyEvent.VK_JAPANESE_KATAKANA, Keyboard.KEY_JAPANESE_KATAKANA);
//		key_map.put(KeyEvent.VK_JAPANESE_ROMAN, Keyboard.KEY_JAPANESE_ROMAN);
		key_map.put(KeyEvent.VK_K, Keyboard.KEY_K);
		key_map.put(KeyEvent.VK_KANA, Keyboard.KEY_KANA);
//		key_map.put(KeyEvent.VK_KANA_LOCK, Keyboard.KEY_KANA_LOCK);
		key_map.put(KeyEvent.VK_KANJI, Keyboard.KEY_KANJI);
//		key_map.put(KeyEvent.VK_KATAKANA, Keyboard.KEY_KATAKANA);
//		key_map.put(KeyEvent.VK_KP_DOWN, Keyboard.KEY_KP_DOWN);
//		key_map.put(KeyEvent.VK_KP_LEFT, Keyboard.KEY_KP_LEFT);
//		key_map.put(KeyEvent.VK_KP_RIGHT, Keyboard.KEY_KP_RIGHT);
//		key_map.put(KeyEvent.VK_KP_UP, Keyboard.KEY_KP_UP);
		key_map.put(KeyEvent.VK_L, Keyboard.KEY_L);
		key_map.put(KeyEvent.VK_LEFT, Keyboard.KEY_LEFT);
//		key_map.put(KeyEvent.VK_LEFT_PARENTHESIS, Keyboard.KEY_LEFT_PARENTHESIS);
//		key_map.put(KeyEvent.VK_LESS, Keyboard.KEY_LESS);
		key_map.put(KeyEvent.VK_M, Keyboard.KEY_M);
//		key_map.put(KeyEvent.VK_META, Keyboard.KEY_LMENU); manually mapped
		key_map.put(KeyEvent.VK_MINUS, Keyboard.KEY_MINUS);
//		key_map.put(KeyEvent.VK_MODECHANGE, Keyboard.KEY_MODECHANGE);
		key_map.put(KeyEvent.VK_MULTIPLY, Keyboard.KEY_MULTIPLY);
		key_map.put(KeyEvent.VK_N, Keyboard.KEY_N);
//		key_map.put(KeyEvent.VK_NONCONVERT, Keyboard.KEY_NONCONVERT);
		key_map.put(KeyEvent.VK_NUM_LOCK, Keyboard.KEY_NUMLOCK);
//		key_map.put(KeyEvent.VK_NUMBER_SIGN, Keyboard.KEY_NUMBER_SIGN);
		key_map.put(KeyEvent.VK_NUMPAD0, Keyboard.KEY_NUMPAD0);
		key_map.put(KeyEvent.VK_NUMPAD1, Keyboard.KEY_NUMPAD1);
		key_map.put(KeyEvent.VK_NUMPAD2, Keyboard.KEY_NUMPAD2);
		key_map.put(KeyEvent.VK_NUMPAD3, Keyboard.KEY_NUMPAD3);
		key_map.put(KeyEvent.VK_NUMPAD4, Keyboard.KEY_NUMPAD4);
		key_map.put(KeyEvent.VK_NUMPAD5, Keyboard.KEY_NUMPAD5);
		key_map.put(KeyEvent.VK_NUMPAD6, Keyboard.KEY_NUMPAD6);
		key_map.put(KeyEvent.VK_NUMPAD7, Keyboard.KEY_NUMPAD7);
		key_map.put(KeyEvent.VK_NUMPAD8, Keyboard.KEY_NUMPAD8);
		key_map.put(KeyEvent.VK_NUMPAD9, Keyboard.KEY_NUMPAD9);
		key_map.put(KeyEvent.VK_O, Keyboard.KEY_O);
		key_map.put(KeyEvent.VK_OPEN_BRACKET, Keyboard.KEY_LBRACKET);
		key_map.put(KeyEvent.VK_P, Keyboard.KEY_P);
		key_map.put(KeyEvent.VK_PAGE_DOWN, Keyboard.KEY_NEXT);
		key_map.put(KeyEvent.VK_PAGE_UP, Keyboard.KEY_PRIOR);
//		key_map.put(KeyEvent.VK_PASTE, Keyboard.KEY_PASTE);
		key_map.put(KeyEvent.VK_PAUSE, Keyboard.KEY_PAUSE);
		key_map.put(KeyEvent.VK_PERIOD, Keyboard.KEY_PERIOD);
//		key_map.put(KeyEvent.VK_PLUS, Keyboard.KEY_PLUS);
//		key_map.put(KeyEvent.VK_PREVIOUS_CANDIDATE, Keyboard.KEY_PREVIOUS_CANDIDATE);
//		key_map.put(KeyEvent.VK_PRINTSCREEN, Keyboard.KEY_PRINTSCREEN);
//		key_map.put(KeyEvent.VK_PROPS, Keyboard.KEY_PROPS);
		key_map.put(KeyEvent.VK_Q, Keyboard.KEY_Q);
//		key_map.put(KeyEvent.VK_QUOTE, Keyboard.KEY_QUOTE);
//		key_map.put(KeyEvent.VK_QUOTEDBL, Keyboard.KEY_QUOTEDBL);
		key_map.put(KeyEvent.VK_R, Keyboard.KEY_R);
		key_map.put(KeyEvent.VK_RIGHT, Keyboard.KEY_RIGHT);
//		key_map.put(KeyEvent.VK_RIGHT_PARENTHESIS, Keyboard.KEY_RIGHT_PARENTHESIS);
//		key_map.put(KeyEvent.VK_ROMAN_CHARACTERS, Keyboard.KEY_ROMAN_CHARACTERS);
		key_map.put(KeyEvent.VK_S, Keyboard.KEY_S);
		key_map.put(KeyEvent.VK_SCROLL_LOCK, Keyboard.KEY_SCROLL);
		key_map.put(KeyEvent.VK_SEMICOLON, Keyboard.KEY_SEMICOLON);
		key_map.put(KeyEvent.VK_SEPARATOR, Keyboard.KEY_DECIMAL);
//		key_map.put(KeyEvent.VK_SHIFT, Keyboard.KEY_LSHIFT); manually mapped
		key_map.put(KeyEvent.VK_SLASH, Keyboard.KEY_SLASH);
		key_map.put(KeyEvent.VK_SPACE, Keyboard.KEY_SPACE);
		key_map.put(KeyEvent.VK_STOP, Keyboard.KEY_STOP);
		key_map.put(KeyEvent.VK_SUBTRACT, Keyboard.KEY_SUBTRACT);
		key_map.put(KeyEvent.VK_T, Keyboard.KEY_T);
		key_map.put(KeyEvent.VK_TAB, Keyboard.KEY_TAB);
		key_map.put(KeyEvent.VK_U, Keyboard.KEY_U);
//		key_map.put(KeyEvent.VK_UNDERSCORE, Keyboard.KEY_UNDERSCORE);
//		key_map.put(KeyEvent.VK_UNDO, Keyboard.KEY_UNDO);
		key_map.put(KeyEvent.VK_UP, Keyboard.KEY_UP);
		key_map.put(KeyEvent.VK_V, Keyboard.KEY_V);
		key_map.put(KeyEvent.VK_W, Keyboard.KEY_W);
		key_map.put(KeyEvent.VK_X, Keyboard.KEY_X);
		key_map.put(KeyEvent.VK_Y, Keyboard.KEY_Y);
		key_map.put(KeyEvent.VK_Z, Keyboard.KEY_Z);

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
                Logger.getLogger(LWJGLGameWindow.class.getName()).log(Level.SEVERE, null, ex);
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

            // enable apha blending
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();

            GL11.glOrtho(0, width, height, 0, -1, 1);

            textureLoader = new TextureLoader();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
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

        public void setScale(float x, float y){
            scale_x = x;
            scale_y = y;
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

                    // scale
                    GL11.glScalef(scale_x, scale_y, 1);

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

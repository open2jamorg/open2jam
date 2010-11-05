package org.open2jam.render;

import org.lwjgl.opengl.DisplayMode;

/**
 * The window in which the game will be displayed. This interface exposes just
 * enough to allow the game logic to interact with, while still maintaining an
 * abstraction away from any physical implementation of windowing (i.e. AWT, LWJGL)
 *
 * @author Kevin Glass
 */
public interface GameWindow {
	
	/**
	 * Set the title of the game window
	 * 
	 * @param title The new title for the game window
	 */
	public void setTitle(String title);
	
	/**
	 * Set the game display resolution
	 * 
	 * @param x The new x resolution of the display
	 * @param y The new y resolution of the display
	 */
        public void setDisplay(DisplayMode dm, boolean vsync, boolean fs) throws Exception;

	public int getResolutionHeight();
	
	/**
	 * Start the game window rendering the display
	 */
	public void startRendering();
	
	/**
	 * Set the callback that should be notified of the window
	 * events.
	 * 
	 * @param callback The callback that should be notified of game
	 * window events.
	 */
	public void setGameWindowCallback(GameWindowCallback callback);
	
	/**
	 * Check if a particular key is pressed
	 * 
	 * @param keyCode The code associate with the key to check
	 * @return True if the particular key is pressed
	 */
	public boolean isKeyPressed(int keyCode);

        public void destroy();
}
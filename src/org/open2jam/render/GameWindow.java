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
     */
        public void setDisplay(DisplayMode dm, boolean vsync, boolean fs);

	public int getResolutionHeight();
        public int getResolutionWidth();
	
	/**
	 * Start the game window rendering the display
	 */
	public void startRendering();

        public void initScales(double width, double height);
	
	/**
	 * Set the callback that should be notified of the window
	 * events.
	 * 
	 * @param callback The callback that should be notified of game
	 * window events.
	 */
	public void setGameWindowCallback(GameWindowCallback callback);
	
	/**
	 * Check if a particular key is held
	 * 
	 * @param keyCode The code associate with the key to check
	 * @return True if the particular key is being held
	 */
	public boolean isKeyDown(int keyCode);

        public void destroy();

        /** manually update the screen */
        public void update();
}
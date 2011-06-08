package org.open2jam.render;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import org.open2jam.GameOptions;
import org.open2jam.render.lwjgl.LWJGLGameWindow;
import org.open2jam.render.lwjgl.LWJGLSprite;

/**
 * A central reference point for creating resources for use in the game. The resources
 * return may be implemented in several different rendering contexts but will also 
 * work within the GameWindow supplied from this class. For instance, a Sprite retrieved
 * as a resource will draw happily in the GameWindow supplied from this factory
 *
 * @author Kevin Glass
 */
public class ResourceFactory {
	/** The single instance of this class to ever exist <singleton> */
	private static final ResourceFactory single = new ResourceFactory();
	
	/**
 	 * Retrieve the single instance of this class 
	 *
 	 * @return The single instance of this class 
	 */
	public static ResourceFactory get() {
		return single;
	}

	/** A value to indicate that we should use OpenGL (LWJGL) to render our game */
	public static final int OPENGL_LWJGL = 1;

	/** The type of rendering that we are currently using */
	private int renderingType = OPENGL_LWJGL;
	/** The window the game should use to render */
	private GameWindow window;

	/** 
	 * The default contructor has been made private to prevent construction of 
	 * this class anywhere externally. This is used to enforce the singleton 
	 * pattern that this class attempts to follow
	 */
	private ResourceFactory() {
	}

	/** 
	 * Set the rendering method that should be used. 
	 * This can only be done before the first resource is accessed.
 	 * @param renderingType The type of rendering to use
	 */
	public void setRenderingType(int renderingType) {
		// If the rendering type is unrecognised tell the caller
		if (renderingType != OPENGL_LWJGL) {
			// Note, we could create our own exception to be thrown here but it
			// seems a little bit over the top for a simple message. In general
			// RuntimeException should be subclassed and thrown, not thrown directly.
			throw new RuntimeException("Unknown rendering type specified: "+renderingType);
		}

		// If the window has already been created then we have already created resources in 
		// the current rendering method, we are not allowed to change rendering types
		if (window != null) {
			throw new RuntimeException("Attempt to change rendering method at game runtime");
		}

		this.renderingType = renderingType;
	}

	/**
 	 * Retrieve the game window that should be used to render the game
	 *
	 * @return The game window in which the game should be rendered
	 */
	public GameWindow getGameWindow() {
		// if we've yet to create the game window, create the appropriate one
		// now
		if (window == null) {
			switch (renderingType) {
				case OPENGL_LWJGL:
				{
					window = new LWJGLGameWindow();
					break;
				}
			}
		}
		return window;
	}

	/**
	 * Create or get a sprite which displays the image that is pointed
	 * to in the class-path by "ref"
	 * 
	 * @param ref A reference to the image to load
	 * @return A sprite that can be drawn onto the current graphics context.
	 */
	public Sprite getSprite(URL ref, Rectangle slice) throws java.io.IOException {
		if (window == null) {
			throw new RuntimeException("Attempt to retrieve sprite before game window was created");
		}
		switch (renderingType) {
			case OPENGL_LWJGL:
			{
				return new LWJGLSprite((LWJGLGameWindow) window,ref, slice);
			}
		}
		throw new RuntimeException("Unknown rendering type: "+renderingType);
	}

        public Sprite getSprite(BufferedImage image) {
            if (window == null) {
                    throw new RuntimeException("Attempt to retrieve sprite before game window was created");
            }
            switch (renderingType) {
                    case OPENGL_LWJGL:
                    {
                            return new LWJGLSprite((LWJGLGameWindow) window, image);
                    }
            }
            throw new RuntimeException("Unknown rendering type: "+renderingType);
        }

        public Sprite doRectangle(int width, int height, GameOptions.VisibilityMod type)
        {
            if (window == null) {
                    throw new RuntimeException("Attempt to retrieve sprite before game window was created");
            }
            switch (renderingType) {
                    case OPENGL_LWJGL:
                    {
                            return new LWJGLSprite(width, height, type);
                    }
            }
            throw new RuntimeException("Unknown rendering type: "+renderingType);
        }
}

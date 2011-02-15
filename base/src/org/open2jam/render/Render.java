
package org.open2jam.render;

import java.util.EnumMap;
import java.util.logging.Logger;

import org.lwjgl.opengl.DisplayMode;
import org.open2jam.Config;
import org.open2jam.parser.Event;
import org.open2jam.parser.Chart;

/**
 *
 * @author fox
 */
public abstract class Render implements GameWindowCallback
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /** store the sound sources being played */
    protected static final int MAX_SOURCES = 64;
    
    /** the mapping of note channels to KeyEvent keys  */
    protected static final EnumMap<Event.Channel, Integer> keyboard_map;
    
    /** The window that is being used to render the game */
    protected final GameWindow window;

    /** the chart being rendered */
    protected final Chart chart;

    static {
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
        keyboard_map = Config.get().getKeyboardMap();
    }

    Render(Chart chart) {
        window = ResourceFactory.get().getGameWindow();
        this.chart = chart;
    }

    /** set the screen dimensions */
    public void setDisplay(DisplayMode dm, boolean vsync, boolean fs) {
        window.setDisplay(dm,vsync,fs);
    }

    /* make the rendering start */
    public abstract void startRendering();

    /** play a sample */
    public abstract void queueSample(Event.SoundSample sample);
}

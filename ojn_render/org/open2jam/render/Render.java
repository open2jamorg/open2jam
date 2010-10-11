package org.open2jam.render;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.util.Map;

import org.open2jam.parser.ResourcesHandler;
import org.open2jam.entities.*;

public class Render extends Canvas implements GameWindowCallback
{
	/** The window that is being used to render the game */
	private GameWindow window;

	/** The time at which the last rendering looped started from the point of view of the game logic */
	private long lastLoopTime = SystemTimer.getTime();

	/** The time since the last record of fps */
	private long lastFpsTime = 0;

	/** The recorded fps */
	private int fps;

	private java.util.List<Entity> entities;

	private Entity key_0;

	public Render(int renderingType)
	{
		// create a window based on a chosen rendering method
		ResourceFactory.get().setRenderingType(renderingType);
		window = ResourceFactory.get().getGameWindow();
		
		window.setResolution(800,600);
		window.setGameWindowCallback(this);
		window.setTitle("Render");
		
		window.startRendering();
	}

	/**
	 * Intialise the common elements for the game.
	 * this is called by the window render
	 */
	public void initialise()
	{
		entities = new java.util.ArrayList<Entity>();

		EntityBuilder eb = new EntityBuilder();
		try {
			javax.xml.parsers.SAXParser saxParser = 
				javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse( new java.io.File("resources.xml"), new ResourcesHandler(eb) );

		} catch (Exception err) {
			err.printStackTrace();
		}
		Map<String,Entity> entites_map = eb.getResult();
		Entity e = entites_map.get("note0");
		e.setX(50); e.setY(50);
		entities.add(e);
		e = entites_map.get("note1");
		e.setX(100); e.setY(50);
		entities.add(e);
		e = entites_map.get("note2");
		e.setX(150); e.setY(50);
		entities.add(e);
		e = entites_map.get("note3");
		e.setX(200); e.setY(50);
		entities.add(e);
		e = entites_map.get("note4");
		e.setX(250); e.setY(50);
		entities.add(e);
		e = entites_map.get("note5");
		e.setX(300); e.setY(50);
		entities.add(e);
		e = entites_map.get("note6");
		e.setX(350); e.setY(50);
		entities.add(e);
	}


	/**
	 * Notification that a frame is being rendered. Responsible for
	 * running game logic and rendering the scene.
	 */
	public void frameRendering()
	{
		SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
		
		// work out how long its been since the last update, this
		// will be used to calculate how far the entities should
		// move this loop
		long delta = SystemTimer.getTime() - lastLoopTime;
		lastLoopTime = SystemTimer.getTime();
		lastFpsTime += delta;
		fps++;
		
		// update our FPS counter if a second has passed
		if (lastFpsTime >= 1000) {
			window.setTitle("Render"+" (FPS: "+fps+")");
			lastFpsTime = 0;
			fps = 0;
		}

		// cycle round asking each entity to move itself
		for (int i=0;i<entities.size();i++)entities.get(i).move(delta);
		
		// cycle round drawing all the entities we have in the game
		for (int i=0;i<entities.size();i++)entities.get(i).draw();

		
		boolean skp = window.isKeyPressed(KeyEvent.VK_SPACE);

		if(skp && entities.size() == 0){
			entities.add(key_0.clone());
		}
	}

	public boolean removeEntity(Entity e){
		return entities.remove(e);
	}

	/**
	 * Notifcation that the game window has been closed
	 */
	public void windowClosed() {
		System.exit(0);
	}


	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		new Render(ResourceFactory.OPENGL_LWJGL);
	}
}


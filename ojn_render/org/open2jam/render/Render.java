package org.open2jam.render;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

import org.open2jam.parser.ResourcesHandler;
import org.open2jam.entities.*;

public class Render extends Canvas implements GameWindowCallback
{
	/** The window that is being used to render the game */
	private GameWindow window;

	/** The time at which the last rendering looped started from the point of view of the game logic */
	private long lastLoopTime;

	/** The time since the last record of fps */
	private long lastFpsTime = 0;

	/** The recorded fps */
	private int fps;

	/** a list of list of entities.
	 ** basically, each list is a layer of entities
	 ** the layers are rendered in order
	 ** so entities at layer X will always be renderd before layer X+1 */
	private List<List<Entity>> entities_matrix;

	private Entity key_0;

	public Render(int renderingType)
	{
		// create a window based on a chosen rendering method
		ResourceFactory.get().setRenderingType(renderingType);
		window = ResourceFactory.get().getGameWindow();
		
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
		entities_matrix = new java.util.ArrayList<List<Entity>>();
		entities_matrix.add(new java.util.ArrayList<Entity>()); // layer 0

		EntityBuilder eb = new EntityBuilder();
		try {
			javax.xml.parsers.SAXParser saxParser = 
				javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse( new java.io.File("resources.xml"), new ResourcesHandler(eb) );

		} catch (Exception err) {
			err.printStackTrace();
		}
		Map<String,Entity> entities_map = eb.getResult();
		Entity e = entities_map.get("note0");
		e.setX(50); e.setY(50);
		entities_matrix.get(0).add(e);
		e = entities_map.get("note1");
		e.setX(79); e.setY(57);
		entities_matrix.get(0).add(e);
		e = entities_map.get("note2");
		e.setX(102); e.setY(64);
		entities_matrix.get(0).add(e);
		e = entities_map.get("note3");
		e.setX(131); e.setY(71);
		entities_matrix.get(0).add(e);
		e = entities_map.get("note4");
		e.setX(164); e.setY(78);
		entities_matrix.get(0).add(e);
		e = entities_map.get("note5");
		e.setX(193); e.setY(85);
		entities_matrix.get(0).add(e);
		e = entities_map.get("note6");
		e.setX(216); e.setY(92);
		entities_matrix.get(0).add(e);

		lastLoopTime = SystemTimer.getTime();
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

		Iterator<List<Entity>> i = entities_matrix.iterator();
		while(i.hasNext()) // loop over layers
		{
			 // get entity iterator from layer
			Iterator<Entity> j = i.next().iterator();
			while(j.hasNext()) // loop over entities
			{
				Entity e = j.next();
				e.move(delta); // move the entity
				if(!e.isAlive())j.remove(); // if dead, remove from list
				else e.draw(); // or draw itself on screen
			}
		}
		
		boolean skp = window.isKeyPressed(KeyEvent.VK_SPACE);

		if(skp && entities_matrix.size() == 0){
			entities_matrix.get(0).add(key_0.clone());
		}
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


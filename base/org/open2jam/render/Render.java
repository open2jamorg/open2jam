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

import org.open2jam.parser.ChartParser;
import org.open2jam.parser.Chart;
import org.open2jam.parser.Event;

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

	// map of entities to use, not directly, by cloning into the matrix
	Map<String,Entity> entities_map;

	// the chart being rendered
	Chart chart;

	double bpm;

	double hispeed = 4;

	private double viewport;
	private double measure_size;

	public Render(int renderingType, Chart c)
	{
		this.chart = c;
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
		viewport = 0.8 * window.getResolutionHeight();
		measure_size = 0.8 * hispeed * viewport;
		setBPM(chart.getHeader().getBPM());

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
		entities_map = eb.getResult();

		update_note_buffer(60000000); // warm up

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

		update_note_buffer(delta);

		Iterator<List<Entity>> i = entities_matrix.iterator();
		while(i.hasNext()) // loop over layers
		{
			 // get entity iterator from layer
			Iterator<Entity> j = i.next().iterator();
			while(j.hasNext()) // loop over entities
			{
				Entity e = j.next();
				e.move(delta); // move the entity

				if(e.getY() + e.getHeight() > viewport)e.judgment();

				if(!e.isAlive())j.remove(); // if dead, remove from list
				else e.draw(); // or draw itself on screen
			}
		}
		System.out.println(entities_matrix.get(0).size());
	}

	public void setBPM(double e)
	{ 
		this.bpm = e;
		note_speed = (bpm/240) * measure_size;
	}
	public double getBPM(){ return bpm; }
	public double getMeasureSize(){ return measure_size; }
	public double getViewPort() { return viewport; }

	public double getNoteSpeed(){ return note_speed; }

	private int buffered_measures = 0;

	private int last_measure = -1;
	private int last_measure_offset = 0;
	
	private double fractional_measure = 1;
	private double note_speed = (bpm/240) * measure_size;

	private double measure_passed = 0;


	private LongNoteEntity[] ln_buffer = new LongNoteEntity[7];

	private void update_note_buffer(long delta)
	{
		measure_passed += (note_speed * delta)/1000;
		if(measure_passed < measure_size/5)return;
		measure_passed = 0;
	
		List<Event> events = new java.util.ArrayList<Event>();
		while(chart.getEvents().size()>0 && chart.getEvents().get(0).getMeasure() == last_measure+1)
		{
			events.add( chart.getEvents().remove(0) );
		}

		fractional_measure = 1;

		for(Event e : events)
		{
			if(e.getChannel() > 8)continue;

			if(e.getChannel() == 0){
				fractional_measure = e.getValue();
				continue;
			}
			if(e.getChannel() == 1){
				double abs_height = last_measure_offset + (e.getPosition() * measure_size) + 1;
				entities_matrix.get(0).add(new BPMEntity(this,e.getValue(),viewport - abs_height));
				continue;
			}
			
			double abs_height = last_measure_offset + (e.getPosition() * measure_size);

			if(e.getType() == 0){
				NoteEntity ne = (NoteEntity) entities_map.get("note"+(e.getChannel()-2)).clone();

				ne.setX(2 + e.getChannel() * 32); 
				ne.setY(viewport - abs_height);
				ne.setRender(this);

				entities_matrix.get(0).add(ne);
			}
			else if(e.getType() == 2){
				LongNoteEntity ne = (LongNoteEntity) entities_map.get("long_note"+(e.getChannel()-2)).clone();
				ne.setX(2 + e.getChannel() * 32);
				ne.setY(viewport - abs_height);
				ne.setRender(this);
				ln_buffer[e.getChannel()-2] = ne;
				entities_matrix.get(0).add(ne);
			}
			else if(e.getType() == 3){
				ln_buffer[e.getChannel()-2].setHeight(viewport - abs_height - ln_buffer[e.getChannel()-2].getY());
				ln_buffer[e.getChannel()-2] = null;
			}
		}
		last_measure++;
		last_measure_offset += measure_size * fractional_measure;
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
		Chart c = ChartParser.parseFile(ChartParser.parseFileHeader(argv[0], 2));
		new Render(ResourceFactory.OPENGL_LWJGL, c);
	}
}


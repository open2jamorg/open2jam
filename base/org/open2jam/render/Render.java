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

	/** map of sprites to use */
	private Map<String,SpriteList> sprite_map;

	/** the chart being rendered */
	private Chart chart;

	/** the bpm at which the entities are falling */
	private double bpm;

	/** the hispeed */
	private final double hispeed = 1;

	/** the vertical space of the entities */
	private double viewport;

	/** the size of a measure */
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

		SpriteBuilder sb = new SpriteBuilder();
		try {
			javax.xml.parsers.SAXParser saxParser = 
				javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse( new java.io.File("resources.xml"), new ResourcesHandler(sb) );
		} catch (Exception err) {
			die(err);
		}
		sprite_map = sb.getResult();

		update_note_buffer(); // warm up
		update_note_buffer(); // warm up
		update_note_buffer(); // warm up

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

		update_note_buffer();

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
// 		System.out.println(last_measure_offset);
	}

	public void setBPM(double e)
	{ 
		this.bpm = e;
		note_speed = (bpm/240) * measure_size;
	}

	public void measureEnd()
	{
		buffered_measures--;
	}

	public double getBPM(){ return bpm; }
	public double getMeasureSize(){ return measure_size; }
	public double getViewPort() { return viewport; }

	public double getNoteSpeed(){ return note_speed; }

	private final int measure_buffer = 60;
	private int buffered_measures = 0;

	private int last_measure = -1;
	private int last_measure_offset = 0;
	
	private double fractional_measure = 1;
	private double note_speed = (bpm/240) * measure_size;


	private LongNoteEntity[] ln_buffer = new LongNoteEntity[7];

	private void update_note_buffer()
	{
		if(buffered_measures > measure_buffer)return;
		buffered_measures++;
	
		List<Event> events = new java.util.ArrayList<Event>();
		while(chart.getEvents().size()>0 && chart.getEvents().get(0).getMeasure() == last_measure+1)
		{
			events.add( chart.getEvents().remove(0) );
		}

		SpriteList sl = sprite_map.get("measure_mark");
		MeasureEntity me = new MeasureEntity(sl, 64, viewport - last_measure_offset);
		me.setRender(this);
		entities_matrix.get(0).add(me);

// 		System.out.println(events.size());

		fractional_measure = 1;

		for(Event e : events)
		{
			double abs_height = last_measure_offset + (e.getPosition() * measure_size);
			switch(e.getChannel())
			{
				case 0:
				fractional_measure = e.getValue();
				System.out.println(e.getValue());
				break;

				case 1:
				entities_matrix.get(0).add(new BPMEntity(this,e.getValue(),viewport - abs_height));
				break;

				case 2:case 3:case 4:
				case 5:case 6:case 7:case 8:
				if(e.getType() == 0){
					sl = sprite_map.get("note_head"+(e.getChannel()-2));
					NoteEntity ne = new NoteEntity(sl,e.getChannel() * 32, viewport - abs_height);
					ne.setRender(this);
					entities_matrix.get(0).add(ne);
				}
				else if(e.getType() == 2){
					SpriteList s_head = sprite_map.get("note_head"+(e.getChannel()-2));
					SpriteList s_body = sprite_map.get("note_body"+(e.getChannel()-2));
					LongNoteEntity ne = new LongNoteEntity(s_head, s_body,e.getChannel() * 32,viewport - abs_height);
					ne.setRender(this);
					ln_buffer[e.getChannel()-2] = ne;
					entities_matrix.get(0).add(ne);
				}
				else if(e.getType() == 3){
					ln_buffer[e.getChannel()-2].setHeight(viewport - abs_height - ln_buffer[e.getChannel()-2].getY());
					ln_buffer[e.getChannel()-2] = null;
				}
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

	public static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		final java.io.PrintWriter pw = new java.io.PrintWriter(r);
		e.printStackTrace(pw);
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}


	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		if(argv.length < 1)throw new RuntimeException("Need file to read !");
		Chart c = ChartParser.parseFile(ChartParser.parseFileHeader(argv[0], 2));
		new Render(ResourceFactory.OPENGL_LWJGL, c);
	}
}


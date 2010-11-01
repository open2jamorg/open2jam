package org.open2jam.render;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.lwjgl.opengl.DisplayMode;

import org.open2jam.parser.ResourcesHandler;
import org.open2jam.parser.Chart;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.BPMEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;

public class Render implements GameWindowCallback
{
	/** The window that is being used to render the game */
	private GameWindow window;

	/** The time at which the last rendering looped started from the point of view of the game logic */
	private long lastLoopTime;

	/** The time since the last record of fps */
	private long lastFpsTime = 0;

	/** The recorded fps */
	private int fps;

	/** the number of keys */
	private final int NUM_KEYS = 7;

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
	private final double hispeed;

	/** the vertical space of the entities */
	private double viewport;

	/** the size of a measure */
	private double measure_size;

	/** the size of the note buffer */
	private final int measure_buffer = 3;

	/** entity limit to buffer per frame */
	private final int buffer_per_frame = 10;

	private final int screen_x_offset = 30;

	/** pre-built offset of the notes horizontal pos */
	private int[] notes_x_offset = new int[NUM_KEYS];

	/** long note buffer */
	private LongNoteEntity[] ln_buffer = new LongNoteEntity[NUM_KEYS];

	/** the vertical speed of entities pixels/milliseconds */
	private double note_speed;

	/** the screen offset of the buffer */
	private double buffer_offset;

        static{
            ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
        }

	public Render(Chart c, double hispeed)
	{
		this.chart = c;
		this.hispeed = hispeed;
		window = ResourceFactory.get().getGameWindow();
	}
        
        public void setDisplay(DisplayMode dm, boolean vsync, boolean fs, boolean do_sync, int sync_cap) throws Exception{
            window.setDisplay(dm,vsync,fs);
            window.setSync(do_sync, sync_cap);
        }

        public void startRendering(){
            window.setGameWindowCallback(this);
            window.setTitle("Render");
            window.startRendering();
        }

	/**
	 * initialize the common elements for the game.
	 * this is called by the window render
	 */
	public void initialise()
	{
		viewport = 0.8 * window.getResolutionHeight();
		measure_size = 0.8 * hispeed * viewport;
		buffer_offset = viewport;
		setBPM(chart.getHeader().getBPM());

		entities_matrix = new ArrayList<List<Entity>>();
		entities_matrix.add(new ArrayList<Entity>()); // layer 0 -- measure marks
		entities_matrix.add(new ArrayList<Entity>()); // layer 1 -- notes

		SpriteBuilder sb = new SpriteBuilder();
		try {
			javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().parse(
				Render.class.getResourceAsStream("/resources/resources.xml"),
				new ResourcesHandler(sb)
			);
		} catch (Exception e) {
			die(e);
		}
		sprite_map = sb.getResult();

		// build the notes horizontal offset
		int off = screen_x_offset;
		for(int i=0;i<NUM_KEYS;i++)
		{
			notes_x_offset[i] = off;
			off += sprite_map.get("note_head"+i).get(0).getWidth();
		}

		 // load up initial buffer
		while(buffered_measures < measure_buffer)update_note_buffer(0);

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
			window.setTitle("Render (FPS: "+fps+")");
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

				if(e.getBounds().getY() + e.getBounds().getHeight() > viewport)e.judgment();
				if(!e.isAlive())j.remove(); // if dead, remove from list
				else e.draw(); // or draw itself on screen
			}
		}
                if(chart.getEvents().isEmpty() && entities_matrix.get(1).isEmpty()){
                    window.destroy();
                    return;
                }
	}

	public void setBPM(double e)
	{
		this.bpm = e;
		note_speed = ((bpm/240) * measure_size) / 1000;
	}

	/** callback for the measure judgment */
	public void measureEnd()
	{
		buffered_measures--;
	}

	/** returns the note speed in pixels/millisecs */
	public double getNoteSpeed() { return note_speed; }

	public double getBPM() { return bpm; }
	public double getMeasureSize() { return measure_size; }
	public double getViewPort() { return viewport; }
	

	private int buffered_measures = 0;

	private int buffer_measure = -1;

	private boolean measure_change = true;
	
	private double fractional_measure = 1;

	/** update the note layer of the entities_matrix.
	*** note buffering is equally distributed between the frames
	**/
	private void update_note_buffer(long delta)
	{
		buffer_offset += note_speed * delta;
		if(buffered_measures > measure_buffer)return;

		if(measure_change) // this is a new measure
		{
			buffer_measure++;
			buffer_offset -= measure_size * fractional_measure;
			entities_matrix.get(0).add(
				new MeasureEntity(this,
				sprite_map.get("measure_mark"),
				screen_x_offset, buffer_offset+6)
			);
			fractional_measure = 1;
			measure_change = false;
		}

		int counter = 0; // how much events we processed in this call
		Iterator<Event> c = chart.getEvents().iterator();
		while(c.hasNext() && counter++ < buffer_per_frame)
		{
			Event e = c.next();
			if(e.getMeasure() > buffer_measure) // this is the start of a new measure
			{                                 // we can't buffer that yet so let's stop here
				measure_change = true;
				buffered_measures++;
				break;
			}

			double abs_height = buffer_offset - (e.getPosition() * measure_size);
			switch(e.getChannel())
			{
				case 0:
				fractional_measure = e.getValue();
				break;

				case 1:
				entities_matrix.get(0).add(new BPMEntity(this,e.getValue(),abs_height));
				break;

				case 2:case 3:case 4:
				case 5:case 6:case 7:case 8:
				int note_number = e.getChannel()-2;
				if(e.getType() == 0){
					entities_matrix.get(1).add(
						new NoteEntity(this, sprite_map.get("note_head"+note_number),
						notes_x_offset[note_number],
						abs_height));
				}
				else if(e.getType() == 2){
					ln_buffer[note_number] = 
						new LongNoteEntity(this,
						sprite_map.get("note_head"+note_number),
						sprite_map.get("note_body"+note_number), 
						notes_x_offset[note_number],
						abs_height);
					entities_matrix.get(1).add(ln_buffer[note_number]);
				}
				else if(e.getType() == 3){
					ln_buffer[note_number].
					setHeight(abs_height-ln_buffer[note_number].getBounds().getY());
					ln_buffer[note_number] = null;
				}
			}
			c.remove(); // once the event is in the buffer we remove it from the list
		}
	}

	/**
	 * Notifcation that the game window has been closed
	 */
	public void windowClosed() {
		
	}

	public static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		e.printStackTrace(new java.io.PrintWriter(r));
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}


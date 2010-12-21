package org.open2jam.render;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.lwjgl.opengl.DisplayMode;
import org.open2jam.Config;
import org.open2jam.util.SystemTimer;

import org.open2jam.parser.Chart;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.BPMEntity;
import org.open2jam.render.entities.ComboCounterEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.JudgmentEntity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;
import org.open2jam.render.entities.NumberEntity;
import org.open2jam.render.entities.SampleEntity;
import org.open2jam.render.lwjgl.SoundManager;


public class Render implements GameWindowCallback
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /** store the sound sources being played */
    private static final int MAX_SOURCES = 64;

    /** the config xml */
    private static final URL resources_xml = Render.class.getResource("/resources/resources.xml");

    /** the mapping of note channels to KeyEvent keys  */
    private static final EnumMap<Event.Channel, Integer> keyboard_map;

    /** The window that is being used to render the game */
    private final GameWindow window;

    /** the chart being rendered */
    private final Chart chart;

    /** the hispeed */
    private final double hispeed;

    /** skin info and entities */
    private Skin skin;

    /** the size of a measure */
    private double measure_size;

    /** the bpm at which the entities are falling */
    private double bpm;

    /** the vertical speed of entities pixels/milliseconds */
    private double note_speed;

    /** the screen offset of the buffer */
    private double buffer_offset;

    /** the note layer, to check when it's empty */
    private int note_layer;

    /** The recorded fps */
    private int fps;

    /** The time at which the last rendering looped started from the point of view of the game logic */
    private long lastLoopTime;

    /** The time since the last record of fps */
    private long lastFpsTime = 0;

    /** defines the judgment space */
    private double judgment_line_y1, judgment_line_y2;

    /** maps the Event value to OpenGL sample ID's */
    private Map<Integer, Integer> samples;

    /** a list of list of entities.
    ** basically, each list is a layer of entities
    ** the layers are rendered in order
    ** so entities at layer X will always be rendered before layer X+1 */
    private EntityMatrix entities_matrix;

    /** this iterator is used by the update_note_buffer
     * to go through the events on the chart */
    private Iterator<Event> buffer_iterator;

    /** this is used by the update_note_buffer
     * to remember the "opened" long-notes */
    private EnumMap<Event.Channel, LongNoteEntity> ln_buffer;

    /** this holds the actual state of the keyboard,
     * whether each is being pressed or not */
    private EnumMap<Event.Channel,Boolean> keyboard_key_pressed;

    /** these are the same notes from the entity_matrix
     * but divided in channels for ease to pull */
    private EnumMap<Event.Channel,LinkedList<NoteEntity>> note_channels;

    /** entities for the key pressed events
     * need to keep track of then to kill
     * when the key is released */
    private EnumMap<Event.Channel,Entity> key_pressed_entity;

    /** keep track of the long note the player may be
     * holding with the key */
    private EnumMap<Event.Channel,LongNoteEntity> longnote_holded;

    /** keep trap of the last sound of each channel
     * so that the player can re-play the sound when the key is pressed */
    private EnumMap<Event.Channel,Event.SoundSample> last_sound;

    /** this queue hold the available sources
     * that may be used to play sounds */
    private ArrayDeque<Integer> source_queue;

    /** this list hold the sources that are playing
     * sounds at the moment */
    private LinkedList<Integer> sources_playing;

    /** number to display the fps, and note counters on the screen */
    private NumberEntity fps_entity;
    private HashMap<String,NumberEntity> note_counter;

    private JudgmentEntity judgment_entity;

    /** the combo counter */
    private ComboCounterEntity combo_entity;

    static{
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
        keyboard_map = Config.get().getKeyboardMap();
    }

    public Render(Chart c, double hispeed)
    {
        this.chart = c;
        this.hispeed = hispeed;
        window = ResourceFactory.get().getGameWindow();
    }
        
    public void setDisplay(DisplayMode dm, boolean vsync, boolean fs) {
        window.setDisplay(dm,vsync,fs);
    }

    public void startRendering(){
        window.setGameWindowCallback(this);
        window.setTitle(chart.getArtist()+" - "+chart.getTitle());

        try{
            window.startRendering();
        }catch(OutOfMemoryError e) {
            System.gc();
            JOptionPane.showMessageDialog(null, "Fatal Error", "System out of memory ! baillin out !!",JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "System out of memory ! baillin out !!{0}", e.getMessage());
            System.exit(1);
        }
    }

    /**
    * initialize the common elements for the game.
    * this is called by the window render
    */
    public void initialise()
    {
        try{
            BufferedImage img = chart.getCover();
            ResourceFactory.get().getSprite(img).draw(0,0);
            window.update();
        } catch (NullPointerException e){
            logger.log(Level.INFO, "No cover image on file: {0}", chart.getSource().getName());
        }
        lastLoopTime = SystemTimer.getTime();
        
        System.gc();

        try {
            SkinHandler sb = new SkinHandler(this,"o2jam");
            SAXParserFactory.newInstance().newSAXParser().parse(resources_xml.openStream(), sb);
            skin = sb.getResult();
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (org.xml.sax.SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (java.io.IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        judgment_line_y1 = skin.judgment.start;
        judgment_line_y2 = skin.judgment.start + skin.judgment.size;

        if(hispeed > 1){
            double off = skin.judgment.size * (hispeed-1);
            judgment_line_y1 -= off;
//            judgment_line_y2 += off;
        }

        entities_matrix = new EntityMatrix(skin.max_layer+1);

        bpm = chart.getBPM();
	measure_size = 0.8 * hispeed * getViewport();
        buffer_offset = getViewport();

        update_note_speed();

        note_layer = skin.getEntityMap().get("NOTE_1").getLayer();

        // adding static entities
        for(Entity e : skin.getEntityList()){
            entities_matrix.add(e);
        }

        note_counter = new HashMap<String,NumberEntity>();
        int off = 0;
        for(String s : skin.judgment.getRates()){
            NumberEntity e = (NumberEntity)skin.getEntityMap().get("FPS_COUNTER").copy();
            note_counter.put(s, e);
            e.setPos(500, off+=100);
        }

        // build long note buffer
        ln_buffer = new EnumMap<Event.Channel,LongNoteEntity>(Event.Channel.class);

        // the notes pressed buffer
        keyboard_key_pressed = new EnumMap<Event.Channel,Boolean>(Event.Channel.class);

        // reference to the notes in the buffer, separated by the channel
        note_channels = new EnumMap<Event.Channel,LinkedList<NoteEntity>>(Event.Channel.class);

        // entity for key pressed events
        key_pressed_entity = new EnumMap<Event.Channel,Entity>(Event.Channel.class);

        // reference to long notes being holded
        longnote_holded = new EnumMap<Event.Channel,LongNoteEntity>(Event.Channel.class);

        last_sound = new EnumMap<Event.Channel,Event.SoundSample>(Event.Channel.class);

        fps_entity = (NumberEntity) skin.getEntityMap().get("FPS_COUNTER");
        entities_matrix.add(fps_entity);

        combo_entity = (ComboCounterEntity) skin.getEntityMap().get("COMBO_COUNTER");
        entities_matrix.add(combo_entity);

        for(Event.Channel c : keyboard_map.keySet())
        {
            keyboard_key_pressed.put(c, Boolean.FALSE);
            note_channels.put(c, new LinkedList<NoteEntity>());
        }

        // load up initial buffer
        buffer_iterator = chart.getEvents().iterator();
        update_note_buffer();


        // create sound sources
        source_queue = new ArrayDeque<Integer>(MAX_SOURCES);
        sources_playing = new LinkedList<Integer>();

        try{
            for(int i=0;i<MAX_SOURCES;i++)
                source_queue.push(SoundManager.newSource()); // creates sources
        }catch(org.lwjgl.openal.OpenALException e){
            logger.log(Level.WARNING, "Couldn''t create enough sources({0})", MAX_SOURCES);
        }

        // get the chart sound samples
        samples = chart.getSamples();

        //clean up
        System.gc();

        // wait a bit.. 5 seconds at min
        SystemTimer.sleep((int) (5000 - (SystemTimer.getTime() - lastLoopTime)));

        lastLoopTime = SystemTimer.getTime();
    }

    
    /**
    * Notification that a frame is being rendered. Responsible for
    * running game logic and rendering the scene.
    */
    public void frameRendering()
    {
        // work out how long its been since the last update, this
        // will be used to calculate how far the entities should
        // move this loop
        long delta = SystemTimer.getTime() - lastLoopTime;
        lastLoopTime = SystemTimer.getTime();
        lastFpsTime += delta;
        fps++;
        
        // update our FPS counter if a second has passed
        if (lastFpsTime >= 1000) {
            logger.log(Level.FINEST, "FPS: {0}", fps);
            fps_entity.setNumber(fps);
            lastFpsTime = 0;
            fps = 0;
        }

        check_keyboard();

        check_sources();
        
        update_note_buffer();

        Iterator<LinkedList<Entity>> i = entities_matrix.iterator();

        while(i.hasNext()) // loop over layers
        {
            // get entity iterator from layer
            Iterator<Entity> j = i.next().iterator();
            while(j.hasNext()) // loop over entities
            {
                Entity e = j.next();
                e.move(delta); // move the entity

                if(e instanceof NoteEntity) // if it's a note
                {
		    NoteEntity ne = (NoteEntity) e;

		    String judge = skin.judgment.ratePrecision(ne.getHit());

		    switch (ne.getState())
		    {
			case NOT_PLAYED: //you missed it (no keyboard input)
			    if((ne instanceof LongNoteEntity) &&
				    ne.isAlive() && ne.getStartY() >= skin.judgment.start+skin.judgment.size*2)
			    {
				if(judgment_entity != null)judgment_entity.setAlive(false);
				judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
				entities_matrix.add(judgment_entity);

				combo_entity.resetNumber();

				ne.setState(NoteEntity.State.KILL);
			    }
			    else if(ne.isAlive() && ne.getY() >= skin.judgment.start+skin.judgment.size*2)
			    {
				if(judgment_entity != null)judgment_entity.setAlive(false);
				judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
				entities_matrix.add(judgment_entity);

				combo_entity.resetNumber();

				ne.setState(NoteEntity.State.KILL);
			    }
			break;
			case LN_HEAD_PLAYED: //LN: Head has been played
			if(ne.getHit() > 0)
			    {
				if(judgment_entity != null)judgment_entity.setAlive(false);
				judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
				entities_matrix.add(judgment_entity);

				note_counter.get(judge).incNumber();
				combo_entity.incNumber();

				ne.setState(NoteEntity.State.LN_HOLD);
			    }
			    else
			    {
				if(judgment_entity != null)judgment_entity.setAlive(false);
				judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
				entities_matrix.add(judgment_entity);

				combo_entity.resetNumber();

				ne.setState(NoteEntity.State.KILL);
			    }
			break;
			case JUDGE: //LN & normal ones: has finished with good result
			if(ne.getHit() > 0)
			{
			    if(judgment_entity != null)judgment_entity.setAlive(false);
			    judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
			    entities_matrix.add(judgment_entity);

			    note_counter.get(judge).incNumber();
			    combo_entity.incNumber();
			}
			else
			{
			    if(judgment_entity != null)judgment_entity.setAlive(false);
			    judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
			    entities_matrix.add(judgment_entity);

			    combo_entity.resetNumber();

			    ne.setState(NoteEntity.State.KILL);
			}
			break;
			case KILL: //KILL THEM!
			if(ne.isAlive() && ne.getY() >= window.getResolutionHeight())
			{
			    // kill it
			    ne.setAlive(false);
			    note_channels.get(ne.getChannel()).removeFirst();
			    last_sound.put(ne.getChannel(), ne.getSample());
			}
			break;
			case LN_HOLD:
			//Do nothing, the LN is being holded
			break;
		    }
		}
                else if(e.getY() >= skin.judgment.start+skin.judgment.size){ // else, if it's on the line, judge it
                    e.judgment();
                }

                if(!e.isAlive())j.remove();
                else 
                if(!(e instanceof NoteEntity) || e.getY() < skin.judgment.start+skin.judgment.size)e.draw();
            }
        }
        buffer_offset += note_speed * delta; // walk with the buffer

        update_note_speed(); // speed will change if the bpm changed in this frame
        
        if(!buffer_iterator.hasNext() && entities_matrix.isEmpty(note_layer)){
            if(sources_playing.isEmpty()){
                window.destroy();
                return;
            }
            else{
                last_sound.clear();
            }
        }
    }

    public void setBPM(double e)
    {
        this.bpm = e;
    }

    private void update_note_speed(){
        note_speed = ((bpm/240) * measure_size) / 1000.0d;
    }

    public double judgmentArea()
    {
        return hispeed * skin.judgment.size;
    }

    /** returns the note speed in pixels/milliseconds */
    public double getNoteSpeed() { return note_speed; }

    public double getMeasureSize() { return measure_size; }
    public double getViewport() { return skin.judgment.start+skin.judgment.size; }


    private void check_keyboard()
    {
        for(Map.Entry<Event.Channel,Integer> entry : keyboard_map.entrySet())
        {
            Event.Channel c = entry.getKey();
            if(window.isKeyDown(entry.getValue())) // this key is being pressed
            {
                if(keyboard_key_pressed.get(c) == false){ // started holding now
                    keyboard_key_pressed.put(c, true);

                    Entity ee = skin.getEntityMap().get("PRESSED_"+c).copy();
                    entities_matrix.add(ee);
                    key_pressed_entity.put(c, ee);

                    if(note_channels.get(c).isEmpty()){
                        Event.SoundSample i = last_sound.get(c);
                        if(i != null)queueSample(i);
                        continue;
                    }

                    NoteEntity e = note_channels.get(c).getFirst();

                    if(e.getState() != NoteEntity.State.KILL)
			queueSample(e.getSample());

                    double hit = e.testHit(judgment_line_y1, judgment_line_y2);

		    e.setHit(hit);
		    if(e instanceof LongNoteEntity)
		    {
			if(e.getState() == NoteEntity.State.NOT_PLAYED)
			    e.setState(NoteEntity.State.LN_HEAD_PLAYED);
		    }
		    else
			e.setState(NoteEntity.State.JUDGE);

                    if(hit > 0){
                        if(e instanceof LongNoteEntity){
			    if(e.getState() != NoteEntity.State.KILL)
			    {
				longnote_holded.put(c, (LongNoteEntity) e);
				//longnote flare effect
				ee = skin.getEntityMap().get("EFFECT_LONGFLARE").copy();
				ee.setPos(e.getX()+e.getWidth()/2-ee.getWidth()/2,
					getViewport()-ee.getHeight()/2);
				entities_matrix.add(ee);
			    }
                        }else{
                            e.setAlive(false);
                            note_channels.get(c).removeFirst();
                            last_sound.put(c, e.getSample());
                        }

			if(e.getState() != NoteEntity.State.KILL)
			{
			    ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
			    ee.setPos(e.getX()+e.getWidth()/2-ee.getWidth()/2,
				getViewport()-ee.getHeight()/2);
			    entities_matrix.add(ee);
			}
		    }
                }
            }
            else
            if(keyboard_key_pressed.get(c) == true) { // key released now

                keyboard_key_pressed.put(c, false);
                key_pressed_entity.get(c).setAlive(false);

                LongNoteEntity e = longnote_holded.get(c);
		longnote_holded.put(c,null);

                if(e == null || note_channels.get(c).isEmpty()
                        || e != note_channels.get(c).getFirst())continue;

                double hit = e.testHit(judgment_line_y1, judgment_line_y2);

		e.setHit(hit);
		if(e.getState() == NoteEntity.State.KILL)
		    e.setState(NoteEntity.State.KILL);
		else
		    e.setState(NoteEntity.State.JUDGE);
		if(hit > 0){
                    e.setAlive(false);
                    note_channels.get(c).removeFirst();
                    last_sound.put(c, e.getSample());

		    if(e.getState() != NoteEntity.State.KILL)
		    {
			Entity ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
			ee.setPos(e.getX()+e.getWidth()/2-ee.getWidth()/2,
			    getViewport()-ee.getHeight()/2);
			entities_matrix.add(ee);
		    }
		}
            }
        }
    } 

    private int buffer_measure = -1;

    private double fractional_measure = 1;

    private final int buffer_upper_bound = -10;

    /** update the note layer of the entities_matrix.
    *** note buffering is equally distributed between the frames
    **/
    private void update_note_buffer()
    {
        while(buffer_iterator.hasNext() && buffer_offset > buffer_upper_bound)
        {
            Event e = buffer_iterator.next();

            while(e.getMeasure() > buffer_measure) // this is the start of a new measure
            {
                buffer_offset -= measure_size * fractional_measure;
                MeasureEntity m = (MeasureEntity) skin.getEntityMap().get("MEASURE_MARK").copy();
                m.setPos(m.getX(), buffer_offset+6);
                entities_matrix.add(m);
                buffer_measure++;
                fractional_measure = 1;
            }

            double abs_height = buffer_offset - (e.getPosition() * measure_size);
            switch(e.getChannel())
            {
                case TIME_SIGNATURE:
                fractional_measure = e.getValue();
                break;

                case BPM_CHANGE:
                entities_matrix.add(new BPMEntity(this,e.getValue(),abs_height));
                break;

                case NOTE_1:case NOTE_2:
                case NOTE_3:case NOTE_4:
                case NOTE_5:case NOTE_6:case NOTE_7:
                if(e.getFlag() == Event.Flag.NONE){
                    NoteEntity n = (NoteEntity) skin.getEntityMap().get(e.getChannel().toString()).copy();
                    n.setPos(n.getX(), abs_height);
                    n.setSample(e.getSample());
                    entities_matrix.add(n);
                    note_channels.get(n.getChannel()).add(n);
                }
                else if(e.getFlag() == Event.Flag.HOLD){
                    LongNoteEntity ln = (LongNoteEntity) skin.getEntityMap().get("LONG_"+e.getChannel()).copy();
                    ln.setPos(ln.getX(), abs_height);
                    ln.setSample(e.getSample());
                    ln_buffer.put(e.getChannel(),ln);
                    entities_matrix.add(ln);
                    note_channels.get(ln.getChannel()).add(ln);
                }
                else if(e.getFlag() == Event.Flag.RELEASE){
                    if(ln_buffer.get(e.getChannel()) == null){
                        logger.log(Level.WARNING, "Attempted to RELEASE note {0}", e.getChannel());
                    }else{
                        ln_buffer.get(e.getChannel()).setEndY(abs_height);
                        ln_buffer.remove(e.getChannel());
                    }
                }
                break;
                
                case AUTO_PLAY:
		case NOTE_SC:
                entities_matrix.add(new SampleEntity(this,e.getSample(),abs_height));
                break;
            }
        }
    }

    /**
     * Notification that the game window has been closed
     */
    public void windowClosed() {
        SoundManager.killData();
    }

    public void queueSample(Event.SoundSample sample)
    {
        Integer buffer = samples.get(sample.sample_id);
        if(buffer == null)return;
        if(source_queue.isEmpty()){
            logger.warning("Source queue exausted !");
            return;
        }
        Integer source = source_queue.pollFirst();
        SoundManager.setGain(source, sample.volume);
        SoundManager.setPan(source, sample.pan);
        SoundManager.play(source, buffer);
        sources_playing.add(source);
    }

    public void stopSample(int source)
    {
	SoundManager.stop(source);
    }

    private void check_sources()
    {
        Iterator<Integer> it = sources_playing.iterator();
        while(it.hasNext())
        {
            Integer i = it.next();
            if(!SoundManager.isPlaying(i)){
                it.remove();
                source_queue.addLast(i);
            }
        }
    }
}


package org.open2jam.render;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.lwjgl.opengl.DisplayMode;
import org.open2jam.Config;
import org.open2jam.parser.Event.Channel;
import org.open2jam.util.SystemTimer;

import org.open2jam.parser.Chart;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.AnimatedEntity;
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

    private final double AUTOPLAY_THRESHOLD = 0.8;
    
    /** is autoplaying ? */
    private final boolean AUTOPLAY;

    /** the hispeed */
    private double hispeed;
    private boolean updateHS = false;

    /** the channelMirror, random select */
    private int channelModifier = 0;

    /** will keep the aspect ratio of the skin */
    private boolean aspect_ratio = false;

    /** the visibility modifier */
    private int visibilityModifier = 0;

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

    /** miss judge from the skin */
    private String MISS_JUDGE;

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

    private EnumMap<Event.Channel,AnimatedEntity> longflare;

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
    private LinkedList<Integer> source_queue;
    private Iterator<Integer> source_queue_iterator;

    /** number to display the fps, and note counters on the screen */
    private NumberEntity fps_entity;
    private HashMap<String,NumberEntity> note_counter;
    private NumberEntity ranking_entity;
    /** JamCombo variables */
    private ComboCounterEntity jamcombo_entity;
    /**
     * Cools: +2
     * Goods: +1
     * Everything else: reset to 0
     * >=50 to add a jam
     */
    int jamcombo_counter; 

    private JudgmentEntity judgment_entity;

    /** the combo counter */
    private ComboCounterEntity combo_entity;

    static{
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
        keyboard_map = Config.get().getKeyboardMap();
    }

    public Render(Chart c, double hispeed, boolean autoplay, int channelModifier, int visibilityModifier)
    {
        this.chart = c;
        this.hispeed = hispeed;
	this.AUTOPLAY = autoplay;
	this.channelModifier = channelModifier;
        this.visibilityModifier = visibilityModifier;
        window = ResourceFactory.get().getGameWindow();
    }
        
    public void setDisplay(DisplayMode dm, boolean vsync, boolean fs, boolean aspect_ratio) {
        this.aspect_ratio = aspect_ratio;
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
        lastLoopTime = SystemTimer.getTime();

        // skin load
        try {
            SkinHandler sb = new SkinHandler(this,"o2jam", window.getResolutionWidth(), window.getResolutionHeight());
            SAXParserFactory.newInstance().newSAXParser().parse(resources_xml.openStream(), sb);
            skin = sb.getResult();
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (org.xml.sax.SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (java.io.IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        //scale
        window.setScreenScale(skin.screen_scale_x, skin.screen_scale_y, aspect_ratio);
        window.update();

        // cover image load
        try{
            BufferedImage img = chart.getCover();
            Sprite s = ResourceFactory.get().getSprite(img);
            s.setScale(skin.screen_scale_x,skin.screen_scale_y);
            s.draw(0, 0);
            window.update();
        } catch (NullPointerException e){
            logger.log(Level.INFO, "No cover image on file: {0}", chart.getSource().getName());
        }

        MISS_JUDGE = skin.judgment.ratePrecision(0);

        judgment_line_y2 = skin.judgment.start + skin.judgment.size;
	updateHispeed();

        entities_matrix = new EntityMatrix(skin.max_layer+1);

        bpm = chart.getBPM();
        buffer_offset = getViewport();
        
        update_note_speed();

        note_layer = skin.getEntityMap().get("NOTE_1").getLayer();

        // adding static entities
        for(Entity e : skin.getEntityList()){
            entities_matrix.add(e);
        }

        note_counter = new HashMap<String,NumberEntity>();
        for(String s : skin.judgment.getRates()){
            NumberEntity e = (NumberEntity)skin.getEntityMap().get("COUNTER_"+s).copy();
            note_counter.put(s, e);
            e.setPos(e.getX(), e.getY());
	    entities_matrix.add(note_counter.get(s));
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

	longflare = new EnumMap<Event.Channel, AnimatedEntity> (Event.Channel.class);

        last_sound = new EnumMap<Event.Channel,Event.SoundSample>(Event.Channel.class);

        fps_entity = (NumberEntity) skin.getEntityMap().get("FPS_COUNTER");
        entities_matrix.add(fps_entity);

        ranking_entity = (NumberEntity) skin.getEntityMap().get("COUNTER_RANKING");
        entities_matrix.add(ranking_entity);

        /**
         * TODO It's a combo counter, but because our combo counter substract 1 when it draws
         * the real number and the drawed number are different
         */
        jamcombo_entity = (ComboCounterEntity) skin.getEntityMap().get("COUNTER_JAM");
        entities_matrix.add(jamcombo_entity);
        
        combo_entity = (ComboCounterEntity) skin.getEntityMap().get("COMBO_COUNTER");
        entities_matrix.add(combo_entity);

        for(Event.Channel c : keyboard_map.keySet())
        {
            keyboard_key_pressed.put(c, Boolean.FALSE);
            note_channels.put(c, new LinkedList<NoteEntity>());
        }


        List<Event> event_list = chart.getEvents();
        buffer_iterator = event_list.iterator();
	
	/**Let's randomize "-"
	 * I don't know any better implementation so...
	 */
	if(channelModifier != 0)
	{
	    if(channelModifier == 1)
		channelMirror(buffer_iterator);
	    if(channelModifier == 2)
		channelShuffle(buffer_iterator);
	    if(channelModifier == 3)
		channelRandom(buffer_iterator);

            // get a new iterator
            buffer_iterator = event_list.iterator();
	}
        
        // load up initial buffer
        update_note_buffer();


        // create sound sources
        source_queue = new LinkedList<Integer>();

        try{
            for(int i=0;i<MAX_SOURCES;i++)
                source_queue.push(SoundManager.newSource()); // creates sources
        }catch(org.lwjgl.openal.OpenALException e){
            logger.log(Level.WARNING, "Couldn''t create enough sources({0})", MAX_SOURCES);
        }

        source_queue_iterator = source_queue.iterator();

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

	if(AUTOPLAY)do_autoplay();
        else check_keyboard();

        if(updateHS)updateHispeed();
        update_note_speed(); // speed will change if the bpm changed in the last frame
        
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
		    check_judgment((NoteEntity)e);
                } 
		else if (e.getY() >= getViewport()) // else, if it's on the line, judge it
		{
                    e.judgment();
                }

                if(!e.isAlive())j.remove();
                else 
                if(!(e instanceof NoteEntity) || e.getY() < getViewport())e.draw(); // TODO: not sure the IF is needed
            }
        }

        buffer_offset += note_speed * delta; // walk with the buffer

        if(!buffer_iterator.hasNext() && entities_matrix.isEmpty(note_layer)){
            for(Integer source : source_queue)
            {
                // this source is still playing, remove the sounds from the player
                if(SoundManager.isPlaying(source)){
                    last_sound.clear();
                    return;
                }
            }
            // all sources have finished playing
            window.destroy();
        }
    }

    public void setBPM(double e)
    {
        this.bpm = e;
    }

    private void update_note_speed(){
        note_speed = ((bpm/240) * measure_size) / 1000.0d;
    }
    
    private double judgmentArea()
    {
        //return judgment_line_y2;
	return judgment_line_y1 + (hispeed * skin.judgment.size * 2);
    }

    private void updateHispeed()
    {
        judgment_line_y1 = skin.judgment.start;
        if(hispeed > 1){
            double off = skin.judgment.size * (hispeed-1);
            judgment_line_y1 -= off;
        }

        measure_size = 0.8 * hispeed * getViewport();

	updateHS = false;
    }

    /** returns the note speed in pixels/milliseconds */
    public double getNoteSpeed() { return note_speed; }

    public double getMeasureSize() { return measure_size; }
    public double getViewport() { return skin.judgment.start+skin.judgment.size; }

    private int computeRanking(String judge)
    {
        if(judge.equals("JUDGMENT_COOL"))
        {
            /**
             * Your current jam combo also affects the score you get for each Cool hit.
             * For each jam combo, the score is increased by 10.
             * If you fail to hit a note, the jam combo will be reset to 0 and also the score/cool to 200.
             * http://o2jam.wikia.com/wiki/Jam_combo
             */
            return 200 + (jamcombo_entity.getNumber()*10);
        }
        else if(judge.equals("JUDGMENT_GOOD"))
        {
            return 128; //i don't know :/
        }
        else
            return 0;
    }

    private void check_judgment(NoteEntity ne)
    {
        String judge;
        switch (ne.getState())
        {
            case LN_HEAD_JUDGE: //LN: Head has been played
                judge = skin.judgment.ratePrecision(ne.getHit());
                if(judgment_entity != null)judgment_entity.setAlive(false);
                judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
                entities_matrix.add(judgment_entity);

		note_counter.get(judge).incNumber();
                ranking_entity.addNumber(computeRanking(judge));
                if(judge.equals("JUDGMENT_COOL"))
                    jamcombo_counter += 2;
                else if(judge.equals("JUDGMENT_GOOD"))
                    jamcombo_counter++;
                else
                {
                    jamcombo_counter = 0;
                    jamcombo_entity.resetNumber();
                }
		if(ne.getHit() > 0)
                {
		    Entity ee = skin.getEntityMap().get("EFFECT_LONGFLARE").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,ee.getY());
		    entities_matrix.add(ee);
                    longflare.put(ne.getChannel(),(AnimatedEntity) ee);
		    
		    ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,
		    getViewport()-ee.getHeight()/2);
		    entities_matrix.add(ee);

		    if(ne.getHit() >= skin.judgment.combo_threshold)combo_entity.incNumber();
		    else combo_entity.resetNumber();
                    ne.setState(NoteEntity.State.LN_HOLD);
                }else
                { // TODO: this else will ever be executed ???
                    combo_entity.resetNumber();
                    note_channels.get(ne.getChannel()).removeFirst();
                    ne.setState(NoteEntity.State.TO_KILL);
                }
                last_sound.put(ne.getChannel(), ne.getSample());
            break;
            case JUDGE: //LN & normal ones: has finished with good result
                judge = skin.judgment.ratePrecision(ne.getHit());
                if(judgment_entity != null)judgment_entity.setAlive(false);
                judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+judge).copy();
                entities_matrix.add(judgment_entity);

		note_counter.get(judge).incNumber();
                ranking_entity.addNumber(computeRanking(judge));
                if(judge.equals("JUDGMENT_COOL"))
                    jamcombo_counter += 2;
                else if(judge.equals("JUDGMENT_GOOD"))
                    jamcombo_counter++;
                else
                {
                    jamcombo_counter = 0;
                    jamcombo_entity.resetNumber();
                }
		if(ne.getHit() > 0) // TODO: compare with MISS ?
                {
		    Entity ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,
		    getViewport()-ee.getHeight()/2);
		    entities_matrix.add(ee);

		    if(ne.getHit() >= skin.judgment.combo_threshold)combo_entity.incNumber();
		    else combo_entity.resetNumber();

                    ne.setAlive(false);
                } else {
                    combo_entity.resetNumber();
                    ne.setState(NoteEntity.State.TO_KILL);
                }
                last_sound.put(ne.getChannel(), ne.getSample());
                note_channels.get(ne.getChannel()).removeFirst();
            break;
            case TO_KILL: // this is the "garbage collector", it just removes the notes off window
                if(ne.isAlive() && ne.getY() >= window.getResolutionHeight())
                {
                    // kill it
                    ne.setAlive(false);
                }
            break;
            case NOT_JUDGED: // you missed it (no keyboard input)
                if(ne.isAlive() 
                        && ((ne instanceof LongNoteEntity && ne.getStartY() >= judgmentArea()) //needed by the ln head
                        || (ne.getY() >= judgmentArea())))
                {
                    if(judgment_entity != null)judgment_entity.setAlive(false);
                    judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+MISS_JUDGE).copy();
                    entities_matrix.add(judgment_entity);

                    note_counter.get(MISS_JUDGE).incNumber();
                    combo_entity.resetNumber();
                    note_channels.get(ne.getChannel()).removeFirst();
                    ne.setState(NoteEntity.State.TO_KILL);
                 }
            break;
            case LN_HOLD:    // You keept too much time the note held that it misses
                if(ne.isAlive() && ne.getY() >= judgmentArea())
                {
                    if(judgment_entity != null)judgment_entity.setAlive(false);
                    judgment_entity = (JudgmentEntity) skin.getEntityMap().get("EFFECT_"+MISS_JUDGE).copy();
                    entities_matrix.add(judgment_entity);

                    note_counter.get(MISS_JUDGE).incNumber();
                    combo_entity.resetNumber();
                    note_channels.get(ne.getChannel()).removeFirst();
                    ne.setState(NoteEntity.State.TO_KILL);
                 }
            break;
        }

        if(jamcombo_counter >= 50)
        {
            jamcombo_counter = 0; //reset
            jamcombo_entity.incNumber();
        }
    }

    private void do_autoplay()
    {
        for(Map.Entry<Event.Channel,Integer> entry : keyboard_map.entrySet())
        {
            Event.Channel c = entry.getKey();
            if(note_channels.get(c).isEmpty())continue;

            NoteEntity ne = note_channels.get(c).getFirst();

            if(ne.getState() != NoteEntity.State.NOT_JUDGED &&
                    ne.getState() != NoteEntity.State.LN_HOLD)continue;

            double hit = ne.testHit(judgment_line_y1, judgment_line_y2);
            if(hit < AUTOPLAY_THRESHOLD)continue;
            ne.setHit(hit);

            if(ne instanceof LongNoteEntity)
            {
                if(ne.getState() == NoteEntity.State.NOT_JUDGED)
                {
                    queueSample(ne.getSample());
                    ne.setState(NoteEntity.State.LN_HEAD_JUDGE);
                    Entity ee = skin.getEntityMap().get("PRESSED_"+ne.getChannel()).copy();
                    entities_matrix.add(ee);
                    key_pressed_entity.put(ne.getChannel(), ee);
                }
                else if(ne.getState() == NoteEntity.State.LN_HOLD)
                {
                    ne.setState(NoteEntity.State.JUDGE);
                    longflare.get(ne.getChannel()).setAlive(false); //let's kill the longflare effect
                    key_pressed_entity.get(ne.getChannel()).setAlive(false);
                }
            }
            else
            {
                queueSample(ne.getSample());
                ne.setState(NoteEntity.State.JUDGE);
            }
        }
    }

    private void check_keyboard()
    {
        /* Misc keys
         * Like up and down
         */
        if(window.isKeyDown(java.awt.event.KeyEvent.VK_UP) && !updateHS)
        {
            if(hispeed > 0.5 || hispeed < 10)
            {
                hispeed += 0.5;
                updateHS = true;
            }
            return;
        }
        if(window.isKeyDown(java.awt.event.KeyEvent.VK_DOWN) && !updateHS)
        {
            if(hispeed > 0.5 || hispeed < 10)
            {
                hispeed -= 0.5;
                updateHS = true;
            }
            return;
        }

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

                    if(e.getState() == NoteEntity.State.TO_KILL)return;

                    queueSample(e.getSample());

                    double hit = e.testHit(judgment_line_y1, judgment_line_y2);
                    
                    String judge = skin.judgment.ratePrecision(hit);
                    e.setHit(hit);

                    /* we compare the judgment with a MISS, misses should be ignored here,
                     * because this is the case where the player pressed the note so soon
                     * that it's worse than BAD ( 20% or below on o2jam) so we need to let
                     * it pass like nothing happened */
                    if(!judge.equals(MISS_JUDGE)){
                        if(e instanceof LongNoteEntity){
                            longnote_holded.put(c, (LongNoteEntity) e);
                            if(e.getState() == NoteEntity.State.NOT_JUDGED)
				e.setState(NoteEntity.State.LN_HEAD_JUDGE);
                        }else{
                            e.setState(NoteEntity.State.JUDGE);
                        }
                    }
                }
            }
            else
            if(keyboard_key_pressed.get(c) == true) { // key released now

                keyboard_key_pressed.put(c, false);
                key_pressed_entity.get(c).setAlive(false);

                LongNoteEntity e = longnote_holded.remove(c);

                Entity lf = longflare.remove(c);
                if(lf !=null)lf.setAlive(false);

                // TODO: necessary ?? --> note_channels.get(c).isEmpty() || e != note_channels.get(c).getFirst()
                if(e == null || e.getState() != NoteEntity.State.LN_HOLD)continue;

                double hit = e.testHit(judgment_line_y1, judgment_line_y2);

                e.setHit(hit);
                e.setState(NoteEntity.State.JUDGE);
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
		    entities_matrix.add(ln);
		    ln_buffer.put(e.getChannel(),ln);
                    note_channels.get(ln.getChannel()).add(ln);
                }
                else if(e.getFlag() == Event.Flag.RELEASE){
                    LongNoteEntity lne = ln_buffer.remove(e.getChannel());
                    if(lne == null){
                        logger.log(Level.WARNING, "Attempted to RELEASE note {0}", e.getChannel());
                    }else{
                        lne.setEndY(abs_height);
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
     * This function will mirrorize the notes
     *
     * @param buffer
     */
    public void channelMirror(Iterator<Event> buffer)
    {
	while(buffer.hasNext())
	{
	    Event e = buffer.next();
	    switch(e.getChannel())
	    {
		case NOTE_1: e.setChannel(Event.Channel.NOTE_7); break;
		case NOTE_2: e.setChannel(Event.Channel.NOTE_6); break;
		case NOTE_3: e.setChannel(Event.Channel.NOTE_5); break;
		case NOTE_5: e.setChannel(Event.Channel.NOTE_3); break;
		case NOTE_6: e.setChannel(Event.Channel.NOTE_2); break;
		case NOTE_7: e.setChannel(Event.Channel.NOTE_1); break;
	    }
	}
    }

    /**
     * This function will shuffle the note lanes
     *
     * @param buffer
     */
    public void channelShuffle(Iterator<Event> buffer)
    {
	List channelSwap = new LinkedList();

	channelSwap.add(Event.Channel.NOTE_1);
	channelSwap.add(Event.Channel.NOTE_2);
	channelSwap.add(Event.Channel.NOTE_3);
	channelSwap.add(Event.Channel.NOTE_4);
	channelSwap.add(Event.Channel.NOTE_5);
	channelSwap.add(Event.Channel.NOTE_6);
	channelSwap.add(Event.Channel.NOTE_7);

	Collections.shuffle(channelSwap);

	while(buffer.hasNext())
	{
	    Event e = buffer.next();
	    switch(e.getChannel())
	    {
		case NOTE_1: e.setChannel((Channel) channelSwap.get(0)); break;
		case NOTE_2: e.setChannel((Channel) channelSwap.get(1)); break;
		case NOTE_3: e.setChannel((Channel) channelSwap.get(2)); break;
		case NOTE_4: e.setChannel((Channel) channelSwap.get(3)); break;
		case NOTE_5: e.setChannel((Channel) channelSwap.get(4)); break;
		case NOTE_6: e.setChannel((Channel) channelSwap.get(5)); break;
		case NOTE_7: e.setChannel((Channel) channelSwap.get(6)); break;
	    }
	}
    }
    
    /**
     * This function will randomize the notes, need more work
     *
     * TODO:
     * * Don't overlap the notes
     * 
     * @param buffer
     */
    public void channelRandom(Iterator<Event> buffer)
    {
	EnumMap<Event.Channel, Event.Channel> ln = new EnumMap<Event.Channel, Event.Channel>(Event.Channel.class);

	List channelSwap = new LinkedList();

	channelSwap.add(Event.Channel.NOTE_1);
	channelSwap.add(Event.Channel.NOTE_2);
	channelSwap.add(Event.Channel.NOTE_3);
	channelSwap.add(Event.Channel.NOTE_4);
	channelSwap.add(Event.Channel.NOTE_5);
	channelSwap.add(Event.Channel.NOTE_6);
	channelSwap.add(Event.Channel.NOTE_7);

	Collections.shuffle(channelSwap);

	while(buffer.hasNext())
	{
	    Event e = buffer.next();

	    switch(e.getChannel())
	    {
		    case NOTE_1:case NOTE_2:
		    case NOTE_3:case NOTE_4:
		    case NOTE_5:case NOTE_6:case NOTE_7:

			Channel chan = e.getChannel();

			int temp = (int)(Math.random()*7);
			chan = (Channel) channelSwap.get(temp);
			
			if(e.getFlag() == Event.Flag.NONE){
			    e.setChannel(chan);
			}
                        //WTF it seems that the release flag can be BEFORE the hold one :/
			else if(e.getFlag() == Event.Flag.HOLD || e.getFlag() == Event.Flag.RELEASE){
			    if(ln.get(e.getChannel()) != null)
			    {
				e.setChannel(ln.get(e.getChannel()));
				ln.remove(e.getChannel());
			    }
                            else
                            {
                                ln.put(e.getChannel(), chan);
                                e.setChannel(chan);
                            }
			}
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


        if(!source_queue_iterator.hasNext())
            source_queue_iterator = source_queue.iterator();
        Integer head = source_queue_iterator.next();

        Integer source = head;

        while(SoundManager.isPlaying(source)){
            if(!source_queue_iterator.hasNext())
                source_queue_iterator = source_queue.iterator();
            source = source_queue_iterator.next();

            if(source.equals(head)){
                logger.warning("Source queue exausted !");
                return;
            }
        }

        SoundManager.setGain(source, sample.volume);
        SoundManager.setPan(source, sample.pan);
        SoundManager.play(source, buffer);
    }
}


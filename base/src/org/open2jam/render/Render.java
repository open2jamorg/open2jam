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
import org.open2jam.render.entities.ComboCounterEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.BarEntity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;
import org.open2jam.render.entities.NumberEntity;
import org.open2jam.render.entities.SampleEntity;
import org.open2jam.render.entities.TimeEntity;
import org.open2jam.render.lwjgl.SoundManager;
import org.open2jam.util.Interval;
import org.open2jam.util.IntervalTree;


public class Render implements GameWindowCallback
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /** store the sound sources being played */
    private static final int MAX_SOURCES = 64;

    /** the config xml */
    private static final URL resources_xml = Render.class.getResource("/resources/resources.xml");

    /** the mapping of note channels to KeyEvent keys  */
    private static final EnumMap<Event.Channel, Integer> keyboard_map;

    private static final double AUTOPLAY_THRESHOLD = 50;

    /** The window that is being used to render the game */
    private final GameWindow window;

    /** the chart being rendered */
    private final Chart chart;
    
    /** is autoplaying ? */
    private final boolean AUTOPLAY;

    private final IntervalTree<Double> velocity_tree;

    /** the hispeed */
    private double hispeed;
    private boolean updateHS = false;

    /** the channelMirror, random select */
    private int channelModifier = 0;

    /** the visibility modifier */
    private int visibilityModifier = 0;

    /** skin info and entities */
    private Skin skin;

    /** the size of a measure */
    private double measure_size;

    /** the bpm at which the entities are falling */
    private double bpm;

    /** the vertical speed of entities pixels/milliseconds */
    //private double note_speed;

    /** the screen offset of the buffer */
//    private double buffer_offset;

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

    private EnumMap<Event.Channel,Entity> longflare;

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
    private NumberEntity score_entity;
    /** JamCombo variables */
    private ComboCounterEntity jamcombo_entity;
    /**
     * Cools: +2
     * Goods: +1
     * Everything else: reset to 0
     * >=50 to add a jam
     */
    private BarEntity jambar_entity;

    private BarEntity lifebar_entity;

    private int pills = 0;
    private LinkedList<Entity> pills_draw;
    private int consecutive_cools = 0;

    private NumberEntity minute_entity;
    private NumberEntity second_entity;

    private Entity judgment_entity;

    /** the combo counter */
    private ComboCounterEntity combo_entity;
    /** the maxcombo counter */
    private NumberEntity maxcombo_entity;

    private double hit_sum = 0, hit_count = 0, total_notes = 0;

    static{
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
        keyboard_map = Config.get().getKeyboardMap();
    }
    private long start_time;

    public Render(Chart c, double hispeed, boolean autoplay, int channelModifier, int visibilityModifier)
    {
        this.chart = c;
        this.hispeed = hispeed;
	this.AUTOPLAY = autoplay;
	this.channelModifier = channelModifier;
        this.visibilityModifier = visibilityModifier;
        window = ResourceFactory.get().getGameWindow();
        velocity_tree = new IntervalTree<Double>();
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
        // at this point the game window has gone away

        double precision = (hit_count / total_notes) * 100;
        double accuracy = (hit_sum / total_notes) * 100;
        JOptionPane.showMessageDialog(null,
                String.format("Precision : %.3f, Accuracy : %.3f", precision, accuracy)
                );
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
            logger.log(Level.SEVERE, "Skin load error {0}", ex);
        } catch (org.xml.sax.SAXException ex) {
            logger.log(Level.SEVERE, "Skin load error {0}", ex);
        } catch (java.io.IOException ex) {
            logger.log(Level.SEVERE, "Skin load error {0}", ex);
        }

        // cover image load
        try{
            BufferedImage img = chart.getCover();
            Sprite s = ResourceFactory.get().getSprite(img);
            s.setScale(skin.screen_scale_x, skin.screen_scale_y);
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
        buffer_bpm = chart.getBPM();

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

	longflare = new EnumMap<Event.Channel, Entity> (Event.Channel.class);

        last_sound = new EnumMap<Event.Channel,Event.SoundSample>(Event.Channel.class);

        fps_entity = (NumberEntity) skin.getEntityMap().get("FPS_COUNTER");
        entities_matrix.add(fps_entity);

        score_entity = (NumberEntity) skin.getEntityMap().get("SCORE_COUNTER");
        entities_matrix.add(score_entity);

        /**
         * TODO It's a combo counter, but because our combo counter substract 1 when it draws
         * the real number and the drawed number are different
         */
        jamcombo_entity = (ComboCounterEntity) skin.getEntityMap().get("JAM_COUNTER");
        entities_matrix.add(jamcombo_entity);

        jambar_entity = (BarEntity) skin.getEntityMap().get("JAM_BAR");
        jambar_entity.setLimit(50);
        entities_matrix.add(jambar_entity);

        lifebar_entity = (BarEntity) skin.getEntityMap().get("LIFE_BAR");
        lifebar_entity.setLimit(1000);
        lifebar_entity.setNumber(1000);
        lifebar_entity.setFillDirection(BarEntity.fillDirection.UP_TO_DOWN);
        entities_matrix.add(lifebar_entity);
        
        combo_entity = (ComboCounterEntity) skin.getEntityMap().get("COMBO_COUNTER");
        entities_matrix.add(combo_entity);

        maxcombo_entity = (NumberEntity) skin.getEntityMap().get("MAXCOMBO_COUNTER");
        entities_matrix.add(maxcombo_entity);

        minute_entity = (NumberEntity) skin.getEntityMap().get("MINUTE_COUNTER");
        entities_matrix.add(minute_entity);

        second_entity = (NumberEntity) skin.getEntityMap().get("SECOND_COUNTER");
        entities_matrix.add(second_entity);
        second_entity.showDigits(2);//show 2 digits

        pills_draw = new LinkedList<Entity>();

        for(Event.Channel c : keyboard_map.keySet())
        {
            keyboard_key_pressed.put(c, Boolean.FALSE);
            note_channels.put(c, new LinkedList<NoteEntity>());
        }


        List<Event> event_list = chart.getEvents();

        construct_velocity_tree(event_list.iterator());

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
        update_note_buffer(0);


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

        start_time = lastLoopTime = SystemTimer.getTime();
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
        long now = SystemTimer.getTime();
        long delta = now - lastLoopTime;
        lastLoopTime = now;
        lastFpsTime += delta;
        fps++;
        
        // update our FPS counter if a second has passed
        if (lastFpsTime >= 1000) {
            logger.log(Level.FINEST, "FPS: {0}", fps);
            fps_entity.setNumber(fps);
            lastFpsTime = 0;
            fps = 0;

            //the timer counter
            if(second_entity.getNumber() >= 59)
            {
                second_entity.setNumber(0);
                minute_entity.incNumber();
            }
            else
                second_entity.incNumber();
        }

        now -= start_time;

	if(AUTOPLAY)do_autoplay(now);
        else check_keyboard(now);

        if(updateHS)updateHispeed();
        
        update_note_buffer(now);

        Iterator<LinkedList<Entity>> i = entities_matrix.iterator();

        while(i.hasNext()) // loop over layers
        {
            // get entity iterator from layer
            Iterator<Entity> j = i.next().iterator();
            while(j.hasNext()) // loop over entities
            {
                Entity e = j.next();
                e.move(delta); // move the entity

                if(e instanceof TimeEntity)
                {
                    TimeEntity te = (TimeEntity) e;
                    double y = getViewport() - velocity_integral(now,te.getTime());
                    if(te.getTime() - now <= 0)e.judgment();
                    if(e instanceof MeasureEntity) y += e.getHeight()*2;
                    e.setPos(e.getX(), y);

                    if(e instanceof NoteEntity){
                        check_judgment((NoteEntity)e);
                    }
                }
		else if (e.getY() >= getViewport()) // else, if it's on the line, judge it
		{
                    e.judgment();
                }

                if(!e.isAlive())j.remove();
                else e.draw();
            }
        }

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

    /**
     * given a time segment, returns the distance, in pixels,
     * from each segment based on the bpm.
     *
     * segment types returned by velocity_tree:
     *  a    t0    b   t1  ->  b - t0
     * t0     a   t1    b  -> t1 -  a
     * t0     a    b   t1  ->  b -  a
     *  a    t0   t1    b  -> t1 - t0
     */
    public double velocity_integral(long t0, long t1)
    {
        int sign = 1;
        if(t0 > t1){
            long tmp = t1;t1 = t0;t0 = tmp; // swap
            sign = -1;
        }
        List<Interval<Double>> list = velocity_tree.getIntervals(t0, t1);
        double integral = 0;
        for(Interval<Double> i : list)
        {
            if(i.getStart() < t0) // 1st or 4th case
            {
                if(i.getEnd() < t1) // 1st case
                    integral += i.getData() * (i.getEnd() - t0);
                else // 4th case
                    integral += i.getData() * (t1 - t0);
            }
            else { // 2nd or 3rd case
                if(t1 < i.getEnd()) // 2nd case
                    integral += i.getData() * (t1 - i.getStart());
                else // 3rd case
                    integral += i.getData() * (i.getEnd() - i.getStart());
            }
        }
        return sign * integral;
    }
    
    private double judgmentArea()
    {
        // y2-y1 is the the upper half of the judgment area
        // 2*(y2-y1) is the total area
        // y1 + 2*(y2-y1) is the end line of the area
        // simplifying: y1 + 2*y2 - 2*y1 == 2*y2 - y1
        return 2 * judgment_line_y2 - judgment_line_y1;
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
    //public double getNoteSpeed() { return note_speed; }

    public double getMeasureSize() { return measure_size; }
    public double getViewport() { return judgment_line_y2; }

    private void check_judgment(NoteEntity ne)
    {
        String judge;
        switch (ne.getState())
        {
            case NOT_JUDGED: // you missed it (no keyboard input)
                if(ne.isAlive()
                        && ((ne instanceof LongNoteEntity && ne.getStartY() >= judgmentArea()) //needed by the ln head
                        || (ne.getY() >= judgmentArea())))
                {
                    if(judgment_entity != null)judgment_entity.setAlive(false);
                    judgment_entity = skin.getEntityMap().get("EFFECT_"+MISS_JUDGE).copy();
                    entities_matrix.add(judgment_entity);

                    note_counter.get(MISS_JUDGE).incNumber();
                    combo_entity.resetNumber();

                    update_screen_info(MISS_JUDGE,ne.getHit());
                    
                    ne.setState(NoteEntity.State.TO_KILL);
                 }
            break;
            case JUDGE: //LN & normal ones: has finished with good result
                judge = skin.judgment.ratePrecision(ne.getHit());

                judge = update_screen_info(judge,ne.getHit());

                if(judgment_entity != null)judgment_entity.setAlive(false);
                judgment_entity = skin.getEntityMap().get("EFFECT_"+judge).copy();
                entities_matrix.add(judgment_entity);

		note_counter.get(judge).incNumber();

		if(!judge.equals(MISS_JUDGE))
                {
		    Entity ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,
		    getViewport()-ee.getHeight()/2);
		    entities_matrix.add(ee);

		    if(ne.getHit() >= skin.judgment.combo_threshold)combo_entity.incNumber();
		    else {
                        if(judge.equals("JUDGMENT_GOOD"))combo_entity.incNumber(); //because of the pills
                        else combo_entity.resetNumber();
                    }
                    if(ne instanceof LongNoteEntity)ne.setState(NoteEntity.State.TO_KILL);
                    else ne.setAlive(false);
                } else {
                    combo_entity.resetNumber();
                    ne.setState(NoteEntity.State.TO_KILL);
                }
                last_sound.put(ne.getChannel(), ne.getSample());
            break;
            case LN_HEAD_JUDGE: //LN: Head has been played
                judge = skin.judgment.ratePrecision(ne.getHit());

                judge = update_screen_info(judge,ne.getHit());

                if(judgment_entity != null)judgment_entity.setAlive(false);
                judgment_entity = skin.getEntityMap().get("EFFECT_"+judge).copy();
                entities_matrix.add(judgment_entity);

		note_counter.get(judge).incNumber();

		if(!judge.equals(MISS_JUDGE))
                {
		    Entity ee = skin.getEntityMap().get("EFFECT_LONGFLARE").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,ee.getY());
		    entities_matrix.add(ee);
                    Entity to_kill = longflare.put(ne.getChannel(),ee);
                    if(to_kill != null)to_kill.setAlive(false);
		    
		    ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,
		    getViewport()-ee.getHeight()/2);
		    entities_matrix.add(ee);

		    if(ne.getHit() >= skin.judgment.combo_threshold)combo_entity.incNumber();
		    else {
                        if(judge.equals("JUDGMENT_GOOD"))combo_entity.incNumber(); //because of the pills
                        else combo_entity.resetNumber();
                    }
                    ne.setState(NoteEntity.State.LN_HOLD);
                }
                last_sound.put(ne.getChannel(), ne.getSample());
            break;
            case LN_HOLD:    // You keept too much time the note held that it misses
                if(ne.isAlive() && ne.getY() >= judgmentArea())
                {
                    if(judgment_entity != null)judgment_entity.setAlive(false);
                    judgment_entity = skin.getEntityMap().get("EFFECT_"+MISS_JUDGE).copy();
                    entities_matrix.add(judgment_entity);

                    note_counter.get(MISS_JUDGE).incNumber();
                    combo_entity.resetNumber();

                    update_screen_info(MISS_JUDGE,ne.getHit());

                    ne.setState(NoteEntity.State.TO_KILL);
                 }
            break;
            case TO_KILL: // this is the "garbage collector", it just removes the notes off window
                if(ne.isAlive() && ne.getY() >= window.getResolutionHeight())
                {
                    // kill it
                    ne.setAlive(false);
                }
            break;
        }
    }

    /**
     * TODO It would be nice to move all the check_judgment non judgment things to this function
     * (bars, combos, counter, etc)
     * @param judge
     * @return judge
     */
    private String update_screen_info(String judge, double hit)
    {
        int score_value = 0;
        if(judge.equals("JUDGMENT_COOL"))
        {
            jambar_entity.addNumber(2);
            consecutive_cools++;
            if(lifebar_entity.getNumber() <= lifebar_entity.getLimit())lifebar_entity.addNumber(100);

            score_value = 200 + (jamcombo_entity.getNumber()*10);
        }
        else if(judge.equals("JUDGMENT_GOOD"))
        {
            jambar_entity.addNumber(1);
            consecutive_cools = 0;
            if(lifebar_entity.getNumber() <= lifebar_entity.getLimit())lifebar_entity.addNumber(50);

             score_value = 100;
        }
        else if(judge.equals("JUDGMENT_BAD"))
        {
            if(pills > 0)
            {
                judge = "JUDGMENT_GOOD";
                jambar_entity.addNumber(1);
                pills--;
                pills_draw.getLast().setAlive(false);

                score_value = 100; // TODO: not sure
            }
            else
            {
                jambar_entity.setNumber(0);
                jamcombo_entity.resetNumber();

                score_value = 4;
            }
            consecutive_cools = 0;
            if(lifebar_entity.getNumber() <= lifebar_entity.getLimit())lifebar_entity.addNumber(30);
        }
        else if(judge.equals("JUDGMENT_MISS"))
        {
            jambar_entity.setNumber(0);
            jamcombo_entity.resetNumber();
            consecutive_cools = 0;

            if(lifebar_entity.getNumber() >= 30)lifebar_entity.addNumber(-30);

            if(score_entity.getNumber() >= 10)score_value = -10;
            else score_value = -score_entity.getNumber();
        }

        score_entity.addNumber(score_value);

        if(jambar_entity.getNumber() >= jambar_entity.getLimit())
        {
            jambar_entity.setNumber(0); //reset
            jamcombo_entity.incNumber();
        }

        if(consecutive_cools >= 15 && pills < 5)
        {
            consecutive_cools -= 15;
            pills++;
            Entity ee = skin.getEntityMap().get("PILL_"+pills).copy();
            entities_matrix.add(ee);
            pills_draw.add(ee);
        }

        if(combo_entity.getNumber() > 1 && maxcombo_entity.getNumber()<(combo_entity.getNumber()-1))
        {
            maxcombo_entity.incNumber();
        }

        hit_sum += hit;
        if(!judge.equals(MISS_JUDGE))hit_count++;
        total_notes++;
        
        return judge;
    }

    private void do_autoplay(long now)
    {
        for(Map.Entry<Event.Channel,Integer> entry : keyboard_map.entrySet())
        {
            Event.Channel c = entry.getKey();

            NoteEntity ne = nextNoteKey(c);

            if(ne == null)continue;
//            if(ne.getStartY() < judgment_line_y2)continue; //sync


            if(Math.abs(ne.getTime() - now) > AUTOPLAY_THRESHOLD)continue;
            ne.setHit(1);
            
            if(ne instanceof LongNoteEntity)
            {
                if(ne.getState() == NoteEntity.State.NOT_JUDGED)
                {
                    queueSample(ne.getSample());
                    ne.setState(NoteEntity.State.LN_HEAD_JUDGE);
                    Entity ee = skin.getEntityMap().get("PRESSED_"+ne.getChannel()).copy();
                    entities_matrix.add(ee);
                    Entity to_kill = key_pressed_entity.put(ne.getChannel(), ee);
                    if(to_kill != null)to_kill.setAlive(false);
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

    private void check_keyboard(long now)
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
                    Entity to_kill = key_pressed_entity.put(c, ee);
                    if(to_kill != null)to_kill.setAlive(false);

                    NoteEntity e = nextNoteKey(c);
                    if(e == null){
                        Event.SoundSample i = last_sound.get(c);
                        if(i != null)queueSample(i);
                        continue;
                    }

                    queueSample(e.getSample());

                    double hit = 1 - Math.abs(e.getTime() - now)/1000.0;
                    if(hit > 1 || hit < 0)hit = 0;
                    
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

                double hit = 1 - Math.abs(e.getTime() - now)/1000.0;
                if(hit > 1 || hit < 0)hit = 0;

                e.setHit(hit);
                e.setState(NoteEntity.State.JUDGE);
            }
        }
    }

    /** this returns the next note that needs to be played
     ** of the defined channel or NULL if there's
     ** no such note in the moment **/
    private NoteEntity nextNoteKey(Event.Channel c)
    {
        if(note_channels.get(c).isEmpty())return null;
        NoteEntity ne = note_channels.get(c).getFirst();
        while(ne.getState() != NoteEntity.State.NOT_JUDGED &&
            ne.getState() != NoteEntity.State.LN_HOLD)
        {
            note_channels.get(c).removeFirst();
            if(note_channels.get(c).isEmpty())return null;
            ne = note_channels.get(c).getFirst();
        }
        return ne;
    }

    private int buffer_measure = 0;

    private double fractional_measure = 1;

    private final int buffer_upper_bound = -10;

    private long buffer_timer = 0;

    private double buffer_bpm;

    private double buffer_measure_pointer = 0;



    /** update the note layer of the entities_matrix.
    *** note buffering is equally distributed between the frames
    **/
    private void update_note_buffer(long now)
    {
        while(buffer_iterator.hasNext() && getViewport() - velocity_integral(now,buffer_timer) > buffer_upper_bound)
        {
            Event e = buffer_iterator.next();
//            System.out.println(buffer_bpm);
            while(e.getMeasure() > buffer_measure) // this is the start of a new measure
            {
                buffer_timer += 1000 * ( 240/buffer_bpm * (fractional_measure-buffer_measure_pointer) );
                MeasureEntity m = (MeasureEntity) skin.getEntityMap().get("MEASURE_MARK").copy();
                m.setTime(buffer_timer);
                entities_matrix.add(m);
                buffer_measure++;
                fractional_measure = 1;
                buffer_measure_pointer = 0;
            }

            buffer_timer += 1000 * ( 240/buffer_bpm * (e.getPosition()-buffer_measure_pointer) );
            buffer_measure_pointer = e.getPosition();

            System.out.println("t: "+buffer_timer+", "+e.getChannel());

            switch(e.getChannel())
            {
                case TIME_SIGNATURE:
                fractional_measure = e.getValue();
                break;

                case BPM_CHANGE:
                buffer_bpm = e.getValue();
                break;

                case NOTE_1:case NOTE_2:
                case NOTE_3:case NOTE_4:
                case NOTE_5:case NOTE_6:case NOTE_7:
                if(e.getFlag() == Event.Flag.NONE){
                    NoteEntity n = (NoteEntity) skin.getEntityMap().get(e.getChannel().toString()).copy();
                    n.setTime(buffer_timer);
                    n.setSample(e.getSample());
		    entities_matrix.add(n);
                    note_channels.get(n.getChannel()).add(n);
                }
                else if(e.getFlag() == Event.Flag.HOLD){
                    LongNoteEntity ln = (LongNoteEntity) skin.getEntityMap().get("LONG_"+e.getChannel()).copy();
                    ln.setTime(buffer_timer);
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
                        lne.setEndTime(buffer_timer);
                    }
                }
                break;
                
                case AUTO_PLAY:
                case NOTE_SC:
                SampleEntity s = new SampleEntity(this,e.getSample(),0);
                s.setTime(buffer_timer);
                entities_matrix.add(s);
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
	List<Event.Channel> channelSwap = new LinkedList<Event.Channel>();

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

	List<Event.Channel> channelSwap = new LinkedList<Event.Channel>();

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

    private void construct_velocity_tree(Iterator<Event> it)
    {
        int measure = 0;
        long last_bpm_change = 0;
        long timer = 0;
        double my_bpm = this.bpm;
        double frac_measure = 1;
        double measure_pointer = 0;
        double my_note_speed = ((my_bpm/240) * measure_size) / 1000.0d;
        while(it.hasNext())
        {
            Event e = it.next();
            while(e.getMeasure() > measure)
            {
                timer += 1000 * ( 240/my_bpm * (frac_measure-measure_pointer) );
                measure++;
                frac_measure = 1;
                measure_pointer = 0;
            }
            timer += 1000 * ( 240/my_bpm * (e.getPosition()-measure_pointer) );
            measure_pointer = e.getPosition();

            switch(e.getChannel())
            {
                case BPM_CHANGE:
                    velocity_tree.addInterval(last_bpm_change, timer, my_note_speed);
                    my_bpm = e.getValue();
                    my_note_speed = ((my_bpm/240) * measure_size) / 1000.0d;
                    last_bpm_change = timer;
                break;
                case TIME_SIGNATURE:
                    frac_measure = e.getValue();
                break;
            }
        }
        velocity_tree.addInterval(last_bpm_change, timer, my_note_speed);
        velocity_tree.build();
    }
}


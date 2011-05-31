
package org.open2jam.render;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.open2jam.util.Logger;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.lwjgl.opengl.DisplayMode;
import org.open2jam.Config;
import org.open2jam.GameOptions;
import org.open2jam.parser.Event;
import org.open2jam.parser.Chart;
import org.open2jam.render.entities.BarEntity;
import org.open2jam.render.entities.ComboCounterEntity;
import org.open2jam.render.entities.CompositeEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;
import org.open2jam.render.entities.NumberEntity;
import org.open2jam.render.entities.SampleEntity;
import org.open2jam.render.lwjgl.SoundManager;
import org.open2jam.util.Interval;
import org.open2jam.util.IntervalTree;
import org.open2jam.util.SystemTimer;

/**
 *
 * @author fox
 */
public abstract class Render implements GameWindowCallback
{
    /** the config xml */
    private static final URL resources_xml = TimeRender.class.getResource("/resources/resources.xml");

    private static final int JUDGMENT_SIZE = 64;

    /** 4 beats per minute, 4 * 60 beats per second, 4*60*1000 per millisecond */
    private static final int BEATS_PER_MSEC = 4 * 60 * 1000;

    /** skin info and entities */
    Skin skin;
    
    /** defines the judgment space */
    double judgment_line_y1;
    double judgment_line_y2;

    /** store the sound sources being played */
    private static final int MAX_SOURCES = 64;
    
    /** the mapping of note channels to KeyEvent keys  */
    final EnumMap<Event.Channel, Integer> keyboard_map;
    
    /** The window that is being used to render the game */
    final GameWindow window;

    /** the chart being rendered */
    private final Chart chart;

    /** The recorded fps */
    int fps;
    
    /** the size of a measure */
    private double measure_size;

    /** the layer of the notes */
    int note_layer;

    /** the bpm at which the entities are falling */
    private double bpm;

    /** this queue hold the available sources
     * that may be used to play sounds */
    LinkedList<Integer> source_queue;
    private Iterator<Integer> source_queue_iterator;

    /** maps the Event value to OpenGL sample ID's */
    private Map<Integer, Integer> samples;

    /** The time at which the last rendering looped started from the point of view of the game logic */
    double lastLoopTime;

    /** The time since the last record of fps */
    double lastFpsTime = 0;

    /** the time it started rendering */
    double start_time;

       /** a list of list of entities.
    ** basically, each list is a layer of entities
    ** the layers are rendered in order
    ** so entities at layer X will always be rendered before layer X+1 */
    final EntityMatrix entities_matrix;

    /** this iterator is used by the update_note_buffer
     * to go through the events on the chart */
    Iterator<Event> buffer_iterator;

    /** this is used by the update_note_buffer
     * to remember the "opened" long-notes */
    private EnumMap<Event.Channel, LongNoteEntity> ln_buffer;

    /** this holds the actual state of the keyboard,
     * whether each is being pressed or not */
    EnumMap<Event.Channel,Boolean> keyboard_key_pressed;

    EnumMap<Event.Channel,Entity> longflare;

    /** these are the same notes from the entity_matrix
     * but divided in channels for ease to pull */
    private EnumMap<Event.Channel,LinkedList<NoteEntity>> note_channels;

    /** entities for the key pressed events
     * need to keep track of then to kill
     * when the key is released */
    EnumMap<Event.Channel,Entity> key_pressed_entity;

    /** keep track of the long note the player may be
     * holding with the key */
    EnumMap<Event.Channel,LongNoteEntity> longnote_holded;

    /** keep trap of the last sound of each channel
     * so that the player can re-play the sound when the key is pressed */
    EnumMap<Event.Channel,Event.SoundSample> last_sound;

    /** number to display the fps, and note counters on the screen */
    NumberEntity fps_entity;

    NumberEntity score_entity;
    /** JamCombo variables */
    ComboCounterEntity jamcombo_entity;
    /**
     * Cools: +2
     * Goods: +1
     * Everything else: reset to 0
     * >=50 to add a jam
     */
    BarEntity jambar_entity;

    BarEntity lifebar_entity;

    LinkedList<Entity> pills_draw;

    int consecutive_cools = 0;

    NumberEntity minute_entity;
    NumberEntity second_entity;

    Entity judgment_entity;

    /** the combo counter */
    ComboCounterEntity combo_entity;
    protected ComboCounterEntity combo_title;

    /** the maxcombo counter */
    NumberEntity maxcombo_entity;

    protected Entity judgment_line;

    /** statistics variables */
    double hit_sum = 0;
    double hit_count = 0;
    double total_notes = 0;

    protected CompositeEntity visibility_entity;
    static {
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
    }

    Render(Chart chart)
    {
        keyboard_map = Config.get().getKeyboardMap(Config.KeyboardType.K7);
        window = ResourceFactory.get().getGameWindow();
        entities_matrix = new EntityMatrix();
        this.chart = chart;
        velocity_tree = new IntervalTree<Double,Double>();
    }

    /** set the screen dimensions */
    public void setDisplay(DisplayMode dm, boolean vsync, boolean fs, boolean bilinear) {
        window.setDisplay(dm,vsync,fs,bilinear);
    }

    /* make the rendering start */
    public void startRendering()
    {
        window.setGameWindowCallback(this);
        window.setTitle(chart.getArtist()+" - "+chart.getTitle());

        try{
            window.startRendering();
        }catch(OutOfMemoryError e) {
            System.gc();
            Logger.global.log(Level.SEVERE, "System out of memory ! baillin out !!{0}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Fatal Error", "System out of memory ! baillin out !!",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        // at this point the game window has gone away

        double precision = (hit_count / total_notes) * 100;
        double accuracy = (hit_sum / total_notes) * 100;
        Logger.global.log(Level.INFO,String.format("Precision : %.3f, Accuracy : %.3f", precision, accuracy));
    }

    /** play a sample */
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
                Logger.global.warning("Source queue exausted !");
                return;
            }
        }
        float vol = GameOptions.getKeyVolume();
        if(sample.isBGM()) vol = GameOptions.getBGMVolume();
        vol = sample.volume*vol;
        if(vol < 0f) vol = 0f;
        if(vol > 1f) vol = 1f;
        SoundManager.setGain(source, vol);
        SoundManager.setPan(source, sample.pan);
        SoundManager.play(source, buffer);
    }

    /** XXX: inline ? */
    private void updateHispeed()
    {
        judgment_line_y1 = skin.judgment_line - JUDGMENT_SIZE;
        if(GameOptions.getHiSpeed() > 1){
            double off = JUDGMENT_SIZE * (GameOptions.getHiSpeed() - 1);
            judgment_line_y1 -= off;
        }

        measure_size = 0.8 * GameOptions.getHiSpeed() * getViewport();
    }

    double getViewport() { return judgment_line_y2; }

    double judgmentArea()
    {
        // y2-y1 is the the upper half of the judgment area
        // 2*(y2-y1) is the total area
        // y1 + 2*(y2-y1) is the end line of the area
        // simplifying: y1 + 2*y2 - 2*y1 == 2*y2 - y1
        return 2 * judgment_line_y2 - judgment_line_y1;
    }

    /**
    * initialize the common elements for the game.
    * this is called by the window render
    */
    @Override
    public void initialise()
    {
        lastLoopTime = SystemTimer.getTime();

        // skin load
        try {
            SkinHandler sb = new SkinHandler("o2jam", window.getResolutionWidth(), window.getResolutionHeight());
            SAXParserFactory.newInstance().newSAXParser().parse(resources_xml.openStream(), sb);
            skin = sb.getResult();
        } catch (ParserConfigurationException ex) {
            Logger.global.log(Level.SEVERE, "Skin load error {0}", ex);
        } catch (org.xml.sax.SAXException ex) {
            Logger.global.log(Level.SEVERE, "Skin load error {0}", ex);
        } catch (java.io.IOException ex) {
            Logger.global.log(Level.SEVERE, "Skin load error {0}", ex);
        }

        // cover image load
        try{
            BufferedImage img = chart.getCover();
            Sprite s = ResourceFactory.get().getSprite(img);
            s.setScale(skin.screen_scale_x, skin.screen_scale_y);
            s.draw(0, 0);
            window.update();
        } catch (NullPointerException e){
            Logger.global.log(Level.INFO, "No cover image on file: {0}", chart.getSource().getName());
        }

        judgment_line_y2 = skin.judgment_line;
	updateHispeed();

        bpm = chart.getBPM();
        buffer_bpm = chart.getBPM();

        note_layer = skin.getEntityMap().get("NOTE_1").getLayer();

        // adding static entities
        for(Entity e : skin.getEntityList()){
            entities_matrix.add(e);
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

        jamcombo_entity = (ComboCounterEntity) skin.getEntityMap().get("JAM_COUNTER");
        entities_matrix.add(jamcombo_entity);

        jambar_entity = (BarEntity) skin.getEntityMap().get("JAM_BAR");
        jambar_entity.setLimit(50);
        entities_matrix.add(jambar_entity);

        lifebar_entity = (BarEntity) skin.getEntityMap().get("LIFE_BAR");
        lifebar_entity.setLimit(1000);
        lifebar_entity.setNumber(1000);
        lifebar_entity.setFillDirection(BarEntity.FillDirection.UP_TO_DOWN);
        entities_matrix.add(lifebar_entity);

        combo_entity = (ComboCounterEntity) skin.getEntityMap().get("COMBO_COUNTER");
        combo_entity.setThreshold(2);
        entities_matrix.add(combo_entity);

        maxcombo_entity = (NumberEntity) skin.getEntityMap().get("MAXCOMBO_COUNTER");
        entities_matrix.add(maxcombo_entity);

        minute_entity = (NumberEntity) skin.getEntityMap().get("MINUTE_COUNTER");
        entities_matrix.add(minute_entity);

        second_entity = (NumberEntity) skin.getEntityMap().get("SECOND_COUNTER");
        second_entity.showDigits(2);//show 2 digits
        entities_matrix.add(second_entity);

        pills_draw = new LinkedList<Entity>();
       
         if (GameOptions.visibilityModifiersOn())
         {
             if (GameOptions.getTowel())
                entities_matrix.add(skin.getEntityMap().get("TOWEL"));
             
             else
             {
                 visibility_entity = new CompositeEntity();
                 visibility();
             }
         }

        judgment_line = skin.getEntityMap().get("JUDGMENT_LINE");
        entities_matrix.add(judgment_line);

        for(Event.Channel c : keyboard_map.keySet())
        {
            keyboard_key_pressed.put(c, Boolean.FALSE);
            note_channels.put(c, new LinkedList<NoteEntity>());
        }


        List<Event> event_list = chart.getEvents();

        construct_velocity_tree(event_list.iterator());

        buffer_iterator = event_list.iterator();
        
	//Let's randomize "-"
        if (GameOptions.channelModifiersOn())
        {        
            if (GameOptions.getRandom())
                channelRandom(buffer_iterator);

            else if (GameOptions.getShuffle())
                channelShuffle(buffer_iterator);

            else if (GameOptions.getMirror())
                channelMirror(buffer_iterator);
            
            // get a new iterator
            buffer_iterator = event_list.iterator();
        }

        // load up initial buffer
        update_note_buffer(0);

        // create sound sources
        source_queue = new LinkedList<Integer>();

        //set main Volume
        SoundManager.mainVolume(GameOptions.getMasterVolume());

        try{
            for(int i=0;i<MAX_SOURCES;i++)
                source_queue.push(SoundManager.newSource()); // creates sources
        }catch(org.lwjgl.openal.OpenALException e){
            Logger.global.log(Level.WARNING, "Couldn''t create enough sources({0})", MAX_SOURCES);
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

    /** this returns the next note that needs to be played
     ** of the defined channel or NULL if there's
     ** no such note in the moment **/
    NoteEntity nextNoteKey(Event.Channel c)
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
        last_sound.put(c, ne.getSample());
        return ne;
    }
    
    
    private int buffer_measure = 0;

    private double fractional_measure = 1;

    private double buffer_timer = 0;

    private double buffer_bpm;

    private double buffer_measure_pointer = 0;


    /** update the note layer of the entities_matrix.
    *** note buffering is equally distributed between the frames
    **/
    void update_note_buffer(double now)
    {
        while(buffer_iterator.hasNext() && getViewport() - velocity_integral(now,buffer_timer) > -10)
        {
            Event e = buffer_iterator.next();
            while(e.getMeasure() > buffer_measure) // this is the start of a new measure
            {
                buffer_timer += (BEATS_PER_MSEC * (fractional_measure-buffer_measure_pointer)) / buffer_bpm;
                MeasureEntity m = (MeasureEntity) skin.getEntityMap().get("MEASURE_MARK").copy();
                m.setTime(buffer_timer);
                entities_matrix.add(m);
                buffer_measure++;
                fractional_measure = 1;
                buffer_measure_pointer = 0;
            }

            buffer_timer += (BEATS_PER_MSEC * (e.getPosition()-buffer_measure_pointer)) / buffer_bpm;
            buffer_measure_pointer = e.getPosition();

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
                        Logger.global.log(Level.WARNING, "Attempted to RELEASE note {0}", e.getChannel());
                    }else{
                        lne.setEndTime(buffer_timer,velocity_integral(lne.getTime(),buffer_timer));
                    }
                }
                break;
                //TODO ADD SUPPORT
                case NOTE_SC:
                case NOTE_8:case NOTE_9:
                case NOTE_10:case NOTE_11:
                case NOTE_12:case NOTE_13:case NOTE_14:
                case NOTE_SC2:

                case AUTO_PLAY:
                e.getSample().toBGM();
                SampleEntity s = new SampleEntity(this,e.getSample(),0);
                s.setTime(buffer_timer);
                entities_matrix.add(s);
                break;
            }
        }
    }

    private final IntervalTree<Double,Double> velocity_tree;
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
    double velocity_integral(double t0, double t1)
    {
        boolean negative = false;
        if(t0 > t1){
            double tmp = t1;t1 = t0;t0 = tmp; // swap
            negative = true;
        }
        List<Interval<Double,Double>> list = velocity_tree.getIntervals(t0, t1);
        double integral = 0;
        for(Interval<Double,Double> i : list)
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
        return negative ? -integral : integral;
    }

    private void construct_velocity_tree(Iterator<Event> it)
    {
        int measure = 0;
        double timer = 0, last_bpm_change = 0;
        double my_bpm = this.bpm;
        double frac_measure = 1;
        double measure_pointer = 0;
        double my_note_speed = (my_bpm * measure_size) / BEATS_PER_MSEC;
        while(it.hasNext())
        {
            Event e = it.next();
            while(e.getMeasure() > measure)
            {
                timer += (BEATS_PER_MSEC * (frac_measure-measure_pointer)) / my_bpm;
                measure++;
                frac_measure = 1;
                measure_pointer = 0;
            }
            timer += (BEATS_PER_MSEC * (e.getPosition()-measure_pointer)) / my_bpm;
            measure_pointer = e.getPosition();

            switch(e.getChannel())
            {
                case BPM_CHANGE:
                    velocity_tree.addInterval(last_bpm_change, timer, my_note_speed);
                    my_bpm = e.getValue();
                    my_note_speed = (my_bpm * measure_size) / BEATS_PER_MSEC;
                    last_bpm_change = timer;
                break;
                case TIME_SIGNATURE:
                    frac_measure = e.getValue();
                break;
            }
        }
        // pad 10s to make sure the song ends
        velocity_tree.addInterval(last_bpm_change, timer+10000, my_note_speed);
        velocity_tree.build();
    }


     /**
     * This function will mirrorize the notes
     * TODO ADD P2 SUPPORT
     * @param buffer
     */
     void channelMirror(Iterator<Event> buffer)
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
     * TODO ADD P2 SUPPORT
     * @param buffer
     */
    void channelShuffle(Iterator<Event> buffer)
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
		case NOTE_1: e.setChannel(channelSwap.get(0)); break;
		case NOTE_2: e.setChannel(channelSwap.get(1)); break;
		case NOTE_3: e.setChannel(channelSwap.get(2)); break;
		case NOTE_4: e.setChannel(channelSwap.get(3)); break;
		case NOTE_5: e.setChannel(channelSwap.get(4)); break;
		case NOTE_6: e.setChannel(channelSwap.get(5)); break;
		case NOTE_7: e.setChannel(channelSwap.get(6)); break;
	    }
	}
    }

    /**
     * This function will randomize the notes
     * o2jam randomize the pattern each measure unless a longnote is in between measures
     * This implementation keeps the randomization of the previous measure if that happens
     * @param buffer
     */
    void channelRandom(Iterator<Event> buffer)
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

        EnumMap<Event.Channel, Event.Channel> lnMap = new EnumMap<Event.Channel, Event.Channel>(Event.Channel.class);

        int last_measure = -1;
	while(buffer.hasNext())
	{
	    Event e = buffer.next();

            if(e.getMeasure() > last_measure)
            {
                if(lnMap.isEmpty())
                    Collections.shuffle(channelSwap);
                last_measure = e.getMeasure();
            }

	    switch(e.getChannel())
	    {
		case NOTE_1: 
                    setRandomChannel(e, lnMap, channelSwap.get(0));
                break;
		case NOTE_2:
                    setRandomChannel(e, lnMap, channelSwap.get(1));
                break;
		case NOTE_3:
                    setRandomChannel(e, lnMap, channelSwap.get(2));
                break;
		case NOTE_4:
                    setRandomChannel(e, lnMap, channelSwap.get(3));
                break;
		case NOTE_5:
                    setRandomChannel(e, lnMap, channelSwap.get(4));
                break;
		case NOTE_6:
                    setRandomChannel(e, lnMap, channelSwap.get(5));
                break;
		case NOTE_7:
                    setRandomChannel(e, lnMap, channelSwap.get(6));
                break;
	    }
	}
    }

    protected void setRandomChannel(Event e, EnumMap<Event.Channel, Event.Channel> lnMap, Event.Channel random)
    {
        Event.Channel c = random;

        if(e.getFlag() == Event.Flag.HOLD || e.getFlag() == Event.Flag.RELEASE)
        {
            if(!lnMap.containsKey(e.getChannel()))
                lnMap.put(e.getChannel(), c);
            else
                c = lnMap.remove(e.getChannel());
        }
        else if(e.getFlag() == Event.Flag.NONE)
            c = lnMap.containsValue(c) ? Event.Channel.NONE : c;

        if(c == null)
        {
            Logger.global.log(Level.WARNING, "FUCK THIS RANDOMNESS! I mean... channel null :/");
            c = random;
        }

        e.setChannel(c);
    }

    protected void visibility()
    {
        int height = 0;
        int width  = 0;

        Sprite rec = null;
        // We will make a new entity with the masking rectangle for each note lane
        // because we can't know for sure where the notes will be,
        // meaning that they may not be together
        for(Event.Channel ev : Event.Channel.values())
        {
            if(ev.toString().startsWith("NOTE_") && skin.getEntityMap().get(ev.toString()) != null)
            {
                height = (int)Math.round(getViewport());
                width = (int)Math.round(skin.getEntityMap().get(ev.toString()).getWidth());
                rec  = ResourceFactory.get().doRectangle(width, height);
                visibility_entity.getEntityList().add(new Entity(rec, skin.getEntityMap().get(ev.toString()).getX(), 0));
            }
        }
        
        int layer = note_layer+1;

        for(Entity e : skin.getEntityList())
            if(e.getLayer() > layer) layer++;

        visibility_entity.setLayer(++layer);

        for(Entity e : skin.getAllEntities())
        {
            int l = e.getLayer();
            if(l >= layer)
                e.setLayer(++l);
        }

//        skin.getEntityMap().get("MEASURE_MARK").setLayer(layer);
        if(GameOptions.getSudden() || GameOptions.getDark())
            skin.getEntityMap().get("JUDGMENT_LINE").setLayer(layer);

        entities_matrix.add(visibility_entity);
        return;
    }

    /**
     * Notification that the game window has been closed
     */
    @Override
    public void windowClosed() {
        SoundManager.killData();
    }
}

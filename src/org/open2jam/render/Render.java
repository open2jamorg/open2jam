
package org.open2jam.render;

import org.open2jam.sound.FmodExSoundSystem;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.DisplayMode;
import org.open2jam.Config;
import org.open2jam.GameOptions;
import org.open2jam.parsers.Chart;
import org.open2jam.parsers.Event;
import org.open2jam.parsers.EventList;
import org.open2jam.parsers.utils.SampleData;
import org.open2jam.render.entities.*;
import org.open2jam.render.judgment.JudgmentResult;
import org.open2jam.render.judgment.JudgmentStrategy;
import org.open2jam.render.judgment.TimeJudgment;
import org.open2jam.render.lwjgl.TrueTypeFont;
import org.open2jam.sound.Sample;
import org.open2jam.sound.Sound;
import org.open2jam.sound.SoundChannel;
import org.open2jam.sound.SoundSystem;
import org.open2jam.sound.SoundSystemException;
import org.open2jam.sound.SoundSystemInitException;
import org.open2jam.util.*;


/**
 *
 * @author fox
 */
public class Render implements GameWindowCallback
{
    
    public interface AutosyncDelegate {
        void autosyncFinished(double displayLag);
    }
    
    /** the config xml */
    private static final URL resources_xml = Render.class.getResource("/resources/resources.xml");

    private static final int JUDGMENT_SIZE = 64;

    /** 4 beats per minute, 4 * 60 beats per second, 4*60*1000 per millisecond */
    private static final int BEATS_PER_MSEC = 4 * 60 * 1000;
    
    private static final double DELAY_TIME = 1500;
    
    /** player options */
    private final GameOptions opt;
    
    private static final double AUTOPLAY_THRESHOLD = 40;

    /** skin info and entities */
    Skin skin;

    /** defines the judgment space */
    double judgment_line_y1;
    double judgment_line_y2;

    /** store the sound sources being played */
    private static final int MAX_SOURCES = 64;

    /** the mapping of note channels to KeyEvent keys  */
    final EnumMap<Event.Channel, Integer> keyboard_map;

    /** the mapping of note channels to KeyEvent keys  */
    final EnumMap<Config.MiscEvent, Integer> keyboard_misc;

    /** The window that is being used to render the game */
    final GameWindow window;
    
    /** The sound system to use */
    final SoundSystem soundSystem;
    
    /** The judge to judge the notes */
    final JudgmentStrategy judge;

    /** the chart being rendered */
    private final Chart chart;

    /** The recorded fps */
    int fps;

    /** the hispeed values*/
    private double last_speed;
    private double speed;
    private double next_speed;
    private static final double SPEED_STEP = 0.5d;
    
    boolean xr_speed = false;
    boolean w_speed = false;
    private final List<Double> speed_xR_values = new ArrayList<Double>();

    private static final double SPEED_FACTOR = 0.005d;
    
    //TODO make it changeable in options... maybe?
    private static final double W_SPEED_FACTOR = 0.0005d;
    private double w_speed_time = 3000d;
    double w_time = 0;
    boolean w_positive = true;

    /** the layer of the notes */
    private int note_layer;

    /** the bpm at which the entities are falling */
    private double bpm;

    /** this queue hold the available sources
     * that may be used to play sounds */
    LinkedList<Integer> source_queue;
    private Iterator<Integer> source_queue_iterator;

    /** maps the Event value to OpenGL sample ID's */
    private Map<Integer, Sound> sounds;

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

    EnumMap<JudgmentResult,NumberEntity> note_counter;
    
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
    
    Map<Integer, Sprite> bga_sprites;
    BgaEntity bgaEntity;

    int consecutive_cools = 0;

    NumberEntity minute_entity;
    NumberEntity second_entity;

    Entity judgment_entity;

    /** the combo counter */
    ComboCounterEntity combo_entity;

    /** the maxcombo counter */
    NumberEntity maxcombo_entity;

    protected Entity judgment_line;
    
    TrueTypeFont trueTypeFont;

    /** statistics variables (delete this soon) */
    double hit_sum = 0;
    double hit_count = 0;
    double total_notes = 0;
    
    /** statistics variables (new version) */
    List<Double> hits = new LinkedList<Double>();
    List<Double> lags = new LinkedList<Double>();
    
    /** display lag */
    boolean is_autosyncing = false;
    double display_lag = 0;
    double original_display_lag = 0;
    AutosyncDelegate autosyncDelegate;
    
    /** song finish time [leave 10 seconds] */
    long finish_time = -1;

    protected CompositeEntity visibility_entity;

    private final static float VOLUME_FACTOR = 0.05f;
    
    /** timing data */
    private TimingData timing = new TimingData(); 

    static {
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
    }
    
    protected final boolean AUTOSOUND;
    
    public Render(Chart chart, GameOptions opt, DisplayMode dm) throws SoundSystemException
    {
        keyboard_map = Config.getKeyboardMap(Config.KeyboardType.K7);
        keyboard_misc = Config.getKeyboardMisc();
        window = ResourceFactory.get().getGameWindow();
        
        soundSystem = new FmodExSoundSystem();
        soundSystem.setMasterVolume(opt.getMasterVolume());
        soundSystem.setBGMVolume(opt.getBGMVolume());
        soundSystem.setKeyVolume(opt.getKeyVolume());
        
        judge = new TimeJudgment();
        
        entities_matrix = new EntityMatrix();
        this.chart = chart;
        this.opt = opt;

        this.next_speed = this.last_speed = speed = opt.getHiSpeed();
        switch(opt.getSpeedType())
        {
            case xRSpeed:
            xr_speed = true;
            break;
            case WSpeed:
            w_speed = true;
            //we use the speed as a multiply to get the time
            w_speed_time = speed * 1000; 
            this.speed = this.next_speed = this.last_speed = 0;
            break;
        }
	
	AUTOSOUND = opt.getAutosound();
	
	//TODO Should get values from gameoptions, but i'm lazy as hell
	if(opt.getAutoplay()) 
	{
	    for(Event.Channel c : Event.Channel.values())
	    {
		if(c.toString().startsWith(("NOTE_")))
		    c.enableAutoplay();
	    }

//	    Event.Channel.NOTE_4.enableAutoplay();
//	    Event.Channel.NOTE_1.enableAutoplay();
	}
        
        display_lag = original_display_lag = opt.getDisplayLag();
	
        window.setDisplay(dm,opt.getVsync(),opt.getFullScreen(),opt.getBilinear());
    }

    public void setAutosync(boolean is_autosyncing) {
        this.is_autosyncing = is_autosyncing;
    }

    public void setAutosyncDelegate(AutosyncDelegate autosyncDelegate) {
        this.autosyncDelegate = autosyncDelegate;
    }

    public boolean isAutosync() {
        return is_autosyncing;
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
            SkinParser sb = new SkinParser(window.getResolutionWidth(), window.getResolutionHeight());
            SAXParserFactory.newInstance().newSAXParser().parse(resources_xml.openStream(), sb);
            if((skin = sb.getResult("o2jam")) == null){
                Logger.global.log(Level.SEVERE, "Skin load error There is no o2jam skin");
            }
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
            s.setScale(skin.getScreenScaleX(), skin.getScreenScaleY());
            s.draw(0, 0);
            window.update();
        } catch (NullPointerException e){
            Logger.global.log(Level.INFO, "No cover image on file: {0}", chart.getSource().getName());
        }

        judgment_line_y2 = skin.getJudgmentLine();

	    changeSpeed(0);

        Random rnd = new Random();

        for(int i = 0;i<chart.getKeys(); i++)
        {
            speed_xR_values.add(i, rnd.nextDouble());
        }

        bpm = chart.getBPM();

        note_layer = skin.getEntityMap().get("NOTE_1").getLayer();

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
        jamcombo_entity.setThreshold(1);
        entities_matrix.add(jamcombo_entity);

        jambar_entity = (BarEntity) skin.getEntityMap().get("JAM_BAR");
        jambar_entity.setLimit(50);
        entities_matrix.add(jambar_entity);

        lifebar_entity = (BarEntity) skin.getEntityMap().get("LIFE_BAR");
        lifebar_entity.setLimit(1000);
        lifebar_entity.setNumber(1000);
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

        visibility_entity = new CompositeEntity();
        if(opt.getVisibilityModifier() != GameOptions.VisibilityMod.None)
            visibility(opt.getVisibilityModifier());

        judgment_line = skin.getEntityMap().get("JUDGMENT_LINE");
        entities_matrix.add(judgment_line);

        for(Event.Channel c : keyboard_map.keySet())
        {
            keyboard_key_pressed.put(c, Boolean.FALSE);
            note_channels.put(c, new LinkedList<NoteEntity>());
        }
        
        note_counter = new EnumMap<JudgmentResult,NumberEntity>(JudgmentResult.class);
        for(JudgmentResult s : JudgmentResult.values()){
            NumberEntity e = (NumberEntity)skin.getEntityMap().get("COUNTER_"+s).copy();
            note_counter.put(s, e);
	    entities_matrix.add(note_counter.get(s));
        }
        start_time = lastLoopTime = SystemTimer.getTime();

        EventList event_list = construct_velocity_tree(chart.getEvents());
	
	event_list.fixEventList(EventList.FixMethod.OPEN2JAM, true);

	//Let's randomize "-"
        switch(opt.getChannelModifier())
        {
            case Mirror:
		event_list.channelMirror();
            break;
            case Shuffle:
                event_list.channelShuffle();
            break;
            case Random:
                event_list.channelRandom();
            break;
        }
	
	bgaEntity = (BgaEntity) skin.getEntityMap().get("BGA");
	entities_matrix.add(bgaEntity);
	
	bga_sprites = new HashMap<Integer, Sprite>();
	if(chart.hasVideo()) {
	    bgaEntity.isVideo = true;
	    bgaEntity.videoFile = chart.getVideo();
	    bgaEntity.initVideo();
	} else if(!chart.getBgaIndex().isEmpty()) {
	    // get all the bgaEntity sprites
	    
	    for(Entry<Integer, File> entry: chart.getImages().entrySet()) {
		BufferedImage img;
		try {
		    img = ImageIO.read(entry.getValue());
		    Sprite s = ResourceFactory.get().getSprite(img);
		    bga_sprites.put(entry.getKey(), s);
		} catch (IOException ex) {
		    java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, null, ex);
		}    
	    }
	}

	
        // adding static entities
        for(Entity e : skin.getEntityList()){
            entities_matrix.add(e);
        }
	
        // get a new iterator
        buffer_iterator = event_list.iterator();

        // load up initial buffer
        update_note_buffer(0, 0);

        // create sound sources
        source_queue = new LinkedList<Integer>();

        source_queue_iterator = source_queue.iterator();

        // get the chart sound samples
	sounds = new HashMap<Integer, Sound>();
        for(Entry<Integer, SampleData> entry : chart.getSamples().entrySet())
        {
            SampleData sampleData = entry.getValue();
            try {
                Sound sound = soundSystem.load(sampleData);
                sounds.put(entry.getKey(), sound);
            } catch (SoundSystemException ex) {
                java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, null, ex);
            }
	    try {
		entry.getValue().dispose();
	    } catch (IOException ex) {
		java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	
        trueTypeFont = new TrueTypeFont(new Font("Tahoma", Font.BOLD, 14), false);
        
        //clean up
        System.gc();

        // wait a bit.. 5 seconds at min
        SystemTimer.sleep((int) (5000 - (SystemTimer.getTime() - lastLoopTime)));

        lastLoopTime = SystemTimer.getTime();
        start_time = lastLoopTime + DELAY_TIME;
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


    /**
    * Notification that a frame is being rendered. Responsible for
    * running game logic and rendering the scene.
    */
    @Override
    public void frameRendering()
    {
        // work out how long its been since the last update, this
        // will be used to calculate how far the entities should
        // move this loop
        double now = SystemTimer.getTime();
        double delta = now - lastLoopTime;
        lastLoopTime = now;
        lastFpsTime += delta;
        fps++;

        update_fps_counter();

        check_misc_keyboard();
        
        changeSpeed(delta); // TODO: is everything here really needed every frame ?

        now = SystemTimer.getTime() - start_time;
        double now_display = now - display_lag;
        
        update_note_buffer(now, now_display);

        now = SystemTimer.getTime() - start_time;

        soundSystem.update();
	do_autoplay(now);
        Keyboard.poll();
        check_keyboard(now);

        
        for(LinkedList<Entity> layer : entities_matrix) // loop over layers
        {
            // get entity iterator from layer
            // need to use iterator here because we remove() below
            Iterator<Entity> j = layer.iterator();
            while(j.hasNext()) // loop over entities
            {
                Entity e = j.next();
                e.move(delta); // move the entity

                if(e instanceof TimeEntity)
                {
                    TimeEntity te = (TimeEntity) e;
                    //autoplays sounds play
                    if(te.getTime() - now <= 0) te.judgment();

                    //channel needed by the xR speed
                    Event.Channel channel = Event.Channel.NONE;
                    if(e instanceof NoteEntity) channel = ((NoteEntity)e).getChannel();

                    double y = getViewport() - calculateNoteDistance(now_display,te.getTime(), channel);

                    //TODO Fix this, maybe an option in the skin
                    //o2jam overlaps 1 px of the note with the measure and, because of this
                    //our skin should do it too xD
                    if(e instanceof MeasureEntity) y -= 1;
		    if(!(e instanceof BgaEntity))
			e.setPos(e.getX(), y);

                    if(e instanceof NoteEntity) check_judgment((NoteEntity)e, now);
                }

                if(e.isDead())j.remove();
                else e.draw();
            }
        }

        if(!w_speed) trueTypeFont.drawString(780, 300, "HI-SPEED: "+next_speed, 1, -1, TrueTypeFont.ALIGN_RIGHT);
	trueTypeFont.drawString(780, 330, "Current Measure: "+current_measure, 1, -1, TrueTypeFont.ALIGN_RIGHT);
        
        if(!buffer_iterator.hasNext() && entities_matrix.isEmpty(note_layer)){
            if (finish_time == -1) {
                finish_time = System.currentTimeMillis() + 10000;
            } else if (System.currentTimeMillis() > finish_time) {
                soundSystem.release();
                window.destroy();
            }
        }
    }

    private void update_fps_counter()
    {
        // update our FPS counter if a second has passed
        if (lastFpsTime >= 1000) {
            Logger.global.log(Level.FINEST, "FPS: {0}", fps);
            fps_entity.setNumber(fps);
            lastFpsTime = lastFpsTime-1000;
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
    }

    void do_autoplay(double now)
    {
        for(Event.Channel c : keyboard_map.keySet())
        {
            if(!c.isAutoplay()) continue;
	    NoteEntity ne = nextNoteKey(c);

            if(ne == null)continue;

            double hit = ne.testTimeHit(now);
            if(hit > AUTOPLAY_THRESHOLD)continue;
            ne.setHitDistance(hit);
            
            if(ne instanceof LongNoteEntity)
            {
                if(ne.getState() == NoteEntity.State.NOT_JUDGED)
                {
                    queueSample(ne.getSample());
                    ne.setState(NoteEntity.State.LN_HEAD_JUDGE);
                    Entity ee = skin.getEntityMap().get("PRESSED_"+ne.getChannel()).copy();
                    entities_matrix.add(ee);
                    Entity to_kill = key_pressed_entity.put(ne.getChannel(), ee);
                    if(to_kill != null)to_kill.setDead(true);
                }
                else if(ne.getState() == NoteEntity.State.LN_HOLD)
                {
                    ne.setState(NoteEntity.State.JUDGE);
                    longflare.get(ne.getChannel()).setDead(true); //let's kill the longflare effect
                    key_pressed_entity.get(ne.getChannel()).setDead(true);
                }
            }
            else
            {
                queueSample(ne.getSample());
                ne.setState(NoteEntity.State.JUDGE);
            }
        }
    }

    public void check_keyboard(double now)
    {
        
	for(Map.Entry<Event.Channel,Integer> entry : keyboard_map.entrySet())
        {
            Event.Channel c = entry.getKey();
	    if(c.isAutoplay()) continue;
            
            boolean keyDown = window.isKeyDown(entry.getValue());
            boolean keyWasDown = keyboard_key_pressed.get(c);
            
            if(keyDown && !keyWasDown){ // started holding now
                
                keyboard_key_pressed.put(c, true);

                Entity ee = skin.getEntityMap().get("PRESSED_"+c).copy();
                entities_matrix.add(ee);
                Entity to_kill = key_pressed_entity.put(c, ee);
                if(to_kill != null)to_kill.setDead(true);

                NoteEntity e = nextNoteKey(c);
                if(e == null){
                    Event.SoundSample i = last_sound.get(c);
                    if(i != null)queueSample(i);
                    continue;
                }

                queueSample(e.getSample());

                e.updateHit(judgment_line_y1, judgment_line_y2, now);

                // don't continue if the note is too far
                if(judge.accept(e)) {
                    if(e instanceof LongNoteEntity) {
                        longnote_holded.put(c, (LongNoteEntity) e);
                        if(e.getState() == NoteEntity.State.NOT_JUDGED)
                            e.setState(NoteEntity.State.LN_HEAD_JUDGE);
                    } else {
                        e.setState(NoteEntity.State.JUDGE);
                    }
                }
                
            }else if(!keyDown && keyWasDown) { // key released now

                keyboard_key_pressed.put(c, false);
                key_pressed_entity.get(c).setDead(true);

                Entity lf = longflare.remove(c);
                if(lf !=null)lf.setDead(true);
                
                LongNoteEntity e = longnote_holded.remove(c);
                if(e == null || e.getState() != NoteEntity.State.LN_HOLD)continue;

                e.updateHit(judgment_line_y1, judgment_line_y2, now);
                e.setState(NoteEntity.State.JUDGE);
                
            }
        }
        
    }
    
    // display hits
    private void display_hits() {
        if (hits.isEmpty()) return;
        double sum = 0;
        for (double hit : hits) {
            sum += hit;
        }
        double average = sum / hits.size();
        System.out.printf("%d %f %f\n", hits.size(), average);
    }
    
    private void autosync() {
        if (!is_autosyncing) return;
        if (lags.size() < 1) return;
        double sum = 0;
        int count = 0;
        for (double lag : lags) {
            sum += lag;
            count ++;
        }
        for (; count < 64; count ++) {
            sum += original_display_lag;
        }
        double average = sum / count;
        display_lag = average;
        System.out.printf("new display lag : %d %f\n", lags.size(), average);
    }
    
    public void check_judgment(NoteEntity ne, double now)
    {
        JudgmentResult result;
        
        switch (ne.getState())
        {
            case NOT_JUDGED: // you missed it (no keyboard input)
                ne.updateHit(judgment_line_y1, judgment_line_y2, now);
                if (judge.missed(ne)) setNoteJudgment(ne, JudgmentResult.MISS);
                break;
                
            case JUDGE: //LN & normal ones: has finished with good result
                result = judge.judge(ne);
                setNoteJudgment(ne, result);
                
                if (!(ne instanceof LongNoteEntity)) {
                    hits.add(ne.getHitTime());
                    lags.add(ne.getHitTime() + display_lag);
                    //display_hits();
                    autosync();
                }
                break;
                
            case LN_HOLD:    // You kept too much time the note held that it misses
                ne.updateHit(judgment_line_y1, judgment_line_y2, now);
                if (judge.missed(ne)) {
                    setNoteJudgment(ne, JudgmentResult.MISS);
                    
                    // kill the long flare
                    Entity lf = longflare.remove(ne.getChannel());
                    if(lf !=null)lf.setDead(true);
                }
                break;

            case LN_HEAD_JUDGE: //LN: Head has been played
  
                result = judge.judge(ne);
                setNoteJudgment(ne, result);
                    
                // display the long flare and kill the old one
                if (result != JudgmentResult.MISS) {
                    Entity ee = skin.getEntityMap().get("EFFECT_LONGFLARE").copy();
                    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,ee.getY());
                    entities_matrix.add(ee);
                    Entity to_kill = longflare.put(ne.getChannel(),ee);
                    if(to_kill != null)to_kill.setDead(true);

                    ne.setState(NoteEntity.State.LN_HOLD);
                }
                break;
                
            case TO_KILL: // this is the "garbage collector", it just removes the notes off window
                
                if(ne.getY() >= window.getResolutionHeight())
                {
                    // kill it
                    ne.setDead(true);
                }
                
            break;
                
        }
        
    }
    
    public void setNoteJudgment(NoteEntity ne, JudgmentResult result) {
        
        // statistics
        hit_sum += ne.getHitDistance();
        if(result != JudgmentResult.MISS) hit_count++;
        total_notes++;
        
        result = handleJudgment(result);
        // display the judgment
        if(judgment_entity != null)judgment_entity.setDead(true);
        judgment_entity = skin.getEntityMap().get("EFFECT_"+result).copy();
        entities_matrix.add(judgment_entity);

        // add to the statistics
        note_counter.get(result).incNumber();
        
        // for cool: display the effect
        if (result == JudgmentResult.COOL) {
            Entity ee = skin.getEntityMap().get("EFFECT_CLICK").copy();
            ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,
            getViewport()-ee.getHeight()/2);
            entities_matrix.add(ee);
        }
        
        // delete the note
        if (result == JudgmentResult.MISS || (ne instanceof LongNoteEntity)) {
            ne.setState(NoteEntity.State.TO_KILL);
        } else {
            ne.setDead(true);
        }
        
        // update combo
        if (shouldIncreaseCombo(result)) {
            combo_entity.incNumber();
        } else {
            combo_entity.resetNumber();
        }
        

    }

    public boolean shouldIncreaseCombo(JudgmentResult result) {
        if (result == null) return false;
        switch (result) {
            case BAD: case MISS: return false;
        }
        return true;
    }
    
    public JudgmentResult handleJudgment(JudgmentResult result) {

        int score_value = 0;
        
        switch(result)
        {
            case COOL:
                jambar_entity.addNumber(2);
                consecutive_cools++;
                lifebar_entity.addNumber(2);
                score_value = 200 + (jamcombo_entity.getNumber()*10);
                break;

            case GOOD:
                jambar_entity.addNumber(1);
                consecutive_cools = 0;
                score_value = 100;
                break;

            case BAD:
                if(pills_draw.size() > 0)
                {
                    result = JudgmentResult.GOOD;
                    jambar_entity.addNumber(1);
                    pills_draw.removeLast().setDead(true);

                    score_value = 100; // TODO: not sure
                }
                else
                {
                    jambar_entity.setNumber(0);
                    jamcombo_entity.resetNumber();

                    score_value = 4;
                }
                consecutive_cools = 0;
            break;

            case MISS:
                jambar_entity.setNumber(0);
                jamcombo_entity.resetNumber();
                consecutive_cools = 0;

                if(lifebar_entity.getNumber() >= 30)lifebar_entity.addNumber(-30);
                else lifebar_entity.setNumber(0);

                if(score_entity.getNumber() >= 10)score_value = -10;
                else score_value = -score_entity.getNumber();
            break;
        }
        
        score_entity.addNumber(score_value);

        if(jambar_entity.getNumber() >= jambar_entity.getLimit())
        {
            jambar_entity.setNumber(0); //reset
            jamcombo_entity.incNumber();
        }
        
        if(consecutive_cools >= 15 && pills_draw.size() < 5)
        {
            consecutive_cools -= 15;
            Entity ee = skin.getEntityMap().get("PILL_"+(pills_draw.size()+1)).copy();
            entities_matrix.add(ee);
            pills_draw.add(ee);
        }

        if(maxcombo_entity.getNumber()<(combo_entity.getNumber()))
        {
            maxcombo_entity.incNumber();
        }
        
        return result;

    }
    
    /* play a sample */
    public void queueSample(Event.SoundSample soundSample)
    {
        if(soundSample == null) return;
	
	Sound sound = sounds.get(soundSample.sample_id);
        if(sound == null)return;
        
        try {
            sound.play(soundSample.isBGM() ? SoundChannel.BGM : SoundChannel.KEY,
                    1.0f, soundSample.pan);
        } catch (SoundSystemException ex) {
            java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void change_bgm_volume(float factor)
    {
        opt.setBGMVolume(opt.getBGMVolume() + factor);
        soundSystem.setBGMVolume(opt.getBGMVolume());
    }
    
    private void change_key_volume(float factor)
    {
        opt.setKeyVolume(opt.getKeyVolume() + factor);
        soundSystem.setKeyVolume(opt.getKeyVolume());
    }
            
    private void changeSpeed(double delta)
    {
        if(w_speed)
        {
            w_time += delta;
            if(w_time < w_speed_time)
            {
                speed += (w_positive ? 1 : -1) * W_SPEED_FACTOR * delta;                
                speed = clamp(speed, 0.5, 10);
            }
            else
            {
                w_time = 0;
                w_positive = !w_positive;
            }
        }
        else
        {
            if(speed == next_speed && delta != 0) return;
            
            if(last_speed > next_speed)
            {
                speed -= SPEED_FACTOR * delta;

                if(speed < next_speed) speed = next_speed;
            }
            else if (last_speed < next_speed)
            {
                speed += SPEED_FACTOR * delta;

                if(speed > next_speed) speed = next_speed;    
            }
            else
            {
                speed = next_speed;
            }
        }
        
        judgment_line_y1 = skin.getJudgmentLine() - JUDGMENT_SIZE;
        
        //only change the offset if the speed is > 1
        //because lowers get a very tiny reaction window then...
        if(speed > 1){
            double off = JUDGMENT_SIZE * (speed-1);
            judgment_line_y1 -= off;
        }
        
        //update the longnotes end time
        for(LinkedList<Entity> layer : entities_matrix) // loop over layers
        {
            for (Entity e : layer) {
                if (e instanceof LongNoteEntity) {
                    LongNoteEntity le = (LongNoteEntity) e;
                    le.setEndDistance(calculateNoteDistance(le.getTime(), le.getEndTime(), le.getChannel()));
                }
            }
        }
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

    /* this returns the next note that needs to be played
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

    private double buffer_timer = 0;
    
    private int current_measure = 0;

    /* update the note layer of the entities_matrix.
    *** note buffering is equally distributed between the frames
    **/
    void update_note_buffer(double now, double now_display)
    {
        while(buffer_iterator.hasNext() && getViewport() - calculateNoteDistance(now_display,buffer_timer) > -10)
        {
            Event e = buffer_iterator.next();

            buffer_timer = e.getTime();
            
            switch(e.getChannel())
            {
                case MEASURE:
                    MeasureEntity m = (MeasureEntity) skin.getEntityMap().get("MEASURE_MARK").copy();
                    m.setTime(e.getTime());
                    entities_matrix.add(m);
		    
		    current_measure = e.getMeasure();
                break;
                    
                case NOTE_1:case NOTE_2:
                case NOTE_3:case NOTE_4:
                case NOTE_5:case NOTE_6:case NOTE_7:
                if(e.getFlag() == Event.Flag.NONE){
		    if(ln_buffer.containsKey(e.getChannel()))
			Logger.global.log(Level.WARNING, "There is a none in the current long {0} @ "+e.getTotalPosition(), e.getChannel());
                    NoteEntity n = (NoteEntity) skin.getEntityMap().get(e.getChannel().toString()).copy();
                    n.setTime(e.getTime());
		    
                    if(AUTOSOUND) auto_sound(e, false);
		    n.setSample(AUTOSOUND ? null : e.getSample());
		    
		    entities_matrix.add(n);
                    note_channels.get(n.getChannel()).add(n);
                }
                else if(e.getFlag() == Event.Flag.HOLD){
		    if(ln_buffer.containsKey(e.getChannel()))
			Logger.global.log(Level.WARNING, "There is a hold in the current long {0} @ "+e.getTotalPosition(), e.getChannel());
                    LongNoteEntity ln = (LongNoteEntity) skin.getEntityMap().get("LONG_"+e.getChannel()).copy();
                    ln.setTime(e.getTime());
		    
                    if(AUTOSOUND) auto_sound(e, false);
		    ln.setSample(AUTOSOUND ? null : e.getSample());
		    
		    entities_matrix.add(ln);
		    ln_buffer.put(e.getChannel(),ln);
                    note_channels.get(ln.getChannel()).add(ln);
                }
                else if(e.getFlag() == Event.Flag.RELEASE){
                    LongNoteEntity lne = ln_buffer.remove(e.getChannel());
                    if(lne == null){
                        Logger.global.log(Level.WARNING, "Attempted to RELEASE note {0} @ "+e.getTotalPosition(), e.getChannel());
                    }else{
                        lne.setEndTime(e.getTime(),calculateNoteDistance(lne.getTime(),e.getTime(), lne.getChannel()));
                    }
                }
                break;
		case BGA:
		    if(!bgaEntity.isVideo) {
			Sprite sprite = null;
			if(bga_sprites.containsKey((int)e.getValue()))
			    sprite = bga_sprites.get((int)e.getValue());
			if(sprite == null) break;
			sprite.setScale(1f, 1f);
			bgaEntity.setSprite(sprite);
		    }
		    
		    bgaEntity.setTime(e.getTime());
		break;
		    
                //TODO ADD SUPPORT
                case NOTE_SC:
                case NOTE_8:case NOTE_9:
                case NOTE_10:case NOTE_11:
                case NOTE_12:case NOTE_13:case NOTE_14:
                case NOTE_SC2:

                case AUTO_PLAY:
                    auto_sound(e, true);
                break;
            }
        }
    }
    
    private void auto_sound(Event e, boolean bgm)
    {
	if(bgm) e.getSample().toBGM();
	SampleEntity s = new SampleEntity(this,e.getSample(),0);
	s.setTime(e.getTime());
	entities_matrix.add(s);	
    }

    private final List<Integer> misc_keys = new LinkedList<Integer>();

    void check_misc_keyboard()
    {
	    for(Map.Entry<Config.MiscEvent,Integer> entry : keyboard_misc.entrySet())
        {
            Config.MiscEvent event  = entry.getKey();

            if(window.isKeyDown(entry.getValue()) && !misc_keys.contains(entry.getValue())) // this key is being pressed
            {
                misc_keys.add(entry.getValue());
                switch(event)
                {
                    case SPEED_UP:
                        last_speed = next_speed;
                        next_speed = clamp(next_speed+SPEED_STEP, 0.5, 10);
                    break;
                    case SPEED_DOWN:
                        last_speed = next_speed;
                        next_speed = clamp(next_speed-SPEED_STEP, 0.5, 10);
                    break;
                    case MAIN_VOL_UP:
                        opt.setMasterVolume(opt.getMasterVolume() + VOLUME_FACTOR);
                        soundSystem.setMasterVolume(opt.getMasterVolume());
                    break;
                    case MAIN_VOL_DOWN:
                        opt.setMasterVolume(opt.getMasterVolume() - VOLUME_FACTOR);
                        soundSystem.setMasterVolume(opt.getMasterVolume());
                    break;
                    case KEY_VOL_UP:
                        change_key_volume(VOLUME_FACTOR);
                    break;
                    case KEY_VOL_DOWN:
                        change_key_volume(-VOLUME_FACTOR);
                    break;
                    case BGM_VOL_UP:
                        change_bgm_volume(VOLUME_FACTOR);
                    break;
                    case BGM_VOL_DOWN:
                        change_bgm_volume(-VOLUME_FACTOR);
                    break;
                }
            }
            else if(!window.isKeyDown(entry.getValue()) && misc_keys.contains(entry.getValue()))
            {
                misc_keys.remove(entry.getValue());
            }
        }
    }

    private EventList construct_velocity_tree(EventList list)
    {
        int measure = 0;
        double timer = DELAY_TIME;
        double my_bpm = this.bpm;
        double frac_measure = 1;
        double measure_pointer = 0;
        double measure_size = 0.8 * getViewport();
        double my_note_speed = (my_bpm * measure_size) / BEATS_PER_MSEC;
        
        EventList new_list = new EventList();
	
        timing.add(timer, bpm);
        
	//there is always a 1st measure
	Event m = new Event(Event.Channel.MEASURE, measure, 0, 0, Event.Flag.NONE);
	m.setTime(timer);
	new_list.add(m);

        for(Event e : list)
        {
            while(e.getMeasure() > measure)
            {
                timer += (BEATS_PER_MSEC * (frac_measure-measure_pointer)) / my_bpm;
                m = new Event(Event.Channel.MEASURE, measure, 0, 0, Event.Flag.NONE);
                m.setTime(timer);
                new_list.add(m);
                measure++;
                frac_measure = 1;
                measure_pointer = 0;
            }
	    double position = e.getPosition() * frac_measure;
            timer += (BEATS_PER_MSEC * (position-measure_pointer)) / my_bpm;
            measure_pointer = position;

            switch(e.getChannel())
            {
		case STOP:
                    timing.add(timer, 0);
		    double stop_time = e.getValue();
		    if(chart.type == Chart.TYPE.BMS) {
			stop_time = (e.getValue() / 192) * BEATS_PER_MSEC / my_bpm;
		    }
                    timing.add(timer + stop_time, my_bpm);
		    timer += stop_time;
		break;
		case BPM_CHANGE:
                    my_bpm = e.getValue();
                    timing.add(timer, my_bpm);
                break;
                case TIME_SIGNATURE:
                    frac_measure = e.getValue();
                break;

                case NOTE_1:case NOTE_2:
                case NOTE_3:case NOTE_4:
                case NOTE_5:case NOTE_6:case NOTE_7:
                case NOTE_SC:
                case NOTE_8:case NOTE_9:
                case NOTE_10:case NOTE_11:
                case NOTE_12:case NOTE_13:case NOTE_14:
                case NOTE_SC2:
                case AUTO_PLAY:
		case BGA:
                    e.setTime(timer + e.getOffset());
		    if(e.getOffset() != 0) System.out.println("offset: "+e.getOffset()+" timer: "+(timer+e.getOffset()));
                break;
                    
                case MEASURE:
                    Logger.global.log(Level.WARNING, "...THE FUCK? Why is a measure event here?");
                break;
            }
            
            new_list.add(e);
        }
        
        timing.finish();
        
        return new_list;
    }

    double calculateNoteDistance(double now, double target)
    {
        double measure_size = 0.8 * getViewport();
        return speed * (timing.getBeat(target) - timing.getBeat(now)) * measure_size / 4;
    }

    double calculateNoteDistance(double now, double target, Event.Channel chan)
    {
        if(!xr_speed) return calculateNoteDistance(now, target);


        double factor = 1;

        switch(chan)
        {
            case NOTE_1: factor += speed_xR_values.get(0); break;
            case NOTE_2: factor += speed_xR_values.get(1); break;
            case NOTE_3: factor += speed_xR_values.get(2); break;
            case NOTE_4: factor += speed_xR_values.get(3); break;
            case NOTE_5: factor += speed_xR_values.get(4); break;
            case NOTE_6: factor += speed_xR_values.get(5); break;
            case NOTE_7: factor += speed_xR_values.get(6); break;
        }

        return calculateNoteDistance(now, target) * factor;
    }

    private void visibility(GameOptions.VisibilityMod value)
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
                rec  = ResourceFactory.get().doRectangle(width, height, value);
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
        if(value != GameOptions.VisibilityMod.Sudden)skin.getEntityMap().get("JUDGMENT_LINE").setLayer(layer);

        entities_matrix.add(visibility_entity);
    }

    /**
     * Notification that the game window has been closed
     */
    @Override
    public void windowClosed() {
	bgaEntity.release();
        soundSystem.release();
	System.gc();        
        if (is_autosyncing && autosyncDelegate != null) {
            autosyncDelegate.autosyncFinished(display_lag);
        }
    }
    
    private double clamp(double value, double min, double max)
    {
        return Math.min(Math.max(value, min), max);
    }
}

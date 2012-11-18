
package org.open2jam.render;

import org.open2jam.sound.SoundInstance;
import org.open2jam.game.TimingData;
import org.open2jam.game.Latency;
import com.github.dtinth.partytime.Client;
import org.open2jam.sound.FmodExSoundSystem;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.DisplayMode;
import org.open2jam.Config;
import org.open2jam.GameOptions;
import org.open2jam.game.speed.SpeedMultiplier;
import org.open2jam.game.speed.Speed;
import org.open2jam.game.position.WSpeed;
import org.open2jam.parsers.Chart;
import org.open2jam.parsers.Event;
import org.open2jam.parsers.EventList;
import org.open2jam.parsers.utils.SampleData;
import org.open2jam.render.entities.*;
import org.open2jam.game.judgment.JudgmentResult;
import org.open2jam.game.judgment.JudgmentStrategy;
import org.open2jam.game.position.HiSpeed;
import org.open2jam.game.position.NoteDistanceCalculator;
import org.open2jam.game.position.RegulSpeed;
import org.open2jam.game.position.XRSpeed;
import org.open2jam.render.lwjgl.TrueTypeFont;
import org.open2jam.sound.Sound;
import org.open2jam.sound.SoundChannel;
import org.open2jam.sound.SoundSystem;
import org.open2jam.sound.SoundSystemException;
import org.open2jam.util.*;


/**
 *
 * @author fox
 */
public class Render implements GameWindowCallback
{
    private String localMatchingServer = "";
    private int rank;
    
    public interface AutosyncCallback {
        void autosyncFinished(double displayLag);
    }
    
    /** the config xml */
    private static final URL resources_xml = Render.class.getResource("/resources/resources.xml");

    /** 4 beats per minute, 4 * 60 beats per second, 4*60*1000 per millisecond */
    private static final int BEATS_PER_MSEC = 4 * 60 * 1000;
    
    private static final double DELAY_TIME = 1500;
    
    /** player options */
    private final GameOptions opt;
    
    private static final double AUTOPLAY_THRESHOLD = 0;

    /** skin info and entities */
    Skin skin;

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
    private JudgmentStrategy judge;

    /** the chart being rendered */
    private final Chart chart;

    /** The recorded fps */
    int fps;

    /** the current "calculated" speed */
    double speed;
    
    /** the base speed multiplier */
    private Speed speedObj;
    
    /** the note distance calculator */
    private NoteDistanceCalculator distance;
    
    private boolean gameStarted = true;

    /** the layer of the notes */
    private int note_layer;

    /** the bpm at which the entities are falling */
    private double bpm;

    /** maps the Event value to Sound objects */
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
    EnumMap<Event.Channel,SampleEntity> last_sound;

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

    /** statistics variable */
    double total_notes = 0;
    
    /** display and audio latency */
    private Latency displayLatency;
    private Latency audioLatency;
    
    /** points to a latency that's currenly syncing:
     * either displayLatency, audioLatency, or null.
     */
    private Latency syncingLatency;
    
    /** what to do after autosync? */
    AutosyncCallback autosyncCallback;
    
    /** local matching */
    private Client localMatching;
    
    /** song finish time [leave 10 seconds] */
    long finish_time = -1;

    protected CompositeEntity visibility_entity;

    private final static float VOLUME_FACTOR = 0.05f;
    
    /** timing data */
    private TimingData timing = new TimingData();
    
    /** status list */
    private StatusList statusList = new StatusList();

    static {
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
    }
    
    protected final boolean AUTOSOUND;
    boolean disableAutoSound = false;
    
    public Render(Chart chart, GameOptions opt, DisplayMode dm) throws SoundSystemException
    {
        keyboard_map = Config.getKeyboardMap(Config.KeyboardType.K7);
        keyboard_misc = Config.getKeyboardMisc();
        window = ResourceFactory.get().getGameWindow();
        
        soundSystem = new FmodExSoundSystem();
        soundSystem.setMasterVolume(opt.getMasterVolume());
        soundSystem.setBGMVolume(opt.getBGMVolume());
        soundSystem.setKeyVolume(opt.getKeyVolume());
        
        entities_matrix = new EntityMatrix();
        this.chart = chart;
        this.opt = opt;
        
        // speed multiplier
        speed = opt.getSpeedMultiplier();
        speedObj = new SpeedMultiplier(speed);
        
        distance = new HiSpeed(timing, 385);
        
        // TODO: refactor this
        switch(opt.getSpeedType())
        {
            case xRSpeed:
                distance = new XRSpeed(distance);
                break;
            case WSpeed:
                distance = new WSpeed(distance, speedObj);
                break;
            case RegulSpeed:
                distance = new RegulSpeed(385);
                break;
        }
	
	AUTOSOUND = opt.isAutosound();
	
	//TODO Should get values from gameoptions, but i'm lazy as hell
	if(opt.isAutoplay()) 
	{
	    for(Event.Channel c : Event.Channel.values())
	    {
		if(c.toString().startsWith(("NOTE_")))
		    c.enableAutoplay();
	    }

//	    Event.Channel.NOTE_4.enableAutoplay();
//	    Event.Channel.NOTE_1.enableAutoplay();
	} else {
            
	    for(Event.Channel c : Event.Channel.values())
	    {
		if(c.toString().startsWith(("NOTE_")))
		    c.disableAutoplay();
	    }
        }
        
        displayLatency = new Latency(opt.getDisplayLag());
        audioLatency = new Latency(opt.getAudioLatency());
        
        statusList.add(new StatusItem() {

            @Override
            public String getText() {
                return distance + ": " + speedObj;
            }

            @Override
            public boolean isVisible() { return true; }
        });
        
        statusList.add(new StatusItem() {

            @Override
            public String getText() {
                return "Current Measure: " + current_measure;
            }

            @Override
            public boolean isVisible() { return true; }
        });
        
        window.setDisplay(dm,opt.isDisplayVsync(),opt.isDisplayFullscreen(),opt.isDisplayBilinear());
    }

    public void setAutosyncCallback(AutosyncCallback autosyncDelegate) {
        this.autosyncCallback = autosyncDelegate;
    }
    
    public void setJudge(JudgmentStrategy judge) {
        this.judge = judge;
    }

    public void setAutosyncDisplay() {
        this.syncingLatency = displayLatency;
    }
    
    public void setAutosyncAudio() {
        this.syncingLatency = audioLatency;
    }
    
    public void setStartPaused() {
        this.gameStarted = false;
    }
    
    public void setLocalMatchingServer(String text) {
        this.localMatchingServer = text;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

	changeSpeed(0);

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

        last_sound = new EnumMap<Event.Channel,SampleEntity>(Event.Channel.class);

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

        initLifeBar();
        
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
        
        judge.setTiming(timing);

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
		    java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, "{0}", ex);
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

        // get the chart sound samples
	sounds = new HashMap<Integer, Sound>();
        for(Entry<Integer, SampleData> entry : chart.getSamples().entrySet())
        {
            SampleData sampleData = entry.getValue();
            try {
                Sound sound = soundSystem.load(sampleData);
                sounds.put(entry.getKey(), sound);
            } catch (SoundSystemException ex) {
                java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, "{0}", ex);
            }
	    try {
		entry.getValue().dispose();
	    } catch (IOException ex) {
		java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, "{0}", ex);
	    }
	}
	
        trueTypeFont = new TrueTypeFont(new Font("Tahoma", Font.BOLD, 14), false);
        
        //clean up
        System.gc();

        // wait a bit.. 5 seconds at min
        SystemTimer.sleep((int) (5000 - (SystemTimer.getTime() - lastLoopTime)));

        lastLoopTime = SystemTimer.getTime();
        start_time = lastLoopTime + DELAY_TIME;
        
        try {
            String[] data = localMatchingServer.trim().split(":");
            if (data.length == 2) {
                String host = data[0];
                int port = Integer.parseInt(data[1]);
                localMatching = new Client(host, port, (long)audioLatency.getLatency());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if (localMatching != null) {
            
            gameStarted = false;
            new Thread(localMatching).start();
            statusList.add(new StatusItem() {

                @Override
                public String getText() {
                    return "" + localMatching.getStatus();
                }

                @Override
                public boolean isVisible() { return true; }
            });
	
        } else if (!gameStarted) {
            
            statusList.add(new StatusItem() {

                @Override
                public String getText() {
                    return "Press any note button to start the game.";
                }

                @Override
                public boolean isVisible() { return !gameStarted; }
            });
            
        }
        
    }
    
    /**
     * Initializes the life bar based on rank
     */
    private void initLifeBar() {
        int base = 12000; // base health bar size
        int multiplier;
        if (rank >= 2) {
            multiplier = 4; // hard coefficient
        } else if (rank >= 1) {
            multiplier = 3; // normal coefficient
        } else {
            multiplier = 2; // easy coefficient
        }
        int maxLife = base * multiplier;
        lifebar_entity.setLimit(maxLife);
        lifebar_entity.setNumber(maxLife);
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

        if (!gameStarted && localMatching != null) {
            if (localMatching.isReady()) gameStarted = true;
        }
        
        if (!gameStarted) {
            start_time = SystemTimer.getTime();
        }
        
        now = SystemTimer.getTime() - start_time;

        if (AUTOSOUND) now -= audioLatency.getLatency();
        
        double now_display = now + displayLatency.getLatency();
        
        update_note_buffer(now, now_display);
        distance.update(now_display, delta);

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
                    
                    double timeToJudge = now;
                    
                    if (e instanceof SoundEntity && AUTOSOUND) {
                        timeToJudge += audioLatency.getLatency();
                    }
                    
                    if(te.getTime() - timeToJudge <= 0) te.judgment();

                    NoteEntity ne = e instanceof NoteEntity ? (NoteEntity)e : null;
                    
                    double y = getViewport() - distance.calculate(now_display, te.getTime(), speed, ne);

                    //TODO Fix this, maybe an option in the skin
                    //o2jam overlaps 1 px of the note with the measure and, because of this
                    //our skin should do it too xD
                    if(e instanceof MeasureEntity) y -= 1;
                    
		    if(!(e instanceof BgaEntity))
			e.setPos(e.getX(), y);
                    
                    if(e instanceof LongNoteEntity) {
                        LongNoteEntity lne = (LongNoteEntity)e;
                        double ey = getViewport() - distance.calculate(now_display, lne.getEndTime(), speed, ne);
                        lne.setEndDistance(Math.abs(ey - y));
                    }

                    if(e instanceof NoteEntity) check_judgment((NoteEntity)e, now);
                }

                if(e.isDead())j.remove();
                else e.draw();
            }
        }

        int y = 300;
        
        for (String s : statusList) {
            trueTypeFont.drawString(780, y, s, 1, -1, TrueTypeFont.ALIGN_RIGHT);
            y += 30;
        }
        
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
            ne.updateHit(now);
            
            if(ne instanceof LongNoteEntity)
            {
                if(ne.getState() == NoteEntity.State.NOT_JUDGED)
                {
                    disableAutoSound = false;
                    ne.keysound();
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
                disableAutoSound = false;
                ne.keysound();
                ne.setState(NoteEntity.State.JUDGE);
            }
        }
    }

    public boolean isDisableAutoSound() {
        return disableAutoSound;
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
                
                if (!gameStarted && localMatching == null) gameStarted = true;  
                
                keyboard_key_pressed.put(c, true);

                Entity ee = skin.getEntityMap().get("PRESSED_"+c).copy();
                entities_matrix.add(ee);
                Entity to_kill = key_pressed_entity.put(c, ee);
                if(to_kill != null)to_kill.setDead(true);

                NoteEntity e = nextNoteKey(c);
                if(e == null){
                    SampleEntity i = last_sound.get(c);
                    if(i != null) i.extrasound();
                    continue;
                }

                e.updateHit(now);

                // don't continue if the note is too far
                if(judge.accept(e)) {
                    disableAutoSound = false;
                    e.keysound();
                    if(e instanceof LongNoteEntity) {
                        longnote_holded.put(c, (LongNoteEntity) e);
                        if(e.getState() == NoteEntity.State.NOT_JUDGED)
                            e.setState(NoteEntity.State.LN_HEAD_JUDGE);
                    } else {
                        e.setState(NoteEntity.State.JUDGE);
                    }
                } else {
                    e.getSampleEntity().extrasound();
                }
                
            }else if(!keyDown && keyWasDown) { // key released now

                keyboard_key_pressed.put(c, false);
                key_pressed_entity.get(c).setDead(true);

                Entity lf = longflare.remove(c);
                if(lf !=null)lf.setDead(true);
                
                LongNoteEntity e = longnote_holded.remove(c);
                if(e == null || e.getState() != NoteEntity.State.LN_HOLD)continue;

                e.updateHit(now);
                e.setState(NoteEntity.State.JUDGE);
                
            }
        }
        
    }
    
    private void autosync(double hit) {
        if (syncingLatency == null) return;
        syncingLatency.autosync(hit);
    }
    
    public void check_judgment(NoteEntity ne, double now)
    {
        JudgmentResult result;
        
        switch (ne.getState())
        {
            case NOT_JUDGED: // you missed it (no keyboard input)
                ne.updateHit(now);
                if (judge.missed(ne)) {
                    disableAutoSound = true;
                    setNoteJudgment(ne, JudgmentResult.MISS);
                }
                break;
                
            case JUDGE: //LN & normal ones: has finished with good result
                result = judge.judge(ne);
                setNoteJudgment(ne, result);
                
                if (!(ne instanceof LongNoteEntity)) {
                    autosync(ne.getHitTime());
                }
                break;
                
            case LN_HOLD:    // You kept too much time the note held that it misses
                ne.updateHit(now);
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
                } else {
                    System.out.println(ne.getTimeToJudge() + " - " + now);
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
        
        result = handleJudgment(result);
        
        // stop the sound if missed
        if (result == JudgmentResult.MISS) {
            ne.missed();
        }
        
        // display the judgment
        if(judgment_entity != null)judgment_entity.setDead(true);
        judgment_entity = skin.getEntityMap().get("EFFECT_"+result).copy();
        entities_matrix.add(judgment_entity);

        // add to the statistics
        note_counter.get(result).incNumber();
        
        // for cool: display the effect
        if (result == JudgmentResult.COOL || result == JudgmentResult.GOOD) {
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
                lifebar_entity.addNumber(rank >= 2 ? 24 : 48);
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
                    lifebar_entity.subtractNumber(120);

                    score_value = 4;
                }
                consecutive_cools = 0;
            break;

            case MISS:
                jambar_entity.setNumber(0);
                jamcombo_entity.resetNumber();
                consecutive_cools = 0;

                lifebar_entity.subtractNumber(720);

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
    public SoundInstance queueSample(Event.SoundSample soundSample)
    {
        if(soundSample == null) return null;
	
	Sound sound = sounds.get(soundSample.sample_id);
        if(sound == null)return null;
        
        try {
            return sound.play(soundSample.isBGM() ? SoundChannel.BGM : SoundChannel.KEY,
                    1.0f, soundSample.pan);
        } catch (SoundSystemException ex) {
            java.util.logging.Logger.getLogger(Render.class.getName()).log(Level.SEVERE, "{0}", ex);
            return null;
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
        speedObj.update(delta);
        speed = speedObj.getCurrentSpeed();
    }

    double getViewport() { return skin.getJudgmentLine(); }

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
        last_sound.put(c, ne.getSampleEntity());
        return ne;
    }

    private double buffer_timer = 0;
    
    private int current_measure = 0;

    /* update the note layer of the entities_matrix.
    *** note buffering is equally distributed between the frames
    **/
    void update_note_buffer(double now, double now_display)
    {
        while(buffer_iterator.hasNext() && getViewport() - distance.calculate(now_display, buffer_timer, speed, null) > -10)
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
		    
                    assignSample(n, e);
		    
		    entities_matrix.add(n);
                    note_channels.get(n.getChannel()).add(n);
                }
                else if(e.getFlag() == Event.Flag.HOLD){
		    if(ln_buffer.containsKey(e.getChannel()))
			Logger.global.log(Level.WARNING, "There is a hold in the current long {0} @ "+e.getTotalPosition(), e.getChannel());
                    LongNoteEntity ln = (LongNoteEntity) skin.getEntityMap().get("LONG_"+e.getChannel()).copy();
                    ln.setTime(e.getTime());
		    
                    assignSample(ln, e);
		    
		    entities_matrix.add(ln);
		    ln_buffer.put(e.getChannel(),ln);
                    note_channels.get(ln.getChannel()).add(ln);
                }
                else if(e.getFlag() == Event.Flag.RELEASE){
                    LongNoteEntity lne = ln_buffer.remove(e.getChannel());
                    if(lne == null){
                        Logger.global.log(Level.WARNING, "Attempted to RELEASE note {0} @ "+e.getTotalPosition(), e.getChannel());
                    }else{
                        lne.setEndTime(e.getTime());
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
                    autoSound(e, true);
                break;
            }
        }
    }
    
    private void assignSample(NoteEntity n, Event e) {
        SampleEntity sampleEntity = createSampleEntity(e, false);
        if(AUTOSOUND) {
            autoSound(sampleEntity);
            sampleEntity.setNote(true);
        }
        n.setSampleEntity(sampleEntity);
    }
    
    private SampleEntity autoSound(Event e, boolean bgm)
    {
        return autoSound(createSampleEntity(e, bgm));
    }
    
    private SampleEntity autoSound(SampleEntity se) {
        entities_matrix.add(se);
        return se;
    }
    
    private SampleEntity createSampleEntity(Event e, boolean bgm) {
	if(bgm) e.getSample().toBGM();
        SampleEntity s = new SampleEntity(this,e.getSample(),0);
        s.setTime(e.getTime());
        return s;
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
                        speedObj.increase();
                    break;
                    case SPEED_DOWN:
                        speedObj.decrease();
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
                    
                    if (!last_sound.containsKey(e.getChannel()) && e.getSample() != null) {
                        last_sound.put(e.getChannel(), createSampleEntity(e, false));
                    }
                    
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
        if (syncingLatency != null && autosyncCallback != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    autosyncCallback.autosyncFinished(syncingLatency.getLatency());
                }
            });
        }
    }
    
    private double clamp(double value, double min, double max)
    {
        return Math.min(Math.max(value, min), max);
    }
}

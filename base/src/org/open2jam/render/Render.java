package org.open2jam.render;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.parsers.SAXParserFactory;

import org.lwjgl.openal.OpenALException;
import org.lwjgl.opengl.DisplayMode;
import org.open2jam.parser.Event.Channel;

import org.open2jam.parser.ResourcesHandler;
import org.open2jam.parser.Chart;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.BPMEntity;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;
import org.open2jam.render.entities.NumberEntity;
import org.open2jam.render.entities.SampleEntity;
import org.open2jam.render.lwjgl.SoundManager;
import org.open2jam.util.Logger;

public class Render implements GameWindowCallback
{
    /** the number of keys */
    private final int NUM_KEYS = 7;

    /** store the sources being played */
    private final int MAX_SOURCES = 64;

    /** the config xml */
    private static final URL resources_xml = Render.class.getResource("/resources/resources.xml");

    /** the mapping of note channels to KeyEvent keys  */
    private static final EnumMap<Event.Channel, Integer> keyboard_map;

    /** The window that is being used to render the game */
    private final GameWindow window;

    /** the chart being rendered */
    private final Chart chart;

    /** the rank of the chart */
    private final int rank;

    /** the hispeed */
    private final double hispeed;

    /** skin info and entities */
    private Skin skin;

    /** the size of a measure */
    private double measure_size;

    /** the vertical speed of entities pixels/milliseconds */
    private double note_speed;

    /** the screen offset of the buffer */
    private double buffer_offset;

    /** the note layer, to check when it's empty */
    private int note_layer;

    /** maps the Event value to OpenGL sample ID's */
    private Map<Integer, Integer> samples;

    private Iterator<Event> buffer_iterator;
    private EnumMap<Channel, LongNoteEntity> ln_buffer;
    private EnumMap<Event.Channel,Boolean> notes_pressed;

    private NumberEntity fps_entity;

    /** the bpm at which the entities are falling */
    private double bpm;

    /** a list of list of entities.
    ** basically, each list is a layer of entities
    ** the layers are rendered in order
    ** so entities at layer X will always be rendered before layer X+1 */
    private List<LinkedList<Entity>> entities_matrix;


    private EnumMap<Event.Channel,LinkedList<NoteEntity>> note_channels;
    private EnumMap<Event.Channel,Entity> notes_pressed_entity;
    private EnumMap<Event.Channel,LongNoteEntity> note_holded;


    /** The recorded fps */
    private int fps;

    private ArrayDeque<Integer> source_queue;
    private LinkedList<Integer> sources_playing;


    /** The time at which the last rendering looped started from the point of view of the game logic */
    private long lastLoopTime;

    /** The time since the last record of fps */
    private long lastFpsTime = 0;

    static{
        ResourceFactory.get().setRenderingType(ResourceFactory.OPENGL_LWJGL);
        keyboard_map = new EnumMap<Event.Channel, Integer>(Event.Channel.class);

        keyboard_map.put(Channel.NOTE_1, KeyEvent.VK_S);
        keyboard_map.put(Channel.NOTE_2, KeyEvent.VK_D);
        keyboard_map.put(Channel.NOTE_3, KeyEvent.VK_F);
        keyboard_map.put(Channel.NOTE_4, KeyEvent.VK_SPACE);
        keyboard_map.put(Channel.NOTE_5, KeyEvent.VK_J);
        keyboard_map.put(Channel.NOTE_6, KeyEvent.VK_K);
        keyboard_map.put(Channel.NOTE_7, KeyEvent.VK_L);
    }

    public Render(Chart c, int rank, double hispeed)
    {
        this.chart = c;
        this.rank = rank;
        this.hispeed = hispeed;
        window = ResourceFactory.get().getGameWindow();
    }
        
    public void setDisplay(DisplayMode dm, boolean vsync, boolean fs) throws Exception{
        window.setDisplay(dm,vsync,fs);
    }

    public void startRendering(){
        window.setGameWindowCallback(this);
        window.setTitle(chart.getArtist()+" - "+chart.getTitle());
        window.startRendering();
    }

    /**
    * initialize the common elements for the game.
    * this is called by the window render
    */
    public void initialise()
    {
        BufferedImage img = chart.getCover();
        if(img != null){
            ResourceFactory.get().getSprite(img).draw(0,0);
            window.update();
        }
        img = null;
        System.gc();

        ResourceBuilder sb = new ResourceBuilder(this,"o2jam");
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(resources_xml.openStream(),new ResourcesHandler(sb));
        } catch (Exception e) {
            Logger.die(e);
        }
        skin = sb.getResult();

        entities_matrix = new ArrayList<LinkedList<Entity>>();

        for(int i=0; i<=skin.max_layer;i++)
            entities_matrix.add(new LinkedList<Entity>());

        measure_size = 0.8 * hispeed * getViewport();
        buffer_offset = getViewport();
        setBPM(chart.getBPM(rank));

        note_layer = skin.getEntityMap().get("NOTE_1").getLayer();

        // adding static entities
        for(Entity e : skin.getEntityList()){
            entities_matrix.get(e.getLayer()).add(e);
        }

        // build long note buffer
        ln_buffer = new EnumMap<Event.Channel,LongNoteEntity>(Event.Channel.class);

        // the notes pressed buffer
        notes_pressed = new EnumMap<Event.Channel,Boolean>(Event.Channel.class);

        // weak reference to the notes in the buffer, separated by the channel
        note_channels = new EnumMap<Event.Channel,LinkedList<NoteEntity>>(Event.Channel.class);

        notes_pressed_entity = new EnumMap<Event.Channel,Entity>(Event.Channel.class);
        note_holded = new EnumMap<Event.Channel,LongNoteEntity>(Event.Channel.class);


        ArrayList<Entity> numbers = new ArrayList<Entity>();
        for(int i=0;i<10;i++)numbers.add(skin.getEntityMap().get("NUMBER_"+i));
        fps_entity = new NumberEntity(numbers, 0, 0);
        entities_matrix.get(numbers.get(0).getLayer()).add(fps_entity);


        for(Event.Channel c : Event.note_channels)
        {
            notes_pressed.put(c, Boolean.FALSE);
            note_channels.put(c, new LinkedList<NoteEntity>());
        }

        // load up initial buffer
        buffer_iterator = chart.getEvents(rank).iterator();
        update_note_buffer();


        // create sound sources
        source_queue = new ArrayDeque<Integer>(MAX_SOURCES);
        sources_playing = new LinkedList<Integer>();

        try{
        for(int i=0;i<MAX_SOURCES;i++)source_queue.push(SoundManager.newSource()); // creates sources
        }catch(OpenALException e){Logger.warn("Couldn't create enough sources("+MAX_SOURCES+")");}

        // get the chart sound samples
        samples = chart.getSamples(rank);

        //clean up
        sb = null;
        numbers = null;
        System.gc();

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
//            window.setTitle("Render (FPS: "+fps+")");
//            Logger.log("FPS: "+fps);
            fps_entity.setNumber(fps);
            lastFpsTime = 0;
            fps = 0;
        }

        // check keyboard keys
        for(Event.Channel c : Event.note_channels)
        {
            int key = keyboard_map.get(c);
            if(window.isKeyDown(key)) // this key is being pressed
            {
                if(notes_pressed.get(c) == false){ // started holding now
                    notes_pressed.put(c, true);

                    Entity ee = skin.getEntityMap().get("PRESSED_"+c).copy();
                    entities_matrix.get(ee.getLayer()).add(ee);
                    notes_pressed_entity.put(c, ee);

                    if(note_channels.get(c).isEmpty())continue;
                    NoteEntity e = note_channels.get(c).getFirst();

                    double prec = e.getStartY() - skin.judgment.start;

                    queueSample(e.getSample());

                    if(Math.abs(prec) < 2*skin.judgment.size*hispeed){

                        if(e instanceof LongNoteEntity){
                            note_holded.put(c, (LongNoteEntity) e);
                        }else{
                            e.setAlive(false);
                            note_channels.get(c).removeFirst();
                        }

                        ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
                        ee.setX(e.getX()+e.getWidth()/2-ee.getWidth()/2);
                        ee.setY(getViewport()-ee.getHeight()/2);
                        entities_matrix.get(ee.getLayer()).add(ee);
                    }
                }
            }
            else
            if(notes_pressed.get(c) == true) { // key released

                notes_pressed.put(c, false);
                notes_pressed_entity.get(c).setAlive(false);

                LongNoteEntity e = note_holded.get(c);
                if(e == null || note_channels.get(c).isEmpty()
                        || e != note_channels.get(c).getFirst())continue;

                note_holded.put(c,null);

                double prec = e.getY()- skin.judgment.start;

                if(e instanceof LongNoteEntity && Math.abs(prec) < 2*skin.judgment.size*hispeed){
                    e.setAlive(false);
                    note_channels.get(c).removeFirst();

                    Entity ee = skin.getEntityMap().get("EFFECT_CLICK_1").copy();
                    ee.setX(e.getX()+e.getWidth()/2-ee.getWidth()/2);
                    ee.setY(getViewport()-ee.getHeight()/2);
                    entities_matrix.get(ee.getLayer()).add(ee);
                }
            }
        }

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

                if(e.getY() >= skin.judgment.start && !(e instanceof NoteEntity)){
                    e.judgment();
                }
                else
                if(e.isAlive() && e instanceof NoteEntity && e.getY() > window.getResolutionHeight()){
                    e.setAlive(false);
                    note_channels.get(e.getChannel()).removeFirst();
                }

                if(!e.isAlive())j.remove();
                else 
                if(!(e instanceof NoteEntity) || e.getY() < skin.judgment.start+skin.judgment.size)e.draw();
            }
        }

        buffer_offset += note_speed * delta; // walk with the buffer

        
        if(!buffer_iterator.hasNext() && entities_matrix.get(note_layer).isEmpty() && sources_playing.isEmpty()){
            window.destroy();
            return;
        }
    }

    public void setBPM(double e)
    {
        this.bpm = e;
        note_speed = ((bpm/240) * measure_size) / 1000.0d;
    }

    /** returns the note speed in pixels/milliseconds */
    public double getNoteSpeed() { return note_speed; }

    public double getBPM() { return bpm; }
    public double getMeasureSize() { return measure_size; }
    public double getViewport() { return skin.judgment.start+skin.judgment.size; }


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
                m.setY(buffer_offset+6);
                entities_matrix.get(m.getLayer()).add(m);
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
                entities_matrix.get(0).add(new BPMEntity(this,e.getValue(),abs_height));
                break;

                case NOTE_1:case NOTE_2:
                case NOTE_3:case NOTE_4:
                case NOTE_5:case NOTE_6:case NOTE_7:
                if(e.getFlag() == Event.Flag.NONE){
                    NoteEntity n = (NoteEntity) skin.getEntityMap().get(e.getChannel().toString()).copy();
                    n.setY(abs_height);
                    n.setSample((int)e.getValue());
                    entities_matrix.get(n.getLayer()).add(n);
                    note_channels.get(n.getChannel()).add(n);
                }
                else if(e.getFlag() == Event.Flag.HOLD){
                    LongNoteEntity ln = (LongNoteEntity) skin.getEntityMap().get("LONG_"+e.getChannel()).copy();
                    ln.setY(abs_height);
                    ln.setSample((int)e.getValue());
                    ln_buffer.put(e.getChannel(),ln);
                    entities_matrix.get(ln.getLayer()).add(ln);
                    note_channels.get(ln.getChannel()).add(ln);
                }
                else if(e.getFlag() == Event.Flag.RELEASE){
                    if(ln_buffer.get(e.getChannel()) == null){
                        Logger.log("Attempted to RELEASE note "+e.getChannel());
                    }else{
                        ln_buffer.get(e.getChannel()).setEndY(abs_height);
                        ln_buffer.remove(e.getChannel());
                    }
                }
                break;
                case AUTO_PLAY:
                entities_matrix.get(0).add(new SampleEntity(this,(int)e.getValue(),abs_height));
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

    public void queueSample(int sample_value)
    {
        Integer buffer = samples.get(sample_value);
        if(buffer == null)return;
        if(source_queue.isEmpty()){
            Logger.log("Source queue exausted !");
            return;
        }
        Integer source = source_queue.pollFirst();
        SoundManager.play(source, buffer);
        sources_playing.add(source);
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


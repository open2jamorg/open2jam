/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.gameplays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.open2jam.GameOptions;
import org.open2jam.screen2d.Actors.EGroup;
import org.open2jam.entities.Entity;
import org.open2jam.entities.LongNoteEntity;
import org.open2jam.entities.MeasureEntity;
import org.open2jam.entities.NoteEntity;
import org.open2jam.entities.SampleEntity;
import org.open2jam.entities.TimeEntity;
import org.open2jam.parsers.Chart;
import org.open2jam.parsers.Event;
import org.open2jam.screen2d.skin.Skin;
import org.open2jam.utils.Interval;
import org.open2jam.utils.IntervalTree;
import org.open2jam.utils.Logger;
import org.open2jam.utils.SystemTimer;

/**
 *
 * @author CdK
 */
public class O2JamPlay extends BasicPlay {
    
    private static final byte JUDGMENT_SIZE = 64;
    
    private static final int DELAY_TIME = 1500 * 1000;
    
    private static final byte AUTOPLAY_THRESHOLD = 40;
    
    private enum JUDGE {
        COOL(0.8f), GOOD(0.5f), BAD(0.2f), MISS(0f);

        final float value;

        private JUDGE(float i){ value = i; }

        @Override
        public String toString() {
            return "JUDGMENT_" + super.toString();
        }
    }

    private static final float COMBO_THRESHOLD = JUDGE.GOOD.value;
    
    private float judgment_line_y1;
    private float judgment_line_y2;
    
    private double speed = 1;
    
    /** this is used by the update_note_buffer
     * to remember the "opened" long-notes */
    private EnumMap<Event.Channel, LongNoteEntity> ln_buffer;
    
    private final IntervalTree<Long,Double> velocity_tree;
    
    public O2JamPlay(Stage stage, Skin skin, Chart chart, GameOptions opt, long time) 
    {
	super(stage, skin, chart, opt, time);
	
	velocity_tree = new IntervalTree<Long,Double>();
	ln_buffer = new EnumMap<Event.Channel,LongNoteEntity>(Event.Channel.class);
    }
    
    @Override
    public boolean init() {
	bpm = chart.getBPM();
	
	judgment_line_y1 = JUDGMENT_SIZE;
	judgment_line_y2 = -JUDGMENT_SIZE;
	
        //only change the offset if the speed is > 1
        //because lowers get a very tiny reaction window then...	
        if(speed > 1){
            double off = JUDGMENT_SIZE * (speed-1);
            judgment_line_y1 -= off;
        }
	
	List<Event> event_list = construct_velocity_tree(chart.getEvents());
	
	buffer = event_list.iterator();
	
	updateEventBuffer(0);
	
	speed = opt.getHiSpeed();
	
	notes.setVisibility(opt.getVisibilityModifier());
	notes.setVisibilityPoints(0.1f, 0f, -0.2f, -0.3f);
	
	return true;
    }

    @Override
    public boolean update(long now) {
	updateEventBuffer(now);
	checkControls(now);
	doAutoplay(now);
	
	for(Group p : notes.parent.getGroups())
	{
	    Iterator<Entity> it = ((EGroup)p).getEntities().iterator();
	    while(it.hasNext())
	    {
		Entity e = it.next();

		    if(e instanceof TimeEntity)
		    {
			TimeEntity te = (TimeEntity) e;
			//autoplays sounds play
			if(te.getTime() - now <= 0) te.judgment();

			//channel needed by the xR speed
			Event.Channel channel = Event.Channel.NONE;
			if(e instanceof NoteEntity) channel = ((NoteEntity)e).getChannel();

			float y = velocity_integral(now,te.getTime());
			//System.out.println(now+" "+te.getTime()+" "+velocity_integral(now,te.getTime()));

			//TODO Fix this, maybe an option in the skin
			//o2jam overlaps 1 px of the note with the measure and, because of this
			//our skin should do it too xD
			if(e instanceof MeasureEntity){y -= 1; }
			e.y = y;

			if(e.getEndY() < judgment_line_y2) e.markToRemove(true);
			//if(e instanceof NoteEntity) check_judgment((NoteEntity)e, now);
		    }
	    }
	}

	return false;
    }

    @Override
    void checkKeyboard(long now) {
	for(Map.Entry<Event.Channel,Integer> entry : keyboard_map.entrySet())
	{
	    Event.Channel c = entry.getKey();
	    
	    if(c.isAutoplay()) continue;
	    
	    if(Gdx.input.isKeyPressed(entry.getValue()))
	    {
		if(!keyboard_key_pressed.get(c))
		{
		    keyboard_key_pressed.put(c, true);
		    togglePressed(c);
		}
	    }
	    else if(keyboard_key_pressed.get(c)) {
		keyboard_key_pressed.put(c, false);
		togglePressed(c);
	    }
	}
    }
    

    @Override
    public void doAutoplay(long now) {
	
    }

    @Override
    public void checkJudgment(Entity e, long now) {
	
    }
    
    @Override
    public boolean updateEventBuffer(long now) {
	while(buffer.hasNext() && notes.height - velocity_integral(now,buffer_timer) > -10)
        {
            Event e = buffer.next();

            buffer_timer = e.getTime();
            
            switch(e.getChannel())
            {
                case MEASURE:
                    MeasureEntity m = ((MeasureEntity) skin.getEntityMap().get("MEASURE_MARK")).copy();
                    m.setTime(e.getTime());
                    measures.addActor(m);
                break;
                    
                case NOTE_1:case NOTE_2:
                case NOTE_3:case NOTE_4:
                case NOTE_5:case NOTE_6:case NOTE_7:
                if(e.getFlag() == Event.Flag.NONE){
                    NoteEntity n = ((NoteEntity) skin.getEntityMap().get(e.getChannel().toString())).copy();
                    n.setTime(e.getTime());
		    
                    if(AUTOSOUND) auto_sound(e, false);
		    n.setSample(AUTOSOUND ? null : e.getSample());
		    
		    notes.addActor(n);
                    //note_channels.get(n.getChannel()).add(n);
                }
                else if(e.getFlag() == Event.Flag.HOLD){
                    LongNoteEntity ln = ((LongNoteEntity) skin.getEntityMap().get("LONG_"+e.getChannel())).copy();
                    ln.setTime(e.getTime());
		    
                    if(AUTOSOUND) auto_sound(e, false);
		    ln.setSample(AUTOSOUND ? null : e.getSample());
		    
		    notes.addActor(ln);
		    ln_buffer.put(e.getChannel(),ln);
                    //note_channels.get(ln.getChannel()).add(ln);
                }
                else if(e.getFlag() == Event.Flag.RELEASE){
                    LongNoteEntity lne = ln_buffer.remove(e.getChannel());
                    if(lne == null){
                        Logger.global.log(Level.WARNING, "Attempted to RELEASE note {0}", e.getChannel());
                    }else{
                        //lne.setEndTime(e.getTime(),velocity_integral(lne.getTime(),e.getTime(), lne.getChannel()));
			lne.setEndTime(e.getTime(),velocity_integral(lne.getTime(),e.getTime()));
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
                    auto_sound(e, true);
                break;
            }
        }
	
	return true;
    }

    private void auto_sound(Event e, boolean bgm)
    {
//	if(bgm) e.getSample().toBGM();
	SampleEntity s = new SampleEntity(e.getSample());
	s.setTime(e.getTime());
	notes.addActor(s);
	//entities_matrix.add(s);	
    }
    
    private List<Event> construct_velocity_tree(List<Event> list)
    {
        int measure = 0;
        long timer = DELAY_TIME;
        long last_bpm_change = 0;
        double my_bpm = this.bpm;
        double frac_measure = 1;
        double measure_pointer = 0;
        double measure_size = 0.8 * stage.height(); //TODO selectable
        double my_note_speed = (my_bpm * measure_size) / SystemTimer.BEATS_PER_MICRO;
        
        List<Event> new_list = new LinkedList<Event>();

        for(Event e : list)
        {
            while(e.getMeasure() > measure)
            {
                timer += (SystemTimer.BEATS_PER_MICRO * (frac_measure-measure_pointer)) / my_bpm;
                Event m = new Event(Event.Channel.MEASURE, measure, 0, 0, Event.Flag.NONE);
                m.setTime(timer);
                new_list.add(m);
                measure++;
                frac_measure = 1;
                measure_pointer = 0;
            }
	    double position = e.getPosition() * frac_measure;
            timer += (SystemTimer.BEATS_PER_MICRO * (position-measure_pointer)) / my_bpm;
            measure_pointer = position;

            switch(e.getChannel())
            {
		case STOP:
		    long t = Math.round(timer+e.getValue());
		    velocity_tree.addInterval(last_bpm_change, timer, my_note_speed);
		    velocity_tree.addInterval(timer, t, 0d);
		    last_bpm_change = timer = t;
		break;
		case BPM_CHANGE:
                    velocity_tree.addInterval(last_bpm_change, timer, my_note_speed);
                    my_bpm = e.getValue();
                    my_note_speed = (my_bpm * measure_size) / SystemTimer.BEATS_PER_MICRO;
                    last_bpm_change = timer;
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
                    e.setTime(timer + e.getOffset());
		    if(e.getOffset() != 0) System.out.println("offset: "+e.getOffset()+" timer: "+(timer+e.getOffset()));
                break;
                    
                case MEASURE:
                    Logger.global.log(Level.WARNING, "...THE FUCK? Why is a measure event here?");
                break;
            }
            
            new_list.add(e);
        }
        // pad 10s to make sure the song ends
        velocity_tree.addInterval(last_bpm_change, timer+10000, my_note_speed);
        velocity_tree.build();
        
        return new_list;
    }

    /*
     * given a time segment, returns the distance, in pixels,
     * from each segment based on the bpm.
     *
     * segment types returned by velocity_tree:
     *  a    t0    b   t1  ->  b - t0
     * t0     a   t1    b  -> t1 -  a
     * t0     a    b   t1  ->  b -  a
     *  a    t0   t1    b  -> t1 - t0
     */
    float velocity_integral(long t0, long t1)
    {
        boolean negative = false;
        if(t0 > t1){
            long tmp = t1;t1 = t0;t0 = tmp; // swap
            negative = true;
        }
        List<Interval<Long,Double>> list = velocity_tree.getIntervals(t0, t1);
        double integral = 0;
        for(Interval<Long,Double> i : list)
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
        return (float) ((negative ? -integral : integral) * speed);
    }
    
    private void togglePressed(Event.Channel c)
    {
	toggleVisibility(Skin.entityID.valueOf("PRESSED_"+c.name()+"_KEY"));
	toggleVisibility(Skin.entityID.valueOf("PRESSED_"+c.name()+"_LANE"));
    }
    
    private JUDGE ratePrecision(double hit)
    {
        if(hit >= JUDGE.COOL.value) // COOL
            return JUDGE.COOL;
        else
        if(hit >= JUDGE.GOOD.value) // GOOD
            return JUDGE.GOOD;
        else
        if(hit >= JUDGE.BAD.value) // BAD
            return JUDGE.BAD;
        else                        // MISS
            return JUDGE.MISS;
    }
}

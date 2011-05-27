package org.open2jam.render;

import java.util.EnumMap;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import org.open2jam.util.Logger;
import org.open2jam.util.SystemTimer;

import org.open2jam.parser.Chart;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.MeasureEntity;
import org.open2jam.render.entities.NoteEntity;
import org.open2jam.render.entities.NumberEntity;
import org.open2jam.render.entities.TimeEntity;
import org.open2jam.render.lwjgl.SoundManager;


public class DistanceRender extends Render
{
    private enum JUDGE {
        COOL(0.8), GOOD(0.5), BAD(0.2), MISS(0);

        final double value;

        private JUDGE(double i){ value = i; }

        @Override
        public String toString() {
            return "JUDGMENT_" + super.toString();
        }
    }

    private static final double AUTOPLAY_THRESHOLD = 0.8;
    private static final double COMBO_THRESHOLD = JUDGE.GOOD.value;

    private EnumMap<JUDGE,NumberEntity> note_counter;

    public DistanceRender(Chart c, double hispeed, boolean autoplay, int channelModifier, int visibilityModifier, int mainVol, int keyVol, int bgmVol)
    {
        super(c,hispeed,autoplay,channelModifier,visibilityModifier, mainVol, keyVol, bgmVol);
    }

    /**
    * initialize the common elements for the game.
    * this is called by the window render
    */
    @Override
    public void initialise()
    {
        super.initialise();
        note_counter = new EnumMap<JUDGE,NumberEntity>(JUDGE.class);
        for(JUDGE s : JUDGE.values()){
            NumberEntity e = (NumberEntity)skin.getEntityMap().get("COUNTER_"+s.toString()).copy();
            note_counter.put(s, e);
	    entities_matrix.add(note_counter.get(s));
        }
        start_time = lastLoopTime = SystemTimer.getTime();
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

        check_misc_keyboard();
        
        now = SystemTimer.getTime() - start_time;
        update_note_buffer(now);

        now = SystemTimer.getTime() - start_time;
        
	if(AUTOPLAY)do_autoplay();
        else check_keyboard();

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
                    if(te.getTime() - now <= 0)
                    {
                        te.judgment();
                    }
                    if(e instanceof MeasureEntity) y += e.getHeight()*2;
                    e.setPos(e.getX(), y);

                    if(e instanceof NoteEntity){
                        check_judgment((NoteEntity)e);
                    }
                }

                if(e.isDead())j.remove();
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

    private void check_judgment(NoteEntity ne)
    {
        JUDGE judge;
        switch (ne.getState())
        {
            case NOT_JUDGED: // you missed it (no keyboard input)
                if((ne instanceof LongNoteEntity && ne.getStartY() >= judgmentArea()) //needed by the ln head
                        || (ne.getY() >= judgmentArea()))
                {
                    if(judgment_entity != null)judgment_entity.setDead(true);
                    judgment_entity = skin.getEntityMap().get("EFFECT_"+JUDGE.MISS).copy();
                    entities_matrix.add(judgment_entity);

                    note_counter.get(JUDGE.MISS).incNumber();
                    combo_entity.resetNumber();

                    update_screen_info(JUDGE.MISS,ne.getHit());
                    
                    ne.setState(NoteEntity.State.TO_KILL);
                 }
            break;
            case JUDGE: //LN & normal ones: has finished with good result
                    judge = ratePrecision(ne.getHit());

                judge = update_screen_info(judge,ne.getHit());

                if(judgment_entity != null)judgment_entity.setDead(true);
                judgment_entity = skin.getEntityMap().get("EFFECT_"+judge).copy();
                entities_matrix.add(judgment_entity);

		note_counter.get(judge).incNumber();

		if(!judge.equals(JUDGE.MISS))
                {
		    Entity ee = skin.getEntityMap().get("EFFECT_CLICK").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,
		    getViewport()-ee.getHeight()/2);
		    entities_matrix.add(ee);

		    if(ne.getHit() >= COMBO_THRESHOLD)
                        combo_entity.incNumber();
		    else {
                        if(judge.equals(JUDGE.GOOD))combo_entity.incNumber(); //because of the pills
                        else combo_entity.resetNumber();
                    }
                    if(ne instanceof LongNoteEntity)ne.setState(NoteEntity.State.TO_KILL);
                    else ne.setDead(true);
                } else {
                    combo_entity.resetNumber();
                    ne.setState(NoteEntity.State.TO_KILL);
                }
            break;
            case LN_HEAD_JUDGE: //LN: Head has been played
                judge = ratePrecision(ne.getHit());

                judge = update_screen_info(judge,ne.getHit());

                if(judgment_entity != null)judgment_entity.setDead(true);
                judgment_entity = skin.getEntityMap().get("EFFECT_"+judge).copy();
                entities_matrix.add(judgment_entity);

		note_counter.get(judge).incNumber();

		if(!judge.equals(JUDGE.MISS))
                {
		    Entity ee = skin.getEntityMap().get("EFFECT_LONGFLARE").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,ee.getY());
		    entities_matrix.add(ee);
                    Entity to_kill = longflare.put(ne.getChannel(),ee);
                    if(to_kill != null)to_kill.setDead(true);
		    
		    ee = skin.getEntityMap().get("EFFECT_CLICK").copy();
		    ee.setPos(ne.getX()+ne.getWidth()/2-ee.getWidth()/2,
		    getViewport()-ee.getHeight()/2);
		    entities_matrix.add(ee);

		    if(ne.getHit() >= COMBO_THRESHOLD)
                        combo_entity.incNumber();
		    else {
                        if(judge.equals(JUDGE.GOOD))combo_entity.incNumber(); //because of the pills
                        else combo_entity.resetNumber();
                    }
                    ne.setState(NoteEntity.State.LN_HOLD);
                }
            break;
            case LN_HOLD:    // You keept too much time the note held that it misses
                if(ne.getY() >= judgmentArea())
                {
                    if(judgment_entity != null)judgment_entity.setDead(true);
                    judgment_entity = skin.getEntityMap().get("EFFECT_"+JUDGE.MISS).copy();
                    entities_matrix.add(judgment_entity);

                    note_counter.get(JUDGE.MISS).incNumber();
                    combo_entity.resetNumber();

                    update_screen_info(JUDGE.MISS,ne.getHit());

                    ne.setState(NoteEntity.State.TO_KILL);
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

    /**
     * TODO It would be nice to move all the check_judgment non judgment things to this function
     * (bars, combos, counter, etc)
     * @param judge
     * @return judge
     */
    private JUDGE update_screen_info(JUDGE judge, double hit)
    {
        int score_value = 0;
        switch(judge)
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
                    judge = JUDGE.GOOD;
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

        hit_sum += hit;
        if(!judge.equals(JUDGE.MISS))hit_count++;
        total_notes++;
        
        return judge;
    }

    private void do_autoplay()
    {
        for(Event.Channel c : keyboard_map.keySet())
        {
            NoteEntity ne = nextNoteKey(c);

            if(ne == null)continue;

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

    private void check_keyboard()
    {
	for(Map.Entry<Event.Channel,Integer> entry : keyboard_map.entrySet())
        {
            Event.Channel c = entry.getKey();
            if(window.isKeyDown(entry.getValue())) // this key is being pressed
            {
                if(!keyboard_key_pressed.get(c)){ // started holding now
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
                   
                    JUDGE judge;
                    double hit = e.testHit(judgment_line_y1, judgment_line_y2);
                    judge = ratePrecision(hit);
                    e.setHit(hit);

                    /* we compare the judgment with a MISS, misses should be ignored here,
                     * because this is the case where the player pressed the note so soon
                     * that it's worse than BAD ( 20% or below on o2jam) so we need to let
                     * it pass like nothing happened */
                    if(!judge.equals(JUDGE.MISS)){
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
            if(keyboard_key_pressed.get(c)) { // key released now

                keyboard_key_pressed.put(c, false);
                key_pressed_entity.get(c).setDead(true);

                LongNoteEntity e = longnote_holded.remove(c);

                Entity lf = longflare.remove(c);
                if(lf !=null)lf.setDead(true);

                if(e == null || e.getState() != NoteEntity.State.LN_HOLD)continue;

                double hit = e.testHit(judgment_line_y1, judgment_line_y2);
                e.setHit(hit);

                e.setState(NoteEntity.State.JUDGE);
            }
        }
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


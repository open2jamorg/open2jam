package org.open2jam.render;

import java.util.Map;
import java.util.EnumMap;

import org.open2jam.util.SystemTimer;

import org.open2jam.parser.Chart;
import org.open2jam.parser.Event;
import org.open2jam.render.entities.Entity;
import org.open2jam.render.entities.LongNoteEntity;
import org.open2jam.render.entities.NoteEntity;
import org.open2jam.render.entities.NumberEntity;


public class TimeRender extends Render
{
    private enum JUDGE {
        PERFECT(20), COOL(41), GOOD(125), BAD(173), MISS(-1);
        
        final int value;

        private JUDGE(int i){ value = i; }

        @Override
        public String toString() {
            return "JUDGMENT_" + super.toString();
        }
    }
    
    private static final int COMBO_THRESHOLD = JUDGE.GOOD.value;

    private EnumMap<JUDGE,NumberEntity> note_counter;

    public TimeRender(Chart c, double hispeed, int speed_type, boolean autoplay, int channelModifier, int visibilityModifier)
    {
        super(c,hispeed,speed_type,autoplay,channelModifier,visibilityModifier);
    }

    @Override
    public void initialise()
    {
        super.initialise();
        note_counter = new EnumMap<JUDGE,NumberEntity>(JUDGE.class);

        //TODO: find place to put the perfect counter first
        for(JUDGE s : new JUDGE[]{JUDGE.MISS,JUDGE.BAD,JUDGE.GOOD,JUDGE.COOL}){
            NumberEntity e = (NumberEntity)skin.getEntityMap().get("COUNTER_"+s.toString()).copy();
            note_counter.put(s, e);
	    entities_matrix.add(note_counter.get(s));
        }
        note_counter.put(JUDGE.PERFECT, note_counter.get(JUDGE.COOL));       
        start_time = lastLoopTime = SystemTimer.getTime();
    }

    @Override
    void check_judgment(NoteEntity ne)
    {
        JUDGE judge;
        switch (ne.getState())
        {
            case NOT_JUDGED: // you missed it (no keyboard input)
                if((ne instanceof LongNoteEntity && ne.getStartY() >= judgmentArea()) //needed by the ln head
                        || (ne.getY() >= judgmentArea())) // TODO: compare the time, not the position
                {
                    if(judgment_entity != null)judgment_entity.setDead(true);
                    judgment_entity = skin.getEntityMap().get("EFFECT_"+JUDGE.MISS.toString()).copy();
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

		    if(ne.getHit() <= COMBO_THRESHOLD)
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

		    if(ne.getHit() <= COMBO_THRESHOLD)
                        combo_entity.incNumber();
		    else {
                        if(judge.equals(JUDGE.GOOD))combo_entity.incNumber(); //because of the pills
                        else combo_entity.resetNumber();
                    }
                    ne.setState(NoteEntity.State.LN_HOLD);
                }
            break;
            case LN_HOLD:    // You keept too much time the note held that it misses
                if(ne.getY() >= judgmentArea()) // TODO: use the time
                {
                    if(judgment_entity != null)judgment_entity.setDead(true);
                    judgment_entity = skin.getEntityMap().get("EFFECT_"+JUDGE.MISS.toString()).copy();
                    entities_matrix.add(judgment_entity);

                    note_counter.get(JUDGE.MISS).incNumber();
                    combo_entity.resetNumber();

                    update_screen_info(JUDGE.MISS,ne.getHit());

                    ne.setState(NoteEntity.State.TO_KILL);
                 }
            break;
            case TO_KILL: // this is the "garbage collector", it just removes the notes off window
                if(ne.getY() > judgmentArea())
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
            case PERFECT:
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

        hit_sum += (hit > JUDGE.BAD.value ? 0 : (JUDGE.BAD.value - hit)/JUDGE.BAD.value);
        if(!judge.equals(JUDGE.MISS))hit_count++;
        total_notes++;
        
        return judge;
    }

    @Override
    void check_keyboard(double now)
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
                   
                    double hit = e.testTimeHit(now);
                    JUDGE judge = ratePrecision(hit);
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

                double hit = e.testTimeHit(now);
                e.setHit(hit);

                e.setState(NoteEntity.State.JUDGE);
            }
        }
    }

    private JUDGE ratePrecision(double hit)
    {
        if(hit <= JUDGE.PERFECT.value)  // PERFECT
            return JUDGE.PERFECT;
        else
        if(hit <= JUDGE.COOL.value)  // COOL
            return JUDGE.COOL;
        else
        if(hit <= JUDGE.GOOD.value) // GOOD
            return JUDGE.GOOD;
        else
        if(hit <= JUDGE.BAD.value) // BAD
            return JUDGE.BAD;
        else           // MISS
            return JUDGE.MISS;
    }
}


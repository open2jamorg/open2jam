/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.gameplays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.EnumMap;
import java.util.Iterator;
import org.open2jam.Config;
import org.open2jam.GameOptions;
import org.open2jam.screen2d.Actors.EGroup;
import org.open2jam.entities.Entity;
import org.open2jam.parsers.Chart;
import org.open2jam.parsers.Event;
import org.open2jam.screen2d.skin.Skin;
import org.open2jam.screen2d.skin.Skin.entityID;

/**
 *
 * @author CdK
 */
public class BasicPlay implements Gameplay {

    final Stage stage;
    final Skin skin;
    final EGroup player;
    final EGroup notes;
    final EGroup measures;
    final Chart chart;
    final GameOptions opt;
    
    final long start_time;
    long now;
    
    Iterator<Event> buffer;
    long buffer_timer = 0;
    
    /** the mapping of note channels to KeyEvent keys  */
    final EnumMap<Event.Channel, Integer> keyboard_map;

    /** the mapping of note channels to KeyEvent keys  */
    final EnumMap<Config.MiscEvent, Integer> keyboard_misc;
    
    /** the bpm at which the entities are falling */
    double bpm;
    
    final boolean AUTOSOUND;
    
    EnumMap<Event.Channel,Boolean> keyboard_key_pressed;
    
    public BasicPlay(Stage stage, Skin skin, Chart chart, GameOptions opt, long time) 
    {
	this.stage = stage;
	this.skin = skin;
	this.chart = chart;
	this.opt = opt;
	this.start_time = time;
	
	player = (EGroup) stage.findActor("player");
	notes = (EGroup) stage.findActor("notes");
	measures = (EGroup) stage.findActor("measures");
	
	AUTOSOUND = opt.getAutosound();
	
        keyboard_map = Config.getKeyboardMap(Config.KeyboardType.K7);
        keyboard_misc = Config.getKeyboardMisc();
	keyboard_key_pressed = new EnumMap<Event.Channel, Boolean>(Event.Channel.class);
	
        for(Event.Channel c : keyboard_map.keySet())
            keyboard_key_pressed.put(c, Boolean.FALSE);
    }

    @Override
    public boolean init() {
	return false;
    }
    
    @Override
    public boolean update(long now) {
	return false;
    }
    
    public Long startTime() { return start_time; }

    @Override
    public void checkControls(long now) {
	if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
	
	checkKeyboard(now);
	checkJoypad(now);
	checkMouse(now);
	checkTouch(now);
    }

    void checkKeyboard(long now) {
	return;
    }

    void checkMouse(long now) {
	return;
    }

    void checkJoypad(long now) {
	return;
    }

    void checkTouch(long now) {
	return;
    }
    
    @Override
    public void doAutoplay(long now) {
	return;
    }

    @Override
    public void checkJudgment(Entity e, long now) {
	return;
    }

    @Override
    public boolean updateEventBuffer(long now) {
	return false;
    }

    void toggleVisibility(entityID id) {
	Actor a = null;
	if(stage.findActor(id.name()) == null)
	{
	    Entity e = (Entity) skin.getEntityMap().get(id.name());
	    EGroup g = (EGroup) stage.findActor(e.group_name);
	    if(g.getActors().size() < e.z_index)
		g.addActor(e);
	    else
		g.addActorAt(e.z_index, e);
	    a = e;
	}
	else
	    a = stage.findActor(id.name());
	
	if(a == null) {System.out.println("WTF toggle in a null actor"); return;}
	a.visible = !a.visible;
    }
}

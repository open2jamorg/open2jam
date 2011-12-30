package org.open2jam.screen2d.skin;

import com.badlogic.gdx.scenes.scene2d.Actor;
import java.util.HashMap;

/**
 * this class will represent the skin selected
 * and will contain all data necessary to the render
 * make the scene
 * 
 * @author fox
 */
public class Skin
{
          
    public enum entityID{
	NOTE_1,NOTE_2,NOTE_3,NOTE_4,NOTE_5,NOTE_6,NOTE_7,NOTE_SC,
	MEASURE_MARK,JUDGMENT_LINE,
	EFFECT_JUDGMENT_PERFECT,EFFECT_JUDGMENT_COOL,EFFECT_JUDGMENT_GOOD,
	EFFECT_JUDGMENT_BAD,EFFECT_JUDGMENT_MISS,
	EFFECT_LONGFLARE,EFFECT_CLICK,
	PRESSED_NOTE_1_LANE,PRESSED_NOTE_2_LANE,PRESSED_NOTE_3_LANE,
	PRESSED_NOTE_4_LANE,PRESSED_NOTE_5_LANE,PRESSED_NOTE_6_LANE,
	PRESSED_NOTE_7_LANE,PRESSED_NOTE_SC_LANE,
	PRESSED_NOTE_1_KEY,PRESSED_NOTE_2_KEY,PRESSED_NOTE_3_KEY,
	PRESSED_NOTE_4_KEY,PRESSED_NOTE_5_KEY,PRESSED_NOTE_6_KEY,
	PRESSED_NOTE_7_KEY,PRESSED_NOTE_SC_KEY,
	COUNTER_FPS,
	COUNTER_JUDGMENT_PERFECT,COUNTER_JUDGMENT_COOL,COUNTER_JUDGMENT_GOOD,
	COUNTER_JUDGMENT_BAD,COUNTER_JUDGMENT_MISS,
	COUNTER_MAXCOMBO,COUNTER_SCORE,
	COUNTER_JAM,COUNTER_COMBO,
	COUNTER_MINUTE,COUNTER_SECOND,
	COMBO_TITLE,JAM_TITLE,
	PILL_1,PILL_2,PILL_3,PILL_4,PILL_5,
	LIFE_BAR,JAM_BAR,TIME_BAR
    }
    
    private final HashMap<String,Actor> named_actors;
    int max_layer = 0;
    
    float screen_scale_x;
    float screen_scale_y;
    
    int keys = 0;

    public Skin()
    {
        named_actors = new HashMap<String,Actor>();
    }

    public HashMap<String,Actor> getEntityMap(){
        return named_actors;
    }

    public float getScreenScaleX()
    {
        return screen_scale_x;
    }

    public float getScreenScaleY()
    {
        return screen_scale_y;
    }
}
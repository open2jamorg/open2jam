package org.open2jam.render;

import java.util.ArrayList;
import java.util.HashMap;
import org.open2jam.render.entities.Entity;

/**
 * this class will represent the skin selected
 * and will contain all data necessary to the render
 * make the scene
 * 
 * @author fox
 */
public class Skin
{
    private final HashMap<String,Entity> named_entities;
    private final ArrayList<Entity> other_entities;

    int max_layer = 0;
    
    float screen_scale_x;
    float screen_scale_y;

    int judgment_line;


    public Skin()
    {
        named_entities = new HashMap<String,Entity>();
        other_entities = new ArrayList<Entity>();
    }

    public HashMap<String,Entity> getEntityMap(){
        return named_entities;
    }

    public ArrayList<Entity> getEntityList(){
        return other_entities;
    }

    public ArrayList<Entity> getAllEntities(){
        ArrayList<Entity> al = new ArrayList<Entity>();

        al.addAll(named_entities.values());
        al.addAll(other_entities);

        return al;
    }

    public float getScreenScaleX()
    {
        return screen_scale_x;
    }

    public float getScreenScaleY()
    {
        return screen_scale_y;
    }

    public int getJudgmentLine()
    {
        return judgment_line;
    }
}

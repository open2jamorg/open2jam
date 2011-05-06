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
    private final HashMap<String, HashMap<String,Entity>> named_entities;
    private final HashMap<String, ArrayList<Entity>> other_entities;
    private String selected_skin;

    int max_layer = 0;
    
    float screen_scale_x;
    float screen_scale_y;

    int judgment_line;


    public Skin()
    {
        named_entities = new HashMap<String, HashMap<String,Entity>>();
        other_entities = new HashMap<String, ArrayList<Entity>>();
    }

    public boolean setSkin(String skin)
    {
        this.selected_skin = skin;
        
        return (named_entities.containsKey(skin) && other_entities.containsKey(skin));
    }

    public void addSkin(String skin)
    {
        named_entities.put(skin, new HashMap<String,Entity>());
        other_entities.put(skin, new ArrayList<Entity>());
        this.selected_skin = skin;
    }

    public HashMap<String,Entity> getEntityMap(){
        return named_entities.get(selected_skin);
    }

    public ArrayList<Entity> getEntityList(){
        return other_entities.get((selected_skin));
    }

    public ArrayList<Entity> getAllEntities(){
        ArrayList<Entity> al = new ArrayList<Entity>();

        al.addAll(named_entities.get(selected_skin).values());
        al.addAll(other_entities.get(selected_skin));

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

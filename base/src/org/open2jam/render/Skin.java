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
    private HashMap<String,Entity> named_entities;
    private ArrayList<Entity> other_entities;

    protected int max_layer = 0;
    
    protected float screen_scale_x;
    protected float screen_scale_y;

    protected int judgment_line;

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
}

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

    protected Judgment judgment;

    public Skin()
    {
        named_entities = new HashMap<String,Entity>();
        other_entities = new ArrayList<Entity>();
    }

    public void addNamed(String id, Entity e)
    {
        named_entities.put(id, e);
    }

    public void add(Entity e)
    {
        other_entities.add(e);
    }

    public HashMap<String,Entity> getEntityMap(){
        return named_entities;
    }

    public ArrayList<Entity> getEntityList(){
        return other_entities;
    }

    protected class Judgment {
        int start;
        int size;

        /** this should return the
         *
         * @param p
         * @return
         */
        public String ratePrecision(double p)
        {
            return null;
        }
    }
}

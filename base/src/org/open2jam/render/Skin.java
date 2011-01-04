package org.open2jam.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
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
    protected int max_layer = 0;
    
    protected float screen_scale_x;
    protected float screen_scale_y;

    public Skin()
    {
        named_entities = new HashMap<String,Entity>();
        other_entities = new ArrayList<Entity>();
        judgment = new Judgment();
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
        double combo_threshold;
        
        NavigableMap<Double,String> score_map = new TreeMap<Double,String>().descendingMap();

        public String ratePrecision(double p)
        {
            for(Map.Entry<Double,String> e : score_map.entrySet())
            {
                if(p >= e.getKey())return e.getValue();
            }
            return null;
        }

        public String[] getRates(){
            return score_map.values().toArray(new String[0]);
        }
    }
}

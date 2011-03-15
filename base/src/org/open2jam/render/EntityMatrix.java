package org.open2jam.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import org.open2jam.render.entities.CompositeEntity;
import org.open2jam.render.entities.Entity;

/**
 * wrapper around the matrix of entities
 * used by the render to manage entities across layers
 * 
 * @author fox
 */
public class EntityMatrix
{
    private final ArrayList<LinkedList<Entity>> matrix;
    
    public EntityMatrix(int layers)
    {
        matrix = new ArrayList<LinkedList<Entity>>(layers);
        for(int i=0;i<layers;i++)matrix.add(new LinkedList<Entity>());
    }

    /**
     * add the entity to the matrix
     * special case of the CompositeEntity,
     * is this case, we will add each sub-entity separated */
    public void add(Entity e)
    {
        if(e instanceof CompositeEntity){
            for(Entity i : ((CompositeEntity) e).getEntityList())
                this.add(i);
        }
        else
        matrix.get(e.getLayer()).add(e);
    }

    public boolean isEmpty(int layer)
    {
        return matrix.get(layer).isEmpty();
    }

    public Iterator<LinkedList<Entity>> iterator()
    {
        return matrix.iterator();
    }
}

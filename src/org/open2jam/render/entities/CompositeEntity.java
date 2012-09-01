package org.open2jam.render.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author fox
 */
public class CompositeEntity extends Entity
{
    private final LinkedList<Entity> entity_list;

    public CompositeEntity(Collection<Entity> list)
    {
        entity_list = new LinkedList<Entity>();
        entity_list.addAll(list);
    }

    public CompositeEntity(Entity ... e)
    {
        entity_list = new LinkedList<Entity>();
        Collections.addAll(entity_list, e);
    }

    private CompositeEntity(CompositeEntity org)
    {
        super(org);
        entity_list = new LinkedList<Entity>();
        for(Entity e : org.entity_list)entity_list.add(e.copy());
    }

    public LinkedList<Entity> getEntityList()
    {
        return entity_list;
    }
    
    @Override
    public void move(double delta)
    {
        super.move(delta);
        for(Entity e : entity_list)e.move(delta);
    }

    @Override
    public void setLayer(int layer)
    {
        super.setLayer(layer);
        for(Entity e : entity_list)e.setLayer(layer);
    }

    @Override
    public void setXMove(double dx)
    {
        throw new UnsupportedOperationException("CompositeEntity does not support this");
    }
    
    @Override
    public void setYMove(double dy)
    {
        throw new UnsupportedOperationException("CompositeEntity does not support this");
    }

    @Override
    public void draw()
    {
        for(Entity e : entity_list)e.draw();
    }

    /**
     * compositeEntity is alive as long
     * as there is a entity alive inside it
     * @return
     */
    @Override
    public boolean isDead(){
        for (Entity e : entity_list) {
            if (e.isDead()) return false;
        }
        return true;

    }


    @Override
    public void setDead(boolean state){
        for(Entity e : entity_list)e.setDead(state);
    }

    @Override
    public CompositeEntity copy()
    {
        return new CompositeEntity(this);
    }
}

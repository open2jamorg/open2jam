package org.open2jam.render.entities;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author fox
 */
public class NumberEntity extends Entity
{
    protected int number = 0;
    protected ArrayList<Entity> entity_list;
    
    public NumberEntity(Collection<Entity> list, double x, double y)
    {
        entity_list = new ArrayList<Entity>();
        entity_list.addAll(list);
        this.x = x;
        this.y = y;
    }

    private NumberEntity(NumberEntity org) {
        super(org);
        entity_list = new ArrayList<Entity>();
        for(Entity e : org.entity_list)entity_list.add(e.copy());
        this.number = org.number;
    }

    public void setNumber(Integer i){
        this.number = i;
    }

    public int getNumber(){
        return number;
    }

    public void incNumber()
    {
        number++;
    }

    @Override
    public void draw()
    {
        char[] chars = String.valueOf(number).toCharArray();
        double tx = x;
        for(char c : chars)
        {
            int i = Integer.parseInt(c+"");
            entity_list.get(i).setPos(tx,y);
            entity_list.get(i).draw();
            tx += entity_list.get(i).getWidth();
        }
    }

    @Override
    public NumberEntity copy()
    {
        return new NumberEntity(this);
    }
}

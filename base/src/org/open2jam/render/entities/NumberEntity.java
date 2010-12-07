package org.open2jam.render.entities;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author fox
 */
public class NumberEntity extends Entity
{
    protected Integer number = 0;
    protected LinkedList<Entity> entity_list;
    
    public NumberEntity(Collection<Entity> list, double x, double y)
    {
        entity_list = new LinkedList<Entity>();
        entity_list.addAll(list);
        this.x = x;
        this.y = y;
    }

    public void setNumber(Integer i){
        this.number = i;
    }

    public int getNumber(){
        return number;
    }

    @Override
    public void draw()
    {
        char[] chars = number.toString().toCharArray();
        double tx = x;
        for(char c : chars)
        {
            int i = Integer.parseInt(c+"");
            entity_list.get(i).setX(tx);
            entity_list.get(i).setY(y);
            entity_list.get(i).draw();
            tx += entity_list.get(i).getWidth();
        }
    }
}

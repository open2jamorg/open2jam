package org.open2jam.render.entities;

import java.util.Collection;

/**
 *
 * @author fox
 */
public class NumberEntity extends CompositeEntity
{
    protected Integer number = null;
    
    public NumberEntity(Collection<Entity> list, double x, double y)
    {
        super(list);
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

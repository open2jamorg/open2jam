package org.open2jam.render.entities;

import java.util.Collection;

/**
 *
 * @author fox
 */
public class NumberEntity extends CompositeEntity
{
    private Integer number = 0;
    
    public NumberEntity(Collection<Entity> list, double x, double y)
    {
        super(list);
        this.x = x;
        this.y = y;
    }

    public void setNumber(int i){
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

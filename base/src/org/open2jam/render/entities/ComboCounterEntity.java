package org.open2jam.render.entities;

import java.util.Collection;

/**
 *
 * @author fox
 */
public class ComboCounterEntity extends NumberEntity
{
    protected double base_y;
    protected double base_x;

    public ComboCounterEntity(Collection<Entity> list, double x, double y)
    {
        super(list,x,y);
        base_y = y;
        base_x = x;
    }


    public void incNumber()
    {
        number++;
        y = base_y - 100;
    }

    public void resetNumber()
    {
        number = 0;
        y = base_y - 100;
    }

    @Override
    public void move(long delta)
    {
        super.move(delta);
        if(y < base_y)y++;
    }

    private void findMiddle()
    {
        char[] chars = number.toString().toCharArray();
        x = 0;
        for(char c : chars){
             int i = Integer.parseInt(c+"");
             x += entity_list.get(i).getWidth();
        }
        x = base_x - (x/2);
    }

    @Override
    public void draw()
    {
        if(number < 1)return;
        findMiddle();
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

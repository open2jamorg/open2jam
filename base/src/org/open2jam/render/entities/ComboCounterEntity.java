package org.open2jam.render.entities;

import java.util.Collection;

/**
 *
 * @author fox
 */
public class ComboCounterEntity extends NumberEntity
{
    /** how much the entity will "wobble" down
     * when the count number is increased, in pixels */
    protected static final int wobble = 50;

    /** the speed in which the entity will go
     * back to the base position */
    protected static final double wobble_dy = -1;

    /** the time in milliseconds in which the entity
     * will be displayed when the count is updated */
    protected static final int show_time = 5000;

    /** the base position of the entity */
    protected double base_y, base_x;

    /** time left to display on screen,
     * won't be draw on screen if it's zero */
    protected int to_show = 0;

    public ComboCounterEntity(Collection<Entity> list, double x, double y)
    {
        super(list,x,y);
        base_y = y;
        base_x = x;
    }

    @Override
    public void setX(double x)
    {
        base_x = x;
    }

    @Override
    public void setY(double y)
    {
        base_y = y;
    }

    public void incNumber()
    {
        super.incNumber();
        y = base_y + wobble;
        to_show = show_time;
    }

    public void resetNumber()
    {
        number = 0;
        y = base_y + wobble;
        to_show = 0;
    }

    @Override
    public void move(long delta)
    {
        super.move(delta);
        to_show -= delta;
        if(y > base_y)y += delta * wobble_dy;
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
        if(to_show <= 0)return;
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

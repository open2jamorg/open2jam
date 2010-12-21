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
    protected static final int show_time = 4000;

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
    public void setPos(double x, double y)
    {
        base_x = x;
        base_y = y;
    }

    @Override
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

    @Override
    public void draw()
    {
        if(to_show < 0)return;
	/* In O2Jam, a combo is simply the number of consecutive cools or goods hit by the player
	 * For example, for the first note, there is no combo, for the second, there is 1 combo, and so on.
	 * http://o2jam.wikia.com/wiki/Combo
	 *
	 * Maybe this is not the best place to do this ·-·
	 */
	if(number > 1)
	{
	    char[] chars = String.valueOf(number-1).toCharArray();
	    x = 0;
	    for(char c : chars){
		 int i = Integer.parseInt(c+"");
		 x += entity_list.get(i).getWidth();
	    }
	    x = base_x - (x/2);
	    double tx = x;
	    for(char c : chars)
	    {
		int i = Integer.parseInt(c+"");
		entity_list.get(i).setPos(tx, y);
		entity_list.get(i).draw();
		tx += entity_list.get(i).getWidth();
	    }
	}
    }
}

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
    private static final int wobble = 10;

    /** the speed in which the entity will go
     * back to the base position */
    private static final double wobble_dy = -0.5;

    /** the time in milliseconds in which the entity
     * will be displayed when the count is updated */
    private static final int show_time = 4000;

    /** the base position of the entity */
    private double base_y;
    private double base_x;

    /** time left to display on screen,
     * won't be draw on screen if it's zero */
    private int to_show = 0;

    private int count_threshold = 0;

    private Entity title_sprite = null;

    public ComboCounterEntity(Collection<Entity> list, Entity title, double x, double y)
    {
        super(list,x,y);
        base_y = y;
        base_x = x;
        title_sprite = title;
    }

    @Override
    public void setPos(double x, double y)
    {
        base_x = x;
        base_y = y;
        if(title_sprite == null)return;
        title_sprite.setPos(x, y);
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

    public void setThreshold(int i)
    {
        count_threshold = i;
    }

    @Override
    public void move(double delta)
    {
        super.move(delta);
        if(title_sprite != null) title_sprite.move(delta);
        to_show -= delta;
        if(y > base_y)y += delta * wobble_dy;
    }

    @Override
    public void draw()
    {
        if(to_show < 0 || number < count_threshold)return;
	/* In O2Jam, a combo is simply the number of consecutive cools or goods hit by the player
	 * For example, for the first note, there is no combo, for the second, there is 1 combo, and so on.
	 * http://o2jam.wikia.com/wiki/Combo
	 *
	 * Maybe this is not the best place to do this ·-·
	 */
        char[] chars = String.valueOf(number-(count_threshold > 0 ? count_threshold-1 : 0)).toCharArray();
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
        if(title_sprite == null) return;
        double title_x = base_x-(title_sprite.getWidth()/2);
        double title_y = base_y-(entity_list.get(0).getHeight());
        title_sprite.setPos(title_x, title_y);
        title_sprite.draw();
    }
}

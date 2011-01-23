
package org.open2jam.render.entities;

import java.util.Collection;

/**
 *
 * @author fox
 */
public class BarEntity extends NumberEntity
{
    private int LIMIT = 1;

    public enum Format {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        UP_TO_DOWN,
        DOWN_TO_UP
    };

    private Format format = Format.LEFT_TO_RIGHT;

    public BarEntity(Collection<Entity> list, double x, double y)
    {
        super(list,x,y);
    }

    public void setLimit(int limit){ LIMIT = limit; }
    public int getLimit() {return LIMIT; }

    public void setFormat(Format value) { format = value; }
    public Format getFormat() { return format; }

    @Override
    public void draw()
    {
        float perc = ((float)number / LIMIT);
        double px = x;
        double py = y;
         // TODO: ideally, this should be called only when the number changes
        switch(format)
        {
            case LEFT_TO_RIGHT:
            sprite.setSlice(perc, 1);
            break;
            case UP_TO_DOWN:
            sprite.setSlice(1, perc);
            break;
            case RIGHT_TO_LEFT:
            px += sprite.getWidth();
            sprite.setSlice(-perc, 1);
            break;
            case DOWN_TO_UP:
            py += sprite.getHeight();
            sprite.setSlice(1, -perc);
            break;
        }
        sprite.draw(px, py);
    }
}

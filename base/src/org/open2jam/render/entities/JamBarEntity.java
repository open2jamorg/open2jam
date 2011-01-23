
package org.open2jam.render.entities;

import java.util.Collection;

/**
 *
 * @author fox
 */
public class JamBarEntity extends NumberEntity
{
    public static final int JAM_LIMIT = 50;

    public JamBarEntity(Collection<Entity> list, double x, double y)
    {
        super(list,x,y);
    }

    @Override
    public void draw()
    {
        float x_perc = ((float)number / JAM_LIMIT);
         // TODO: ideally, this should be called only when the number changes
        sprite.setSlice(x_perc, 1);
        sprite.draw(x, y);
    }
}

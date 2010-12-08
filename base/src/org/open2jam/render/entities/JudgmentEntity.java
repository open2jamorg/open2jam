package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;

/**
 *
 * @author fox
 */
public class JudgmentEntity extends Entity
{
    /** the time in milliseconds in which the entity
     * will be displayed when the count is updated */
    protected int show_time = 3000;
    
    public JudgmentEntity(SpriteList refs, double x, double y)
    {
        super(refs, x, y);
        x -= sprite.getWidth()/2;
    }

    protected JudgmentEntity(JudgmentEntity org)
    {
        super(org);
        this.show_time = org.show_time;
    }

    @Override
    public void move(long delta)
    {
        super.move(delta);
        show_time -= delta;
        if(show_time < 0)alive = false;
    }

    @Override
    public void draw()
    {
        if(show_time > 2900)
            sprite.draw(x+(sprite.getWidth()*0.1)/2, y, 0.9f* sprite.getScaleX(), 0.9f*sprite.getScaleY());
        else
        super.draw();
    }

    @Override
    public JudgmentEntity copy()
    {
        return new JudgmentEntity(this);
    }
}

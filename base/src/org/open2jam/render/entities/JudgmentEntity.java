package org.open2jam.render.entities;

import org.open2jam.render.SpriteList;

/**
 *
 * @author fox
 */
public class JudgmentEntity extends AnimatedEntity
{
    /** the time in milliseconds in which the entity
     * will be displayed when the count is updated */
    private int show_time = 3000;
    
    public JudgmentEntity(SpriteList refs, double x, double y)
    {
        super(refs, x, y);
        this.x -= sprite.getWidth()/2;
    }

    private JudgmentEntity(JudgmentEntity org)
    {
        super(org);
        this.show_time = org.show_time;
    }

    @Override
    public void move(double delta)
    {
        super.move(delta);
        show_time -= delta;
        if(show_time < 0)dead = true;
    }

    @Override
    public void draw()
    {
        double tx = x, ty = y;
        float sx = sprite.getScaleX(), sy = sprite.getScaleY();

        if(show_time > 2900){
            double p = 0.5 + (3000-show_time)/200f;
            tx += (1-p)*(sprite.getWidth()/2);
            ty += (1-p)*(sprite.getHeight()/2);
            sx *= p;
            sy *= p;
        }
        sprite.draw(tx, ty, sx, sy);
    }

    @Override
    public JudgmentEntity copy()
    {
        return new JudgmentEntity(this);
    }
}

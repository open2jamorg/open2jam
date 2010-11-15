package org.open2jam.render.entities;

import org.open2jam.parser.Event;
import org.open2jam.render.Sprite;

/**
 *
 * @author fox
 */
public class LaneEntity extends Entity
{
    protected double y2;
    
    public LaneEntity(Sprite s, Event.Channel ch, double x, double y1, double y2)
    {
        super(s,ch, x,y1);
        this.y2 = y2;
    }

    protected LaneEntity(LaneEntity org) {
        super(org);
        this.y2 = org.y2;
    }

    @Override
    public void draw(){
        double p = y;
        while(p < y2){
            sprite.draw(x, p);
            p += sprite.getHeight();
        }
    }

    public LaneEntity copy(){
        return new LaneEntity(this);
    }
}

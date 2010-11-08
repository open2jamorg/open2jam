/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.open2jam.render.entities;

import java.awt.geom.Rectangle2D;
import org.open2jam.render.Render;

/**
 *
 * @author fox
 */
public class SampleEntity extends Entity
{
    private int value;
    private Render render;

    public SampleEntity(Render r, int value, double y)
    {
        this.render = r;
        this.value = value;
        bounds = new Rectangle2D.Double(0,y,0,0);
    }

    public void move(long delta)
    {
            setYMove(render.getNoteSpeed());
            super.move(delta);
    }

    public void judgment()
    {
         render.queueSample(value);
         alive = false;
    }

    
    public void draw() {}
}

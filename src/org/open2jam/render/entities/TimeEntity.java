
package org.open2jam.render.entities;

/**
 *
 * @author fox
 */
public interface TimeEntity {

    public void setTime(double t);

    public double getTime();

    /**
     * judgment time.
     * this will be called once, when it hits judgment.
     */
    public void judgment();
}

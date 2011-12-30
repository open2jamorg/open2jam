/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.entities;

/**
 *
 * @author CdK
 */
public interface TimeEntity {

    public void setTime(long t);

    public long getTime();

    /**
     * judgment time.
     * this will be called once, when it hits judgment.
     */
    public void judgment();   
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.gameplays;

import org.open2jam.entities.Entity;

/**
 *
 * @author CdK
 */
public interface Gameplay {
    
    boolean init();
    /*
     * Update the gameplay logic
     * return: if it finished 
     */
    boolean update(long now);
    
    
    void checkControls(long now);
    
    void doAutoplay(long now);
    
    void checkJudgment(Entity e, long now);
    
    boolean updateEventBuffer(long now);
    
}


package org.open2jam.game.position;

import org.open2jam.render.entities.NoteEntity;

/**
 * The NoteDistanceCalculator interface calculates the note position, or more
 * accurately, the distance between the the note and the target.
 * It can affect individual notes.
 * 
 * @author Thai Pangsakulyanont
 */
public interface NoteDistanceCalculator {
    
    /**
     * Updates this NoteDistanceCalculator. This method should be invoked every
     * frame.
     * 
     * @param now the current game time
     * @param delta the time difference between last render and now
     */
    void update(double now, double delta);
    
    /**
     * Calculates the distance between hit target and the note, in pixel.
     * 
     * @param now the current game time
     * @param target the target time to calculate
     * @param speed the speed multiplier
     * @param noteEntity the related note entity, may be null.
     * @return the distance, in pixels, between the hit tatget and the note
     */
    double calculate(double now, double target, double speed, NoteEntity noteEntity);
    
}

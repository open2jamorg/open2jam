
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
    
    void update(double now, double delta);
    
    double calculate(double now, double target, double speed, NoteEntity noteEntity);
    
}

package org.open2jam.game.judgment;

import org.open2jam.render.entities.NoteEntity;
import org.open2jam.game.TimingData;

/**
 * A hit judgment strategy.
 * @author dttvb
 */
public interface JudgmentStrategy {
    
    /**
     * Returns true if the note should be accepted (judged) by the game.
     * 
     * @param note the note to check
     * @return true if the note is to be judged
     */
    boolean accept(NoteEntity note);
    
    /**
     * Returns true if the player missed the note.
     * 
     * @param note the note to check
     * @return true if the note is missed, false otherwise
     */
    boolean missed(NoteEntity note);
    
    /**
     * Sets the timing data associated with this judge.
     * 
     * @param timing timing data to set
     */
    void setTiming(TimingData timing);

    /**
     * Judge the note.
     * 
     * @param note the note to judge
     * @return the result of the judgment
     */
    JudgmentResult judge(NoteEntity note);
    
}

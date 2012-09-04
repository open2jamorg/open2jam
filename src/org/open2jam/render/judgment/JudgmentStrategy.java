package org.open2jam.render.judgment;

import org.open2jam.render.entities.NoteEntity;

/**
 * A hit judgment strategy.
 * @author dttvb
 */
public interface JudgmentStrategy {
    
    boolean accept(NoteEntity note);
    boolean missed(NoteEntity note);

    JudgmentResult judge(NoteEntity note);
    
}

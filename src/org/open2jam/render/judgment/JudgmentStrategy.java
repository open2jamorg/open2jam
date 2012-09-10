package org.open2jam.render.judgment;

import org.open2jam.render.entities.NoteEntity;
import org.open2jam.util.TimingData;

/**
 * A hit judgment strategy.
 * @author dttvb
 */
public interface JudgmentStrategy {
    
    boolean accept(NoteEntity note);
    boolean missed(NoteEntity note);
    
    void setTiming(TimingData timing);

    JudgmentResult judge(NoteEntity note);
    
}

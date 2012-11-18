
package org.open2jam.game.judgment;

import org.open2jam.render.entities.NoteEntity;
import org.open2jam.game.TimingData;

/**
 * Judge hits by distance.
 * @author dtinth
 */
public class BeatJudgment extends AbstractJudgmentStrategy {

    private static final double BAD_THRESHOULD = 0.8;
    private static final double GOOD_THRESHOULD = 0.5;
    private static final double COOL_THRESHOULD = 0.2;
    
    private double calculateHit(NoteEntity note) {
        double noteTime = note.getTimeToJudge();
        double hitTime = note.getTimeToJudge() - note.getHitTime();
        double noteBeat = timing.getBeat(noteTime);
        double hitBeat = timing.getBeat(hitTime);
        return (noteBeat - hitBeat) / 0.664;
    }
    
    @Override
    public boolean accept(NoteEntity note) {
        return calculateHit(note) <= BAD_THRESHOULD;
    }

    @Override
    public boolean missed(NoteEntity note) {
        return calculateHit(note) < -BAD_THRESHOULD;
    }

    @Override
    public JudgmentResult judge(NoteEntity note) {
        double hit = Math.abs(calculateHit(note));
        if (hit <= COOL_THRESHOULD) return JudgmentResult.COOL;
        if (hit <= GOOD_THRESHOULD) return JudgmentResult.GOOD;
        if (hit <= BAD_THRESHOULD) return JudgmentResult.BAD;
        return JudgmentResult.MISS;
    }
    
}

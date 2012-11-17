package org.open2jam.game.position;

import org.open2jam.game.TimingData;
import org.open2jam.game.speed.Speed;
import org.open2jam.render.entities.NoteEntity;

/**
 *
 * @author Thai Pangsakulyanont
 */
public class HiSpeed implements NoteDistanceCalculator {
    private final TimingData timing;
    private final double measureSize;

    public HiSpeed(TimingData timingData, double measureSize) {
        this.timing = timingData;
        this.measureSize = measureSize;
    }
    
    @Override
    public double calculate(double now, double target, double speed, NoteEntity noteEntity) {
        return speed * (timing.getBeat(target) - timing.getBeat(now)) * measureSize / 4;
    }

    @Override
    public void update(double now, double delta) {
    }

    @Override
    public String toString() {
        return "HI-SPEED";
    }
    
}

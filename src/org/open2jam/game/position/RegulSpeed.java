package org.open2jam.game.position;

import org.open2jam.render.entities.NoteEntity;

/**
 *
 * @author Thai Pangsakulyanont
 */
public class RegulSpeed implements NoteDistanceCalculator {
    
    private final double measureSize;

    public RegulSpeed(double measureSize) {
        this.measureSize = measureSize;
    }
    
    @Override
    public double calculate(double now, double target, double speed, NoteEntity noteEntity) {
        double delta = target - now;
        double beats = delta * 150 / 60000;
        return speed * beats * measureSize / 4;
    }

    @Override
    public void update(double now, double delta) {
    }
    
    @Override
    public String toString() {
        return "REGUL-SPEED";
    }
    
}

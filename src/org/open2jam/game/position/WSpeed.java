package org.open2jam.game.position;

import org.open2jam.game.position.NoteDistanceCalculator;
import org.open2jam.game.speed.Speed;
import org.open2jam.game.speed.Speed;
import org.open2jam.render.entities.NoteEntity;

/**
 *
 * @author Thai Pangsakulyanont
 */
public class WSpeed implements NoteDistanceCalculator {
    private final NoteDistanceCalculator base;
    
    private Speed speedObj;
    
    private double speed = 0.5;
    private double time = 0;
    private boolean positive = true;
    
    private static final double W_SPEED_FACTOR = 0.0005d;
    
    public WSpeed(NoteDistanceCalculator base, Speed speed) {
        this.base = base;
        this.speedObj = speed;
    }

    @Override
    public void update(double now, double delta) {
        base.update(now, delta);
        time += delta;
        if(time < 3000 * speedObj.getSpeed())
        {
            speed += (positive ? 1 : -1) * W_SPEED_FACTOR * delta;                
            if (speed > 10) speed = 10;
            if (speed < 0.5) speed = 0.5;
        }
        else
        {
            time = 0;
            positive = !positive;
        }

    }

    @Override
    public String toString() {
        return "W-SPEED";
    }

    @Override
    public double calculate(double now, double target, double speed, NoteEntity noteEntity) {
        return base.calculate(now, target, 1, noteEntity) * this.speed;
    }
    
}

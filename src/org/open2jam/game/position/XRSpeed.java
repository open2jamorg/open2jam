package org.open2jam.game.position;

import java.util.Random;
import org.open2jam.parsers.Event;
import org.open2jam.render.entities.NoteEntity;

/**
 *
 * @author Thai Pangsakulyanont
 */
public class XRSpeed implements NoteDistanceCalculator {
    private final NoteDistanceCalculator base;
    private double[] values = new double[7];
    
    public XRSpeed(NoteDistanceCalculator base) {
        this.base = base;
        
        Random rnd = new Random();
        for(int i = 0; i < 7; i++) {
            values[i] = rnd.nextDouble();
        }

    }
    
    @Override
    public void update(double now, double delta) {
        base.update(now, delta);
    }

    @Override
    public double calculate(double now, double target, double speed, NoteEntity noteEntity) {
        
        double factor = 1;

        Event.Channel chan = noteEntity == null ? null : noteEntity.getChannel();
        
        if (chan != null) switch (chan) {
            case NOTE_1: factor += values[0]; break;
            case NOTE_2: factor += values[1]; break;
            case NOTE_3: factor += values[2]; break;
            case NOTE_4: factor += values[3]; break;
            case NOTE_5: factor += values[4]; break;
            case NOTE_6: factor += values[5]; break;
            case NOTE_7: factor += values[6]; break;
        }

        return base.calculate(now, target, speed, noteEntity) * factor;
        
    }
    
    @Override
    public String toString() {
        return "xR-SPEED";
    }
    
}

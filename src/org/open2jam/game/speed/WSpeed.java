package org.open2jam.game.speed;

import org.open2jam.game.speed.Speed;

/**
 *
 * @author Thai Pangsakulyanont
 */
public class WSpeed implements Speed {
    
    private Speed base;
    
    private double speed = 0;
    private double time = 0;
    private boolean positive = true;
    
    private static final double W_SPEED_FACTOR = 0.0005d;
    
    public WSpeed(Speed base) {
        this.base = base;
    }

    @Override
    public void decrease() {
        base.decrease();
    }

    @Override
    public double getCurrentSpeed() {
        return speed;
    }

    @Override
    public double getSpeed() {
        return base.getSpeed();
    }

    @Override
    public void increase() {
        base.increase();
    }

    @Override
    public void setSpeed(double speed) {
        base.setSpeed(speed);
    }

    @Override
    public void update(double delta) {
        base.update(delta);
        time += delta;
        if(time < 3000 * base.getSpeed())
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
    
}

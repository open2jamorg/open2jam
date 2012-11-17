package org.open2jam.game.speed;

import org.open2jam.game.speed.Speed;

/**
 *
 * @author Thai Pangsakulyanont
 */
public class SpeedMultiplier implements Speed {
    
    private double speed = 1;
    private double currentSpeed = 1;
    
    private static final double MAX_SPEED = 10;
    private static final double MIN_SPEED = 0.5;
    
    private static final double SPEED_STEP = 0.5d;
    private static final double SPEED_FACTOR = 0.005d;

    public SpeedMultiplier(double speed) {
        this.speed = currentSpeed = speed;
    }
    
    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public double getCurrentSpeed() {
        return currentSpeed;
    }
    
    @Override
    public void update(double delta) {
        
        if (currentSpeed < speed) {
            currentSpeed += SPEED_FACTOR * delta;
            if (currentSpeed > speed) currentSpeed = speed;
        }
        else if (currentSpeed > speed) {
            currentSpeed -= SPEED_FACTOR * delta;
            if (currentSpeed < speed) currentSpeed = speed;
        }

    }
    
    @Override
    public void increase() {
        setSpeed(speed + SPEED_STEP > MAX_SPEED ? MAX_SPEED : speed + SPEED_STEP);
    }
    
    @Override
    public void decrease() {
        setSpeed(speed - SPEED_STEP < MIN_SPEED ? MIN_SPEED : speed - SPEED_STEP);
    }

    @Override
    public String toString() {
        return "x" + speed;
    }
    
}

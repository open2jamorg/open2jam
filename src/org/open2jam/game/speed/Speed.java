package org.open2jam.game.speed;

/**
 * The Speed interface handles calculation and manipulation of speed multiplier
 * The speed multiplier affects all notes in the game.
 * 
 * @author Thai Pangsakulyanont
 */
public interface Speed {

    void decrease();

    double getCurrentSpeed();

    double getSpeed();

    void increase();

    void setSpeed(double speed);

    void update(double delta);
    
}

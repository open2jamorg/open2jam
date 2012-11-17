package org.open2jam.game.speed;

/**
 * The Speed interface handles calculation and manipulation of speed multiplier
 * The speed multiplier affects all notes in the game.
 * 
 * @author Thai Pangsakulyanont
 */
public interface Speed {

    /**
     * Decreases the speed multiplier.
     */
    void decrease();

    /**
     * Returns the current speed multiplier to be rendered.
     * @return the current speed multiplier
     */
    double getCurrentSpeed();

    /**
     * Returns the "target" speed multiplier.
     * @return the target speed multiplier
     */
    double getSpeed();

    /**
     * Increases the speed multiplier.
     */
    void increase();

    /**
     * Sets the target speed multiplier.
     * @param speed the target speed multiplier
     */
    void setSpeed(double speed);

    /**
     * Updates this speed multiplier. This method should be invoked every frame.
     * @param delta time difference from last frame
     */
    void update(double delta);
    
}


package org.open2jam.game.judgment;

/**
 *
 * @author dtinth
 */
public enum JudgmentResult {
    PERFECT, COOL, GOOD, BAD, MISS;
    @Override
    public String toString() {
        return "JUDGMENT_" + super.toString();
    }
}

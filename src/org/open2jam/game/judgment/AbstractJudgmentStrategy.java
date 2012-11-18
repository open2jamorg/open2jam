/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.game.judgment;

import org.open2jam.game.TimingData;

/**
 *
 * @author Thai Pangsakulyanont
 */
public abstract class AbstractJudgmentStrategy implements JudgmentStrategy {

    protected TimingData timing;

    public void setTiming(TimingData timing) {
        this.timing = timing;
    }
    
}

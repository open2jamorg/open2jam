
package org.open2jam.game;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class stores the timing data, replaces the velocity tree.
 * It is also able to calculate elapsed beat from the elapsed time.
 * @author Thai Pangsakulyanont
 */
public class TimingData {
    
    public static class VelocityChange {
        
        private double time;
        private double bpm;
        private double beat;

        public VelocityChange(double time, double bpm) {
            this.time = time;
            this.bpm = bpm;
        }

        public double getBeat() {
            return beat;
        }

        public void setBeat(double beat) {
            this.beat = beat;
        }

        public double getBpm() {
            return bpm;
        }

        public double getTime() {
            return time;
        }
        
        public double calculateBeat(double target) {
            return beat + (target - time) * bpm / 60000;
        }

    }
    
    List<VelocityChange> buffer = new LinkedList<VelocityChange>();
    VelocityChange[] changes;
    
    public void add(double time, double bpm) {
        buffer.add(new VelocityChange(time, bpm));
    }
    
    public void finish() {
        changes = buffer.toArray(new VelocityChange[buffer.size()]);
        changes[0].beat = 0;
        for (int i = 1; i < changes.length; i ++) {
            changes[i].setBeat(changes[i - 1].calculateBeat(changes[i].getTime()));
        }
    }
    
    public double getBeat(double time) {
        int min = 0, max = changes.length - 1;
        int left = min, right = max;
        while (left <= right) {
            int mid = (left + right) / 2;
            boolean afterMid = mid < 0 || changes[mid].getTime() <= time;
            boolean beforeNext = mid + 1 >= changes.length || time < changes[mid + 1].getTime();
            if (afterMid && beforeNext) {
                return changes[mid].calculateBeat(time);
            } else if (afterMid) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return changes[0].calculateBeat(time);
    }
    
}

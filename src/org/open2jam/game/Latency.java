/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.game;

import java.util.LinkedList;
import java.util.List;

/**
 * This class handles the latency stuff, facilitating auto-synchronization.
 * 
 * @author Thai Pangsakulyanont
 */
public class Latency {
    
    private double latency;
    private double starting;
    private List<Double> history = new LinkedList<Double>();
    
    public Latency(double latency) {
        this.latency = this.starting = latency;
    }

    public double getLatency() {
        return latency;
    }
    
    public void autosync(double hit) {
        
        history.add(latency - hit);
        
        double sum = 0;
        int count = 0;
        
        for (double lag : history) {
            sum += lag;
            count ++;
        }
        
        while (count < 64) {
            sum += starting;
            count ++;
        }
        
        latency = sum / count;
        
        System.out.println("Latency : " + latency);
        
    }
    
}

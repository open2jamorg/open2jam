/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.utils;

/**
 *
 * @author CdK
 */
public class SystemTimer {
    
    private static float frame_time;
    private static float beat_time;
    
    /** The number of nanoseconds per microsecond */
    private static final int nanoTicksPerMicro = 1000;// 10^3
    
    /** 4 beats per minute, 4 * 60 beats per second */
    public static final long BEATS_PER_SECOND = 4 * 60;
    
    /** 4 beats per minute, 4 * 60 beats per second, 4*60*1000 per millisecond */
    public static final long BEATS_PER_MILLI = BEATS_PER_SECOND * 1000;
    
    /** 4 beats per minute, 4 * 60 beats per second, 4*60*1000000 per microsecond */
    public static final long BEATS_PER_MICRO = BEATS_PER_MILLI * 1000;
	
    /**
     * Get the high resolution time in microseconds
     *
     * @return The high resolution time in microseconds
     */
    public static long getTime() {
        return System.nanoTime() / nanoTicksPerMicro;
    }
    
    public static void updateTimes(float delta)
    {
	frame_time += delta;
    }
    
    public static float getBeatTime(){
	return beat_time;
    }
    public static float getFrameTime(){
	return frame_time;
    }
	
    public static void sleep(int milli)
    {
        if(milli <= 0)return;
        try{
                Thread.sleep(milli);
        }catch(InterruptedException ignored){}
    }
}

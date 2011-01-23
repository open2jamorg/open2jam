package org.open2jam.util;

/**
 * A wrapper class that provides timing methods.
 * 
 * @author fox
 */
public class SystemTimer {
    /** The number of "timer ticks" per second */
    private static final long timerTicksPerMilli = 1000000L;// 10^6
    private static long ticks = 0;
    private static long start;

    /**
    * Starts the timer running. The number of ticks is reset to zero and the timer is synchronized with the
    * leading edge of the wave.
    */
    static {
        long time = System.nanoTime();
        long prev_time = time;

        ticks = 0;

        //Synchronize our timer
        while(time == prev_time)time = System.nanoTime();
        start = System.nanoTime();
    }
	
    /**
     * Get the high resolution time in milliseconds
     *
     * @return The high resolution time in milliseconds
     */
    public static long getTime() {
        // we get the "timer ticks" from the high resolution timer
        // then divide by the number of ticks in a millisecond giving
        // us a nice clear time in milliseconds
        return getClockTicks() / timerTicksPerMilli;
    }

    /**
    * Returns the number of clock ticks since the timer was started. If the timer is stopped,
    * the number of ticks will be frozen at the duration between when the clock was started and
    * stopped.
    *
    * @return Number of ticks since the clock started.
    */

    public static long getClockTicks()
    {
        ticks = (System.nanoTime()-start);
        return ticks;
    }
	
    public static void sleep(int milli)
    {
        if(milli <= 0)return;
        try{
                Thread.sleep(milli);
        }catch(InterruptedException e){}
    }
}

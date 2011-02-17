package org.open2jam.util;

/**
 * A wrapper class that provides timing methods.
 * 
 * @author fox
 */
public class SystemTimer {
    /** The number of nanoseconds per millisecond */
    private static final int nanoTicksPerMilli = 1000000;// 10^6
	
    /**
     * Get the high resolution time in milliseconds
     *
     * @return The high resolution time in milliseconds
     */
    public static long getTime() {
        return System.nanoTime() / nanoTicksPerMilli;
    }
	
    public static void sleep(int milli)
    {
        if(milli <= 0)return;
        try{
                Thread.sleep(milli);
        }catch(InterruptedException e){}
    }
}

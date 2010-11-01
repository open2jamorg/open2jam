package org.open2jam.render;

/**
 * A wrapper class that provides timing methods. This class
 * provides us with a central location where we can add
 * our current timing implementation. Initially, we're going to
 * rely on the GAGE timer. (@see http://java.dnsalias.com)
 * 
 * @author Kevin Glass
 */
public class SystemTimer {
	/** The number of "timer ticks" per second */
	private static long timerTicksPerSecond = 1000000000L;// 10^9
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
		// multiply by 1000 so our end result is in milliseconds
		// then divide by the number of ticks in a second giving
		// us a nice clear time in milliseconds
		return (getClockTicks() * 1000) / timerTicksPerSecond;
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
	
	/**
	* Stops code execution for the specified number of clock ticks. To prevent the next
	* tick from being lost, this is implemented as a hard loop that calls Thread.yield().
	* Unless the calling thread is of extremely low priority, this should return almost
	* immediately after the clock changes.
	*
	* @param ticks The number of ticks to wait before returning control
	*/
	
	public static void sleep(long ticks)
	{
		long tick = getClockTicks();
		while(getClockTicks() < tick+ticks) Thread.yield();
	}
}

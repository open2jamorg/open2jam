package org.open2jam.render;

import com.dnsalias.java.timer.AdvancedTimer;

/**
 * A wrapper class that provides timing methods. This class
 * provides us with a central location where we can add
 * our current timing implementation. Initially, we're going to
 * rely on the GAGE timer. (@see http://java.dnsalias.com)
 * 
 * @author Kevin Glass
 */
public class SystemTimer {
	/** Our link into the GAGE timer library */
	private static AdvancedTimer timer = new AdvancedTimer();
	/** The number of "timer ticks" per second */
	private static long timerTicksPerSecond;
	
	/** A little initialisation at startup, we're just going to get the GAGE timer going */
	static {
		timer.start();
		timerTicksPerSecond = AdvancedTimer.getTicksPerSecond();
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
		return (timer.getClockTicks() * 1000) / timerTicksPerSecond;
	}
	
	/**
	 * Sleep for a fixed number of milliseconds. 
	 * 
	 * @param duration The amount of time in milliseconds to sleep for
	 */
	public static void sleep(long duration) {
		timer.sleep((duration * timerTicksPerSecond) / 1000);
	}
}
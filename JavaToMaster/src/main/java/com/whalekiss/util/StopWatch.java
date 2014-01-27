package com.whalekiss.util;


/**
 * 计算时间 的
 * 调用方法为start，stop
 * Provides 'StopWatch' style timing features to measure
 * total elaspsed time as well as individual 'lap' splits,
 * minimum & maximum lap times as well average lap time.
 * 
 * Sample Code
 * 
 * 		// test granularity of timer
 *		
 *		int changes = 200;
 *		System.out.println("Testing Resolution of Timer over " + changes + " changes...");
 *		Granularity g = getGranularity(changes); 
 *			
 *		System.out.println("min nanos = " + g.m_min);
 *		System.out.println("max nanos = " + g.m_max);
 *		System.out.println("avg nanos = " + g.m_avg);
 *		
 *		// simple elapsed time
 *		System.out.println("Simple elapsed time .....");
 *
 *		StopWatch sw = new StopWatch();
 *		sw.start();
 *		Thread.sleep(5);
 *		long elapsedTimeNanos = sw.stop();
 *		
 *		double seconds = StopWatch.getSeconds(elapsedTimeNanos);
 *		double millis = StopWatch.getMillis(sw.getElapsedTime());
 *		
 *		System.out.println("Slept for " + seconds + " seconds");
 *		System.out.println("Slept for " + millis + " millis");
 *		
 *		long wholeSeconds = (long) seconds;
 *		long roundedSeconds = Math.round(seconds);
 *		long wholeMillis  = (long) millis;
 *		long roundedMillis  = Math.round(millis);
 *		
 *		System.out.println("Slept for " + wholeSeconds + " whole seconds");
 *		System.out.println("Slept for " + wholeMillis + " whole millis");
 *		System.out.println("Slept for " + roundedSeconds + " rounded seconds");
 *		System.out.println("Slept for " + roundedMillis + " rounded millis");
 *		
 *		System.out.println();
 *		System.out.println("Time a set (known) number of iterations");
 *		
 *		int iterations = 10;
 *		sw = new StopWatch();
 *		sw.start(iterations);
 *		for (int i = 0; i < iterations; i++)
 *		{
 *			Thread.sleep(5);
 *			sw.lapCompleted();
 *		}
 *		
 *		elapsedTimeNanos = sw.getElapsedTime();
 *		long minLapNanos = sw.getMinLap();
 *		long maxLapNanos = sw.getMaxLap();
 *		long avgLapNanos = sw.getAvgLap();
 *		
 *		System.out.println("Elapsed nanos = " + elapsedTimeNanos);
 *		System.out.println("Min Lap nanos = " + minLapNanos);
 *		System.out.println("Max Lap nanos = " + maxLapNanos);
 *		System.out.println("Avg Lap nanos = " + avgLapNanos);
 *		
 *		System.out.println();
 *		System.out.println("Elapsed millis = " + StopWatch.getMillis(elapsedTimeNanos));
 *		System.out.println("Min Lap millis = " + StopWatch.getMillis(minLapNanos));
 *		System.out.println("Max Lap millis = " + StopWatch.getMillis(maxLapNanos));
 *		System.out.println("Avg Lap namillisnos = " + StopWatch.getMillis(avgLapNanos));
 *	
 *		System.out.println();
 *		System.out.println("Time a number of iterations, not knowing the number in advance");
 *		
 *		sw = new StopWatch();
 *		sw.start(iterations);
 *		for (int i = 0; i < iterations - 1; i++)
 *		{
 *			Thread.sleep(5);
 *			sw.lapCompleted();
 *		}
 *		Thread.sleep(10);
 *		sw.stop();
 *		
 *		elapsedTimeNanos = sw.getElapsedTime();
 *		minLapNanos = sw.getMinLap();
 *		maxLapNanos = sw.getMaxLap();
 *		avgLapNanos = sw.getAvgLap();
 *		
 *		System.out.println("Elapsed nanos = " + elapsedTimeNanos);
 *		System.out.println("Min Lap nanos = " + minLapNanos);
 *		System.out.println("Max Lap nanos = " + maxLapNanos);
 *		System.out.println("Avg Lap nanos = " + avgLapNanos);
 *		
 *		System.out.println();
 *		System.out.println("Elapsed millis = " + StopWatch.getMillis(elapsedTimeNanos));
 *		System.out.println("Min Lap millis = " + StopWatch.getMillis(minLapNanos));
 *		System.out.println("Max Lap millis = " + StopWatch.getMillis(maxLapNanos));
 *		System.out.println("Avg Lap millis = " + StopWatch.getMillis(avgLapNanos));
 *
 * Some comments added by cstein.
 *
 * @author <a href="mailto:jzajac@expedia.com">John Zajac</a>
 * 
 */
public class StopWatch
{
	private long m_start = 0;
	private long m_end = 0;
	private boolean m_isStarted = false;
	
	private long m_minLap = Long.MAX_VALUE;
	private long m_maxLap = Long.MIN_VALUE;
	
	private long m_lapCounter = 0;
	private long m_lapsToTime = 0;
	private long m_lastLapStart = 0;

	/**
	 * Starts the watch, if already started, it will be re-started.
	 */
	public void start()
	{
		m_minLap = Long.MAX_VALUE;
		m_maxLap = Long.MIN_VALUE;
		m_lapCounter = 0;
		m_lapsToTime = 0;
		
		m_start = System.nanoTime();
		m_lastLapStart = m_start;
		m_isStarted = true;
	}
	
	/**
	 * Starts the watch, if already started, it will be re-started,
	 * the watch will auto stop after specifed laps completed.
	 * @param lapCount the number of laps after which to auto start
	 */
	public void start(int lapCount)
	{
		start();
		m_lapsToTime = lapCount; 
	}
	
	/**
	 * Stops the watch and records last lap.
	 * @return total duration from start to stop (all laps)
	 */
	public long stop()
	{
		if (m_isStarted)
		{
			m_end = System.nanoTime();
			recordLapCompleted(m_end);
			m_isStarted = false;
			return (m_end - m_start);
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * tells the watch that a 'lap' has been completed and
	 * that the next one has started.
	 * @return the duration of the completed lap
	 */
	public long lapCompleted()
	{
		return lapCompleted(false);
	}
	
	/**
	 * tells the watch that a 'lap' has been completed and either
	 * the next one has started or that this was final one, in which
	 * case the watch is stopped. 
	 * @param isFinalLap stops the watch if true
	 * @return the duration of the completed lap
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public long lapCompleted(boolean isFinalLap)
	{
		long lapDuration = 0;
		if (m_isStarted)
		{
			if (isFinalLap || (m_lapsToTime == (m_lapCounter + 1)))
			{
				long finalLapStart = m_lastLapStart;
				stop();
				lapDuration = m_end - finalLapStart;
			}
			else
			{
				lapDuration = recordLapCompleted(System.nanoTime());
			}
		}
		else
		{
			System.out.println("lap completed called while not started");
		}
	
		return lapDuration;
	}
	
	/**
	 * Determines if the stopwatch is started.
	 * 
	 * @return Returns true if the stopwatch has been started.
	 */
	public boolean isStarted()
	{
		return m_isStarted;
	}
	
	/**
	 * Gets the number of completed laps.
	 * 
	 * @return Returns the number of completed laps.
	 */
	public long getCompletedLaps()
	{
		return m_lapCounter;
	}
	
	/**
	 * Gets the elapsed time.
	 * 
	 * @return Returns the elapsed time in nanoseconds.
	 */
	public long getElapsedTime()
	{
		long end = m_end;
		if (m_isStarted)
		{
			end = System.nanoTime();
		}
		return (end - m_start);
	}
	
	/**
	 * Gets the min lap time.
	 * 
	 * @return Returns the min lap time.
	 */
	public long getMinLap()
	{
		return m_minLap;
	}
	
	/**
	 * Gets the max lap time.
	 * 
	 * @return Returns the max lap time.
	 */
	public long getMaxLap()
	{
		return m_maxLap;
	}
	
	/**
	 * Gets the avg lap time.
	 * 
	 * @return Returns the avg lap time.
	 */
	public long getAvgLap()
	{
		long rc = 0;
		if (m_lapCounter > 0)
		{
			rc =  (m_lastLapStart - m_start)/ m_lapCounter;
		}
		return rc;
	}
	
	/**
	 * Get seconds from nanoseconds.
	 * 
	 * @param nanos The time in nanoseconds.
	 * @return Returns the time in seconds.
	 */
	public static double getSeconds(long nanos)
	{
		double d = nanos;
		d = d/1000000000;
		return d;
	}
	
	/**
	 * Get rounded seconds from nanoseconds.
	 * 
	 * @param nanos The time in nanoseconds.
	 * @return Returns the time in rounded seconds.
	 */
	public static long getRoundedSeconds(long nanos)
	{
		return Math.round(getSeconds(nanos));
	}
	
	/**
	 * Get milliseconds from nanoseconds.
	 * 
	 * @param nanos The time in nanoseconds.
	 * @return Returns the time in milliseconds.
	 */
	public static double getMillis(long nanos)
	{
		double d = nanos;
		d = d/1000000;
		return d;
	}
	
	/**
	 * Get rounded milliseconds from nanoseconds.
	 * 
	 * @param nanos The time in nanoseconds.
	 * @return Returns the time in rounded milliseconds.
	 */
	public static long getRoundedMillis(long nanos)
	{
		return Math.round(getMillis(nanos));
	}
	
		
	/**
	 * Get the granularity.
	 * 
	 * @param changes The number of changes.
	 * @return Returns the granularity.
	 */
	public static Granularity getGranularity(int changes)
	{
   		long startTime = System.nanoTime();
		long endTime = startTime;
   		
		long minTime = Long.MAX_VALUE;
		long maxTime = Long.MIN_VALUE;
		long totalTime = 0;
		
		for (int i = 0; i < changes; i++)
		{
			while (startTime == endTime)
			{
				// wait for it to change
				endTime = System.nanoTime();
			}
			long curTime = endTime - startTime;
			totalTime += curTime;
			startTime = endTime;
			if (curTime > maxTime)
			{
				maxTime = curTime;
			}
			if (curTime < minTime)
			{
				minTime = curTime;
			}
		}
		long avgTime = totalTime / changes;
		
		return new Granularity(minTime, maxTime, avgTime);
	}
	
	/**
	 * Represents the timing Granularity.
	 * 
	 * @author <a href="mailto:jzajac@expedia.com">jzajac</a>
	 *
	 */
	public static class Granularity
	{
		/** The minimum time. */
		public long m_min;
		
		/** The maximum time. */
		public long m_max;
		
		/** The average time. */
		public long m_avg;

		/**
		 * Constructor.
		 * 
		 * @param min The minimum time.
		 * @param max The maximum time.
		 * @param avg The average time.
		 */
		protected Granularity(long min, long max, long avg)
		{
			m_min = min;
			m_max = max;
			m_avg = avg;
		}
	}
	
	private long recordLapCompleted(long lapEnd)
	{
		long lapDuration;

		m_lapCounter++;
		lapDuration = lapEnd - m_lastLapStart;
		m_lastLapStart = lapEnd;
		
		if (lapDuration > m_maxLap)
		{
			m_maxLap = lapDuration;
		}
		if (lapDuration < m_minLap)
		{
			m_minLap = lapDuration;
		}
		
		return lapDuration;
	}
	

}


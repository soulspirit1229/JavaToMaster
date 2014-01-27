package com.whalekiss.util.moitor;


/**
 * Class description:
 *	Does to IMonitorNumber what IMonitorCounterFamily does to IMonitorCounter
 *
 * Some comments added by cstein.
 *
 * @author <a href="mailto:pcote@expedia.com">Pierre Cote</a>
 */
public interface IMonitorNumberFamily
{
	/**
	 * Call begin ... you guessed it, at the beginning of an operation to monitor.
	 * Your application should keep the value returned by the call to begin() and
	 * pass it back to the IMonitorCounter.end() method. This allows the monitoring
	 * system to match begin and end to measure tx duration time.
	 * 
	 * @param instance The monitor instance within the family to increment.
	 * @return Returns a context object to be used when calling the end() method
	 * to stop monitoring.
	 */
	IMonitorContext begin(String instance);
	
	/**
	 * When your operation is done, call end() and pass it the IMonitorContext
	 * it gave you in the call to begin().
	 * 
	 * @param ctx The monitor context that was return from the call to begin().
	 * @param fSuccess The flag fSuccess will be used to determine percentage 
	 * of success and failures.
	 * @param n The new number on which to keep statistics.  Note that n is 
	 * ignored if fSuccess is false.
	 */
	void end(IMonitorContext ctx, boolean fSuccess, long n);

	/**
	 * Completely resets the counter to its startup state.
	 * This method is expected to be called only extremely rarely
	 */
	void clearCounter();

	/**
	 * Remove a counter that is no longer required.
	 * 
	 * @param instance The specific monitor instance within the family to delete.
	 */
	void deleteCounter(String instance);
}


package com.whalekiss.jms;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides base functionality for JMS resource pools, such as 
 * connection pools.
 * 
 * @author <a href="mailto:adoerhoefer@expedia.com">Alan Doerhoefer</a>
 */
public abstract class ResourcePool
{
	/** The max number of resources allowed.  */
	protected int m_maxPoolSize = 1;
	
	/** The minimum number of resources to start with. */
	protected int m_minPoolSize = 1;
	
	/** The max number of seconds to wait for outstanding resources before shutdown. */
	protected int m_shutdownWaitTimeSeconds = 30;

	/** The current number of connections. */
	protected AtomicInteger m_currentCount = new AtomicInteger(0);
	
	/** True if the pool is running. */
	protected boolean m_running = false;
	
	/** True if the pool has been started successfully. */
	protected boolean m_started = false;
	
	/**
	 * Sets the maximum pool size.
	 * 
	 * @param maxPoolSize the maxPoolSize to set
	 */
	public void setMaxPoolSize(int maxPoolSize)
	{
		m_maxPoolSize = maxPoolSize;
	}

	/**
	 * Sets the minimum pool size.
	 * 
	 * @param minPoolSize the minPoolSize to set
	 */
	public void setMinPoolSize(int minPoolSize)
	{
		m_minPoolSize = minPoolSize;
	}

	/**
	 * Returns the shutdown wait time in seconds.
	 * 
	 * @return the shutdownWaitTimeSeconds
	 * @see #setShutdownWaitTimeSeconds(int)
	 */
	public int getShutdownWaitTimeSeconds()
	{
		return m_shutdownWaitTimeSeconds;
	}

	/**
	 * Sets the shutdown wait time in seconds.
	 * 
	 * @param shutdownWaitTimeSeconds the shutdownWaitTimeSeconds to set
	 * @see #getShutdownWaitTimeSeconds()
	 */
	public void setShutdownWaitTimeSeconds(int shutdownWaitTimeSeconds)
	{
		m_shutdownWaitTimeSeconds = shutdownWaitTimeSeconds;
	}
	
}

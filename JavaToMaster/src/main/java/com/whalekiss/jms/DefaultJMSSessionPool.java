package com.whalekiss.jms;

import javax.jms.Session;

/**
 * A thread-safe configurable pool that holds JMSSessionBag objects, which each encapsulate
 * a JMS Session and its JMS Connection.
 * 
 * @author <a href="mailto:adoerhoefer@expedia.com">Alan Doerhoefer</a>
 */
@SuppressWarnings("PMD.TooManyFields")
public class DefaultJMSSessionPool extends ResourcePool //implements IJMSSessionPool, MessageListener
{		
	///** Event log id for INFO on concurrency settings such as number of listeners, etc */
	//protected static final int EVENT_INFO_CONCURRENCY = 1;
	
	//** Event log id for INFO on general life cycle events etc */
	//protected static final int EVENT_INFO = 100;
	
	///** Event log id for INFO on session related events, etc */
	//protected static final int EVENT_INFO_SESSION = 101;
	
	///** Event log id for ERROR on session related events, etc */
	//protected static final int EVENT_ERROR_SESSION = 500;
	
	///** to shut up pmd */
	//protected static final String DEF_JMS_SESSIONPOOL_STRING = "DefaultJMSSessionPool ";
	
	///** The blocking priority queue for the sessions. */
	//protected PriorityBlockingQueue<JMSSessionBag> m_sessionQueue;
		
	/** Set to true to make the sessions created by this pool transactional. */
	protected boolean m_sessionsAreTransactional = false;
	
	/** The Session acknowledge mode for sessions created by this pool. */
	protected int m_sessionAckMode = Session.AUTO_ACKNOWLEDGE;
	
	///** The SessionType for the sessions in this pool. */
	//protected SessionType m_sessionType = SessionType.PRODUCER;
	
	/** The default incoming destination name. */
	protected String m_incomingDestinationName;
	
	/** The default outgoing destination name. */
	protected String m_outgoingDestinationName;
	
	///** The default selector to use when creating a consumer. */
	//protected String m_selector;
	
	///** The incoming destination. */
	//protected Destination m_incomingDestination;
	
	///** The outgoing destination. */
	//protected Destination m_outgoingDestination;
	
	///** The message listener instance called by the incoming sessions. */ 
	//protected MessageListener m_messageListener;
	
	/** Set to true if incoming destination is a topic. */
	protected boolean m_incomingDestIsTopic = false;
	
	/** Set to true if the outgoing destination is a topic. */
	protected boolean m_outgoingDestIsTopic = false;
	
	/** Set to true if a temp queue is to be used for incoming. */
	protected boolean m_useTemporaryQueue = false;

	/** The connection pool used by this session pool. */
	protected DefaultJMSConnectionPool m_connectionPool;
	
	///** The jndi context object used to do a JNDI lookup for the connection factory. */
	//protected IJNDIContext m_jndiContext;
	
	///** The JNDI name of the connection factory to lookup. */
	//protected String m_jndiConnectionFactoryName;
	
	/** The max number of seconds to wait for a session to become available */
	protected int m_sessionWaitTimeOutSeconds = 120;
	
	///** The logger. */
	//protected Logger m_logger = Logger.getLogger(this.getClass());
	
	///** A user supplied context to help identify instance in logging etc */
	//protected String m_userContext = "?";
	
	// note, using defaults in the code for time out completion monitoring
	// as we want it by default and since there is not a common abstract bean
	// for session pools, yet, want to insure this is on by default with
	// appropriate values even when custom session pool beans are added,
	// e.g. config updatd svc, now or in the future.	
	
	/** certain code paths are monitored for completion when set to true */
	protected boolean m_useTimeCompletionMonitoring = true;
	
	///** A monitor that is used to check for non completing code sections */
	//protected TimedCodeCompletionMonitor m_completionMonitor = null;
	
	protected int m_timeCompletionMonitoringTimeOutSeconds = 300;
	/*
	/** 
	 * Starts the session pool and it's connection pool.
	 */
	/*
	public void start() throws Exception
	{
		synchronized(this)
		{
			if (m_started)
			{
				//throw new UnsupportedOperationException("The pool has already been started.");
				return;
			}

			// Always set it to true after this method has been called.
			m_running = true;

			// Init resources
			m_sessionQueue = new PriorityBlockingQueue<JMSSessionBag>();
			
			// start the the code completion time out monitor, needs to be started 
			// before jms is up and running
			
			if (m_useTimeCompletionMonitoring)
			{
				m_completionMonitor = new TimedCodeCompletionMonitor("TimedCodeCompletionMonitor: "
						                                             + m_userContext);
				EventLogEntry.logEvent(m_logger, 
			               Level.INFO, 
			               EVENT_INFO,
			               DEF_JMS_SESSIONPOOL_STRING
			             + m_userContext
			             + " starting TimedCompletionMonitor, "
			             + "with delay of "
			             + m_timeCompletionMonitoringTimeOutSeconds * 1000
			             + " millis");
			}
 
			// Init and start the connection pool
			m_connectionPool.setUserContext(m_userContext);
			m_connectionPool.setJndiConnectionFactoryName(m_jndiConnectionFactoryName);
			m_connectionPool.setJndiContext(m_jndiContext);
			m_connectionPool.start();

			// Pre-start sessions
			if (m_sessionType == SessionType.CONSUMER)
			{
				createListeningSessions();
			}
			else
			{
				createMinSessions();
			}

			m_started = true;

			EventLogEntry.logEvent(m_logger, 
		               Level.INFO, 
		               EVENT_INFO,
		               DEF_JMS_SESSIONPOOL_STRING
		             + m_userContext
		             + " Started");
		}
	}
	
	/**
	 * Stops the session pool and then the connection pool.
	 */
	/*
	public void stop() throws Exception
	{
		synchronized(this)
		{
			// Allow stop() to be called more than once since it won't do any harm.
			if (m_running)
			{
				m_started = false;
				m_running = false;

				if (m_currentCount.get() > 0)
				{
					// Wait shutdownWaitTimeSeconds for any in-use connections to be returned to the pool
					if (m_sessionQueue.size() < m_currentCount.get())
					{
						long maxWaitTimeMs = System.currentTimeMillis() + (1000 * m_shutdownWaitTimeSeconds);
						while (m_sessionQueue.size() < m_currentCount.get()
								&& System.currentTimeMillis() < maxWaitTimeMs)
						{
							Thread.sleep(500);
						}
					}

					// Close and remove all the sessions in the pool
					for (JMSSessionBag session : m_sessionQueue)
					{
						session.close();
					}

					m_sessionQueue.clear();
					m_sessionQueue = null;

					m_currentCount.set(0);
					
					m_completionMonitor.stop();
					m_completionMonitor = null;
				}

				m_connectionPool.stop();
				
				EventLogEntry.logEvent(m_logger, 
			               Level.INFO, 
			               EVENT_INFO,
			               DEF_JMS_SESSIONPOOL_STRING
			             + m_userContext
			             + " Stopped");
			}
		}
	}
	
	/**
	 * Gets an available session from the pool that's using the connection with the least
	 * current usage (based on sessions in use).  If one is not currently available it will
	 * attempt to create one (assuming under max).  As a last resort it will wait for one
	 * to become available up to the configured time limit.
	 * @return JMSSessionBag
	 * @throws Exception, MessageServiceException
	 */
	/*
	public JMSSessionBag getSession() throws Exception
	{
		if (!m_running)
		{
			throw new UnsupportedOperationException("The JMS session pool is no longer running.");
		}
		
		if (!m_started)
		{
			start();
		}
		
		JMSSessionBag bag = m_sessionQueue.poll();    // try to get a session if one is available
		if (null == bag)
		{
			if (m_currentCount.get() < m_maxPoolSize) // try to create a new one if not maxxed out
			{
				bag = createSession(false); 
			}
			
			if (null == bag)            // if that fails, wait for one up to time limit
			{
				bag = m_sessionQueue.poll(m_sessionWaitTimeOutSeconds, TimeUnit.SECONDS);
				// review, consider adding monitor counters so we see see how often & how long
				// we are waiting for sessions
				if (null == bag)
				{
					throw new MessageServiceException(m_userContext
							                         + " Timed out waiting for an available session after "
							                         + m_sessionWaitTimeOutSeconds
							                         + "seconds.  There are "
							                         + m_currentCount.get()
							                         + " sessions, max possible is "
							                         + m_maxPoolSize
							                         + ".");
				}
			}
		}
				
		bag.getConnectionRefCountable().increment();
		
		return bag;
	}
	
	/**
	 * Inits a producer on the session using the destination info
	 * already configured for the pool.
	 * 
	 * @param bag	the JMS session
	 * @throws Exception
	 */
	/*
	protected void createProducer(JMSSessionBag bag) throws Exception
	{		
		// Init the outgoing destination if it hasn't already been done
		if (null != m_outgoingDestinationName
				&& null == m_outgoingDestination)
		{
			if (m_outgoingDestIsTopic)
			{
				m_outgoingDestination = bag.getSession().createTopic(m_outgoingDestinationName);
			}
			else
			{
				m_outgoingDestination = bag.getSession().createQueue(m_outgoingDestinationName);
			}
		}
		
		bag.setProducer(bag.getSession().createProducer(m_outgoingDestination));
	}
	
	/**
	 * Inits a consumer on the session using the destination, temp queue,
	 * message listener, and selector already configured for the pool.
	 * 
	 * @param bag	the JMS session
	 * @throws Exception
	 */
	/*
	protected void createConsumer(JMSSessionBag bag) throws Exception
	{
		// Init the incoming destination if it hasn't already been done
		if (null == m_incomingDestination)
		{
			if (null == m_incomingDestinationName && m_useTemporaryQueue)
			{
				m_incomingDestination = bag.getSession().createTemporaryQueue();
			}
			else if (m_incomingDestIsTopic)
			{
				m_incomingDestination = bag.getSession().createTopic(m_incomingDestinationName);
			}
			else
			{
				m_incomingDestination = bag.getSession().createQueue(m_incomingDestinationName);
			}
		}
		
		// Create the consumer and set the message listener
		MessageConsumer consumer = bag.getSession().createConsumer(m_incomingDestination, m_selector);
		
		if (m_useTimeCompletionMonitoring)
		{
			// set ourselves as the listener so that we can get control
			// and monitor the real listener handler for timed completion
			consumer.setMessageListener(this);
		}
		else
		{
			consumer.setMessageListener(m_messageListener);
		}
		
		bag.setConsumer(consumer);
	}
	
	/**
	 * Release the session back to the pool.
	 * @param session	the JMS session
	 */
	/*
	public void releaseSession(JMSSessionBag session)
	{	
		session.getConnectionRefCountable().decrement();
		m_sessionQueue.put(session);
	}
	
	
	/**
	 * Creates min pool size sessions and adds them to the queue.
	 * 
	 * @throws Exception
	 */
	/*
	protected void createMinSessions() throws Exception
	{
		String msg = " Creating min configured jms inbound sessions = ";
		if (m_sessionType == SessionType.PRODUCER)
		{
			msg = " Creating min configured jms outbound sessions = ";
		}		
		
		EventLogEntry.logEvent(m_logger, 
	                           Level.INFO, 
	                           EVENT_INFO_CONCURRENCY,
	                           m_userContext 
	                         + msg 
	                         + m_minPoolSize
	                         + ", max = "
	                         + m_maxPoolSize);
		
		for (int i = 0; i < m_minPoolSize; i++)
		{
			m_sessionQueue.put(createSession(true));
		}
	}
	
	/**
	 * Creates maxPoolSize sessions and message consumers
	 * and registers the messageListener on each one.
	 * 
	 * @throws Exception
	 */
	/*
	protected void createListeningSessions() throws Exception
	{
		EventLogEntry.logEvent(m_logger, 
				               Level.INFO, 
				               EVENT_INFO_CONCURRENCY,
				               m_userContext
				             + " Creating max configured jms session/listeners = " + m_maxPoolSize);
		
		for (int i = 0; i < m_maxPoolSize; i++)
		{
			m_sessionQueue.put(createSession(true));
		}
	}
	
	/**
	 * Creates a new session.
	 * 
	 * @param isPreCreate set to true if we are in pre-heat mode and we want the next
	 * connection round-robined regardless if they all have zero usage. 
	 * @return JMSSessionBag or null if maxxed out
	 * @throws Exception
	 */
	/*
	protected JMSSessionBag createSession(boolean isPreCreate) throws Exception
	{
		JMSSessionBag bag = null;
		
		synchronized(this)
		{
			if (m_currentCount.get() < m_maxPoolSize)
			{
				boolean isOk = false;
				try
				{
					bag = new JMSSessionBag(getConnection(isPreCreate));
					bag.setTimeOutMonitor(m_completionMonitor, m_timeCompletionMonitoringTimeOutSeconds);
		
					bag.setSession(bag.getConnection().createSession(m_sessionsAreTransactional, m_sessionAckMode));
		
					if (m_sessionType == SessionType.CONSUMER)
					{
						createConsumer(bag);
		
						EventLogEntry.logEvent(m_logger, 
		                                       Level.INFO, 
		                                       EVENT_INFO_SESSION,
		                                       m_userContext
		                                       + " Created a new consumer session"); 
					}
					else
					{
						createProducer(bag);
		
						EventLogEntry.logEvent(m_logger, 
                                Level.INFO, 
                                EVENT_INFO_SESSION,
                                m_userContext
                                + " Created a new producer session"); 
					}
					isOk = true;
				}
				finally
				{
					if (!isOk)
					{
						if (null != bag)
						{
							try
							{
								bag.close();  // make sure we do not collect orphaned sessions, producers etc.
							}
							catch(Throwable t)
							{
								EventLogEntry.logEvent(m_logger, 
		                                Level.ERROR, 
		                                EVENT_ERROR_SESSION,
		                                m_userContext
	                                  + "Problem closing session bag that could not be completely created",
		                                t); 
								
							}
							finally  // should normally get here due to exception so nulling out bag
							{        // not normally required, but just in case....
								bag = null;
							}
						}
					}
					else
					{
						m_currentCount.incrementAndGet();
					}
				}
			}
		}
		return bag;
	}

	/**
	 * Gets a connection from the connection pool.
	 * @param isPreCreate set to true if we are in pre-heat mode and we want the next
	 * connection round-robined regardless if they all have zero usage. 
	 * @return ConnectionRefCountable
	 * @throws Exception
	 */
	/*
	protected ConnectionRefCountable getConnection(boolean isPreCreate) throws Exception
	{		
		return m_connectionPool.getConnection(isPreCreate); 		
	}
	*/	
	/**
	 * The Session acknowledge mode for sessions created by this pool.
	 * 
	 * @param sessionAckMode the sessionAckMode to set
	 */
	public void setSessionAckMode(int sessionAckMode)
	{
		m_sessionAckMode = sessionAckMode;
	}

	/**
	 * Set to true to make the sessions created by this pool transactional.
	 * 
	 * @param sessionsAreTransactional the sessionsAreTransactional to set
	 */
	public void setSessionsAreTransactional(boolean sessionsAreTransactional)
	{
		m_sessionsAreTransactional = sessionsAreTransactional;
	}

	/**
	 * Specifies the incoming destination name.
	 * 
	 * @param defaultIncomingDestinationName the defaultIncomingDestinationName to set
	 * @see IJMSSessionPool#setIncomingDestinationName(String)
	 */
	public void setIncomingDestinationName(
			String defaultIncomingDestinationName)
	{
		m_incomingDestinationName = defaultIncomingDestinationName;
	}

	/**
	 * Specifies the outgoing destination name.
	 * 
	 * @param defaultOutgoingDestinationName the defaultOutgoingDestinationName to set
	 * @see IJMSSessionPool#setOutgoingDestinationName(String)
	 */
	public void setOutgoingDestinationName(
			String defaultOutgoingDestinationName)
	{
		m_outgoingDestinationName = defaultOutgoingDestinationName;
	}
    /*
	/**
	 * The default selector to use when creating a consumer.
	 * 
	 * @param defaultSelector the defaultSelector to set
	 */
	/*
	public void setSelector(String defaultSelector)
	{
		m_selector = defaultSelector;
	}

    */
	/**
	 * Specifies the session type.
	 * 
	 * @param defaultSessionType the defaultSessionType to set
	 */
	public void setSessionType(String defaultSessionType)
	{
		// just for backward compatability to existing bean generation
	}

	/**
	 * Specifies whether the incoming destination is a topic or not.
	 * 
	 * @param incomingDestIsTopic the incomingDestIsTopic to set
	 * @see IJMSSessionPool#setIncomingDestIsTopic(boolean)
	 */
	public void setIncomingDestIsTopic(boolean incomingDestIsTopic)
	{
		m_incomingDestIsTopic = incomingDestIsTopic;
	}
    /*
	/**
	 * Specifies the message listener.
	 * 
	 * @param messageListener the messageListener to set
	 * @see IJMSSessionPool#setMessageListener(MessageListener)
	 */
	/*
	public void setMessageListener(MessageListener messageListener)
	{
		m_messageListener = messageListener;
	}
    */
	/**
	 * Specifies whether the outgoing destination is a topic or not.
	 * 
	 * @param outgoingDestIsTopic whether the outgoing destination is a topic or not
	 * @see IJMSSessionPool#setOutgoingDestIsTopic(boolean)
	 */
	public void setOutgoingDestIsTopic(boolean outgoingDestIsTopic)
	{
		m_outgoingDestIsTopic = outgoingDestIsTopic;
	}

	/**
	 * Specifies whether to use temporary queue.
	 * 
	 * @param useTemporaryQueue whether to use temporary queue
	 * @see IJMSSessionPool#setUseTemporaryQueue(boolean)
	 */
	public void setUseTemporaryQueue(boolean useTemporaryQueue)
	{
		m_useTemporaryQueue = useTemporaryQueue;
	}

	/**
	 * Specifies the connection pool.
	 * 
	 * @param connectionPool the connectionPool to set
	 */
	public void setConnectionPool(DefaultJMSConnectionPool connectionPool)
	{
		m_connectionPool = connectionPool;
	}
    /*
	/**
	 * @return the incomingDestination
	 */
	/*
	public Destination getIncomingDestination()
	{
		return m_incomingDestination;
	}
	
	/**
	 * Set the JNDI name for the ConnectionFactory to use.
	 * @param name the JNDI connection factory name
	 */
	/*
	public void setJndiConnectionFactoryName(String name)
	{
		m_jndiConnectionFactoryName = name;
	}
	
	/**
	 * Set the IJNDIContext used for JNDI lookup of the connection factory.
	 * @param jndiContext	the JNDI context object
	 */
	/*
	public void setJndiContext(IJNDIContext jndiContext)
	{
		m_jndiContext = jndiContext;
	}
	*/
	/** 
	 * Set the seconds we will wait for session to become available before erroring out.
	 * @see com.expedia.e3.platform.foundation.busproviders.jms.ResourcePool#setShutdownWaitTimeSeconds(int)
	 */
	public void setSessionWaitTimeOutSeconds(int sessionWaitTimeOutSeconds)
	{
		m_sessionWaitTimeOutSeconds = sessionWaitTimeOutSeconds;
	}

	/**
	 * If set to true jms sends and receives (onMessage)will be monitored for
	 * timely completion.
	 * @param value
	 */
	public void setUseTimeCompletionMonitoring(boolean value)
	{
		m_useTimeCompletionMonitoring = value;
	}
	
	/**
	 * Set how long a send or recieve (onMessage) must complete
	 * before to avoid an error event.
	 * @param seconds
	 */
	public void setTimeCompletionMonitoringTimeOutSeconds(int seconds)
	{
		m_timeCompletionMonitoringTimeOutSeconds = seconds;
	}
	/*	
	/** 
	 * Allows context to be set that will be used in logging).
	 * @see com.expedia.e3.platform.foundation.busproviders.jms.IJMSSessionPool#setUserContext(java.lang.String)
	 */
	/*
	public void setUserContext(String userContext) 
	{
		if (null == userContext)
		{
			m_userContext = "null";
		}
		else
		{
			m_userContext = userContext;
		}
	}

	/**
	 * A wrapper message listener that does time completion monitoring
	 * on the configured listener.
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	/*
	public void onMessage(Message msg) 
	{
		TimedCodeCompletionEvent timedEvent = null;
		if (null != m_completionMonitor)
		{
			timedEvent = new TimedCodeCompletionEvent(m_timeCompletionMonitoringTimeOutSeconds * 1000, 
					                                  "JmsMessageListener.onMessage");

			m_completionMonitor.startEvent(timedEvent);
		}
		try
		{
			m_messageListener.onMessage(msg);
		}
		finally
		{
			if (null != m_completionMonitor)
			{
				m_completionMonitor.stopEvent(timedEvent);
			}
		}
	}
	*/
}


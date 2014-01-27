package com.whalekiss.jms;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.whalekiss.log.eventlog.EventIDs;
import com.whalekiss.log.eventlog.EventLogEntry;
import com.whalekiss.util.StopWatch;
import com.whalekiss.util.bean.manage.BeanFactory;
import com.whalekiss.util.moitor.IMonitorContext;
import com.whalekiss.util.moitor.IMonitorNumberFamily;

public class MQSession implements Session{
	private static final Logger LOGGER = Logger.getLogger(MQSession.class);
	
	private static IMonitorNumberFamily s_receiveMessageMonitorNumberFamily = null;
	private static IMonitorNumberFamily s_getConsumerMonitorNumberFamily = null;
	private static IMonitorNumberFamily s_dispatchMessageMonitorNumberFamily = null;
	private static final AtomicBoolean LOADED_MONITOR_COUNTERS = new AtomicBoolean(false);
	
	private final AtomicBoolean m_closed = new AtomicBoolean(false);
	private final AtomicBoolean m_paused = new AtomicBoolean(false);
	//FIFO Queue
	private final BlockingQueue<Message> m_processingQueue = new SynchronousQueue<Message>(true);
	private final SessionRunnable m_processingRunnable;
	
	private final MQConnection m_connection;
	private final AtomicBoolean m_providerURLValidated = new AtomicBoolean(false);
	private String m_providerURL = null;
	private String m_username = null;
	private String m_password = null;

	MQSession(MQConnection connection, String providerURL, String username, String password)
	{
		if (LOADED_MONITOR_COUNTERS.compareAndSet(false, true))
		{
			s_receiveMessageMonitorNumberFamily =
				lookupMonitorNumberFamily(this.getClass().getName() + ".ReceiveMessageMonitorNumberFamily");
			s_getConsumerMonitorNumberFamily =
				lookupMonitorNumberFamily(this.getClass().getName() + ".GetConsumerMonitorNumberFamily");
			s_dispatchMessageMonitorNumberFamily =
				lookupMonitorNumberFamily(this.getClass().getName() + ".DispatchMessageMonitorNumberFamily");
		}
		m_connection = connection;
		m_providerURL = providerURL;
		m_username = username;
		m_password = password;
		m_processingRunnable = this.new SessionRunnable();
		// JMS sessions are "started" at creation time
		start();
	}

	public BytesMessage createBytesMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public MapMessage createMapMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public Message createMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectMessage createObjectMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectMessage createObjectMessage(Serializable object)
			throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public StreamMessage createStreamMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public TextMessage createTextMessage() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public TextMessage createTextMessage(String text) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getTransacted() throws JMSException {
		// TODO Auto-generated method stub
		return false;
	}

	public int getAcknowledgeMode() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void commit() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public void rollback() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public void close() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public void recover() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public MessageListener getMessageListener() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMessageListener(MessageListener listener)
			throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}

	public MessageProducer createProducer(Destination destination)
			throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public MessageConsumer createConsumer(Destination destination)
			throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public MessageConsumer createConsumer(Destination destination,
			String messageSelector) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public MessageConsumer createConsumer(Destination destination,
			String messageSelector, boolean NoLocal) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public Queue createQueue(String queueName) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public Topic createTopic(String topicName) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public TopicSubscriber createDurableSubscriber(Topic topic, String name)
			throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public TopicSubscriber createDurableSubscriber(Topic topic, String name,
			String messageSelector, boolean noLocal) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public QueueBrowser createBrowser(Queue queue) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public QueueBrowser createBrowser(Queue queue, String messageSelector)
			throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public TemporaryQueue createTemporaryQueue() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public TemporaryTopic createTemporaryTopic() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsubscribe(String name) throws JMSException {
		// TODO Auto-generated method stub
		
	}
	
	private IMonitorNumberFamily lookupMonitorNumberFamily(String name)
	{
		if (!BeanFactory.containsBean(name))
		{
			return null;
		}
		Object obj = BeanFactory.getBean(name);
		if (!(obj instanceof IMonitorNumberFamily))
		{
			return null;
		}
		return (IMonitorNumberFamily) obj;
	}
	
	/**
	 * 
	 * This represents our JMS delivery thread for JMS.  The HTTP threads handoff messages for processing
	 * via our blocking queue, these threads pick them up and deliver them through the system.  Based on
	 * the session starting/stopping these threads may end up being executed multiple times.
	 * 
	 * 分发message的线程
	 *
	 */
	private class SessionRunnable implements Runnable
	{
		public void run()
		{
			while (!m_paused.get() && !m_closed.get())
			{
				try
				{
					Message msg = MQSession.this.m_processingQueue.poll(1, TimeUnit.SECONDS);
					if (null != msg)
					{
						dispatchMessage(msg);
					}
				}
				catch (InterruptedException ex)
				{
					// ignore since we check closed above
				}
			}
		}
		
		/**
		 * Dispatches the supplied message to the next available consumer listening on the messages
		 * destination.
		 * 
		 * @param message the message to dispatch.
		 * @throws InterruptedException
		 */
		private void dispatchMessage(Message message) throws InterruptedException
		{
			IMonitorContext monCtx = null;
			StopWatch sw = new StopWatch();
			if (null != s_dispatchMessageMonitorNumberFamily)
			{
				monCtx = s_dispatchMessageMonitorNumberFamily.begin(message.getClass().getName());
				sw.start();
			}
			boolean good = false;
			try
			{
				MQMessageConsumer consumer = getConsumer(message);
				if (null == consumer)
				{
					EventLogEntry.logEvent(LOGGER, Level.ERROR, EventIDs.NO_CONSUMER_FOUND_ERROR,
							"No message consumer found to process message!");
					return;
				}
				if (!consumer.receiveMessage(message))
				{
					EventLogEntry.logEvent(LOGGER, Level.ERROR, EventIDs.CONSUMER_REFUSED_MESSAGE_ERROR,
							"Consumer refused to process message!");
				}
				else
				{
					good = true;
				}
			}
			finally
			{
				if (null != monCtx)
				{
					s_dispatchMessageMonitorNumberFamily.end(monCtx, good, sw.stop());
				}
			}
		}
		
		/**
		 * Retrieves the next available consumer from our blocking queue based on the supplied messages
		 * destination.
		 * 
		 * @param message the message to retrieve a consumer for.
		 * @return the next available consumer if one is available, null otherwise.
		 * @throws InterruptedException
		 */
		private MQMessageConsumer getConsumer(Message message) throws InterruptedException
		{
			IMonitorContext monCtx = null;
			StopWatch sw = new StopWatch();
			if (null != s_getConsumerMonitorNumberFamily)
			{
				monCtx = s_getConsumerMonitorNumberFamily.begin(message.getClass().getName());
				sw.start();
			}
			boolean good = false;
			try
			{
				Destination dest = null;
				m_consumersLock.readLock().lock();
				try
				{
					dest = message.getJMSDestination();
					LockableLinkedBlockingQueue<MQMessageConsumer> consumers = m_consumers.get(dest);
					if (null != consumers && 0 < consumers.size())
					{
						MQMessageConsumer consumer = consumers.rotateBack();
						good = (null != consumer);
						return consumer;
					}
				}
				catch (JMSException ex)
				{
					EventLogEntry.logEvent(LOGGER, Level.ERROR, EventIDs.MESSAGE_HAS_NO_DESTINATION_ERROR,
							"", ex);
				}
				finally
				{
					m_consumersLock.readLock().unlock();
				}
				return null;
			}
			finally
			{
				if (null != monCtx)
				{
					s_getConsumerMonitorNumberFamily.end(monCtx, good, sw.stop());
				}
			}
		}
	}
}


}

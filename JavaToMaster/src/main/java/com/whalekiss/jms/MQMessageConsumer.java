package com.whalekiss.jms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import com.whalekiss.util.StopWatch;
import com.whalekiss.util.bean.manage.BeanFactory;
import com.whalekiss.util.moitor.IMonitorContext;
import com.whalekiss.util.moitor.IMonitorNumberFamily;

public class MQMessageConsumer implements MessageConsumer {
	
	private static final AtomicBoolean LOADED_MONITOR_COUNTERS = new AtomicBoolean(false);
	private static IMonitorNumberFamily s_receiveMessageMonitorNumberFamily = null;
	
	private final AtomicBoolean m_closed = new AtomicBoolean(false);
	private final BlockingQueue<Message> m_queue = new SynchronousQueue<Message>();
	private MQSession m_session = null;
	private MQQueue m_destination = null;
	private MessageListener m_listener = null;
	private String m_selector = null;
	
	public MQMessageConsumer(MQSession session, MQQueue destination, String messageSelector)
	{
		if (LOADED_MONITOR_COUNTERS.compareAndSet(false, true))
		{
			s_receiveMessageMonitorNumberFamily =
				lookupMonitorNumberFamily(this.getClass().getName() + ".ReceiveMessageMonitorNumberFamily");
		}
		m_session = session;
		m_destination = destination;
		m_selector = messageSelector;
	}

	public String getMessageSelector() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public MessageListener getMessageListener() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMessageListener(MessageListener listener)
			throws JMSException {
		// TODO Auto-generated method stub

	}

	public Message receive() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public Message receive(long timeout) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public Message receiveNoWait() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}
	

	/**
	 * Called from our session to deliver an inbound message via this consumer.  If a listener is present will deliver
	 * directly to it, otherwise attempts to offer to our {@link BlockingQueue}.
	 * 
	 * @param msg the message to deliver.
	 * @return true if the message was delivered.
	 */
	boolean receiveMessage(Message msg)
	{
		if (m_closed.get())
		{
			return false;
		}
		IMonitorContext monCtx = null;
		StopWatch sw = new StopWatch();
		if (null != s_receiveMessageMonitorNumberFamily)
		{
			monCtx = s_receiveMessageMonitorNumberFamily.begin(msg.getClass().getName());
			sw.start();
		}
		boolean good = false;
		try
		{
			if (null != m_listener)
			{
				m_listener.onMessage(msg);
				good = true;
			}
			else
			{
				good = m_queue.offer(msg);
			}
			return good;
		}
		finally
		{
			if (null != monCtx)
			{
				s_receiveMessageMonitorNumberFamily.end(monCtx, good, sw.stop());
			}
		}
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

}

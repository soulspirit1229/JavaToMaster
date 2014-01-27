package com.whalekiss.jms;

import javax.jms.JMSException;
import javax.jms.Queue;

public class MQQueue implements Queue {

	private final String m_queueName;
	
	MQQueue(String queueName)
	{
		m_queueName = queueName;
	}

	/** 
	 * @see javax.jms.Queue#getQueueName()
	 */
	public String getQueueName() throws JMSException
	{
		return m_queueName;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof MQQueue)
		{
			MQQueue q = (MQQueue) obj;
			return (m_queueName == q.m_queueName) ? true : (
					(null != m_queueName && m_queueName.equals(q.m_queueName)) ? true : false
				);
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		if (null == m_queueName)
		{
			return System.identityHashCode(this);
		}
		return m_queueName.hashCode();
	}

}

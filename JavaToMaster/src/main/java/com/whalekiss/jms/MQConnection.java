package com.whalekiss.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import com.whalekiss.util.concurrent.PlatformReentrantReadWriteLock;

public class MQConnection implements Connection{
	
	private final AtomicBoolean m_closed = new AtomicBoolean(false);
	private final AtomicBoolean m_running = new AtomicBoolean(false);
	private String m_clientID;
	private String m_providerURL = null;
	private String m_username = null;
	private String m_password = null;
	private static final AtomicInteger NEXT_CLIENT_ID = new AtomicInteger(0);
	private final ReadWriteLock m_sessionsLock = new PlatformReentrantReadWriteLock();
	private final List<MQSession> m_sessions = new ArrayList<MQSession>();
	
	MQConnection(String providerURL, String username, String password)
	{
		m_providerURL = providerURL;
		m_username = username;
		m_password = password;
		int id = NEXT_CLIENT_ID.incrementAndGet();
		m_clientID = "http.jmsprovider " + id;
	}

	public Session createSession(boolean transacted, int acknowledgeMode)
			throws JMSException {
		if (m_closed.get())
		{
			throw new IllegalStateException("Cannot call createSession on closed Connection!");
		}
		MQSession session = new MQSession(this, m_providerURL, m_username, m_password);
		
		//上写锁，不允许其他人读写
		m_sessionsLock.writeLock().lock();
		try
		{
			m_sessions.add(session);
		}
		finally
		{
			m_sessionsLock.writeLock().unlock();
		}
		return session;
	}

	public String getClientID() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setClientID(String clientID) throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public ConnectionMetaData getMetaData() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public ExceptionListener getExceptionListener() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setExceptionListener(ExceptionListener listener)
			throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public void start() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public void stop() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public void close() throws JMSException {
		// TODO Auto-generated method stub
		
	}

	public ConnectionConsumer createConnectionConsumer(Destination destination,
			String messageSelector, ServerSessionPool sessionPool,
			int maxMessages) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public ConnectionConsumer createDurableConnectionConsumer(Topic topic,
			String subscriptionName, String messageSelector,
			ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

}

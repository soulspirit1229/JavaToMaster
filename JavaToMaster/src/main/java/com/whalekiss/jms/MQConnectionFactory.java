package com.whalekiss.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

public class MQConnectionFactory implements ConnectionFactory{
	
	private String m_providerURL = null;
	private String m_username = null;
	private String m_password = null;

	public Connection createConnection() throws JMSException {
		
		return new MQConnection(m_providerURL,m_username,m_password);
	}

	public Connection createConnection(String userName, String password)
			throws JMSException {
	
		return new MQConnection(m_providerURL,userName,password);
	}

}

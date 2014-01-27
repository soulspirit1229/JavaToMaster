package com.whalekiss.jms;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class QueueSend {
	public static void main(String[] args) throws JMSException {
		ConnectionFactory connectionfactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		//创建与JMS服务的连接:ConnectionFactory被管理的对象，由客户端创建，用来创建一个连接对象
		Connection connection = connectionfactory.createConnection();//获取连接，connection一个到JMS系统提供者的活动连接
		Session session =connection.createSession(false,Session.AUTO_ACKNOWLEDGE );//打开会话，一个单独的发送和接受消息的线程上下文
		
		sendTextMsg(session,"使用jms发送文本消息","queue.msgText");
		
		MapMessage mapMessage = session.createMapMessage();
		mapMessage.setString("name", "nero");
		mapMessage.setBoolean("male", true);
		sendMapMsg(session,mapMessage,"queue.msgMap");
		
		Person person = new Person("lixunhuan");
		sendObjMsg(session,person,"queue.msgObj");
		
        session.close();
		connection.close();

    }

	private static void sendTextMsg(Session session,String msgText,String queueName) throws JMSException {
		Queue queue = new ActiveMQQueue(queueName);
		MessageProducer msgProducer = session.createProducer(queue);
		Message msg = session.createTextMessage(msgText);
		msgProducer.send(msg);
		System.out.println("文本消息已发送");
	}
	
	private static void sendMapMsg(Session session,MapMessage map,String queueName) throws JMSException {
		Queue queue = new ActiveMQQueue(queueName);
		MessageProducer msgProducer = session.createProducer(queue);
		msgProducer.send(map);
		System.out.println("map消息已发送");
	}
	
	private static void sendObjMsg(Session session,Object obj,String queueName) throws JMSException {
		Queue queue = new ActiveMQQueue(queueName);
		ObjectMessage objMsg = session.createObjectMessage((Serializable)obj);
		MessageProducer msgProducer = session.createProducer(queue);
		msgProducer.send(objMsg);
		System.out.println("obj消息已发送");
	}
}

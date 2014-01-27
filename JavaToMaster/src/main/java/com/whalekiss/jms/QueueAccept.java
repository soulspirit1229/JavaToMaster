package com.whalekiss.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class QueueAccept implements MessageListener{
	public static void main(String[] args) throws JMSException {
		ConnectionFactory connectionfactory =null;
		Connection connection=null;
		Session session=null;
		if(connectionfactory==null){
			connectionfactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		}
		if(connection==null){
		 connection = connectionfactory.createConnection();
			connection.start();
		 }
		 session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue queue = new ActiveMQQueue("queue.msgText");//根据发送的名称接受消息
		MessageConsumer consumer = session.createConsumer(queue);
		consumer.setMessageListener(new QueueAccept());//不继承MessageListener时可以用consumer.receive()手动接受消息
		
		Queue queue1 = new ActiveMQQueue("queue.msgMap");
		MessageConsumer consumer1 = session.createConsumer(queue1);
		consumer1.setMessageListener(new QueueAccept());
		
		Queue queue2 = new ActiveMQQueue("queue.msgObj");
		MessageConsumer consumer2 = session.createConsumer(queue2);
		consumer2.setMessageListener(new QueueAccept());
	}
	
	public void onMessage(Message message) {
		/**
		 * 接受文本类型的消息 
		 */
		if(message instanceof TextMessage){ //instanceof 测试它所指向的对象是否是TextMessage类
			TextMessage text = (TextMessage) message;
			try {
				System.out.println("发送的文本消息内容为："+text.getText()); //接受文本消息 
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		/**
		 * 接受Map类型的消息
		 */
		if(message instanceof MapMessage){
			MapMessage map = (MapMessage) message;
			try {
				System.out.println("姓名："+map.getString("name"));
				System.out.println("是否是n男性："+map.getBoolean("male"));
				
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		if(message instanceof ObjectMessage){
			ObjectMessage objMsg = (ObjectMessage) message;
			try {
				Person person=(Person) objMsg.getObject();
				System.out.println("用户名："+person.getName());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}

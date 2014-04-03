package lab3.Server.MarketPlace;

import java.util.Properties;

import javax.jms.*;
import javax.naming.*;

public class MarketPlaceServer implements MessageListener{
private MarketPlace simpleMP = new MarketPlace();
	
	private Connection connection;
	private Session session;
	private Queue loginQueue;
	private Topic marketTopic;
	private MessageConsumer consumer;

	public static void main(String args[])
	{
		new MarketPlaceServer();
	}
	
	public void sendReply(Message request, boolean signUpInfo)
	{
		try
		{
			MessageProducer MP = session.createProducer(null);
			Destination reply = request.getJMSReplyTo();
			TextMessage TM = session.createTextMessage();
			TM.setText(""+signUpInfo);
			MP.send(reply, TM);
		}
		catch(JMSException JMSE)
		{
			System.out.println("JMS Exception: "+JMSE);
		}
	}
	
	public void sendReply(Message request, String signInInfo)
	{
		try
		{
			MessageProducer MP = session.createProducer(null);
			Destination reply = request.getJMSReplyTo();
			TextMessage TM = session.createTextMessage();
			TM.setText(""+signInInfo);
			MP.send(reply, TM);
		}
		catch(JMSException JMSE)
		{
			System.out.println("JMS Exception: "+JMSE);
		}
	}
	
		
	public void onMessage(Message message)
	{
		try
		{
		TextMessage TM = (TextMessage)message;
		String[] msg = TM.getText().split(":");
		
			if( msg[0].equalsIgnoreCase("signUp"))
			{
				String firstName = msg[1];
				String lastName = msg[2];
				String emailID = msg[3];
				String password = msg[4];
				String userID = msg[5];
				boolean signUpStatus = simpleMP.signUp(firstName,lastName,emailID,password, userID);
				sendReply(message, signUpStatus);
			}
			else if( msg[0].equalsIgnoreCase("signIn"))
			{
				String userID = msg[1];
				String password = msg[2];
				String signInInfo = simpleMP.signIn(userID, password);
				sendReply(message, signInInfo);
			}
			
			else if( msg[0].equalsIgnoreCase("storeAdvertisement"))
			{
				String itemName = msg[1];
				String itemDesc = msg[2];
				float price = Float.parseFloat(msg[3]);
				int quantity = Integer.parseInt(msg[4]);
				String userID = msg[5];
				boolean adInfo = simpleMP.storeAdvertisement(itemName, itemDesc, price, quantity, userID);
				sendReply(message, adInfo);
			}	
			
			else if( msg[0].equalsIgnoreCase("displayAdvertisement"))
			{
				String userID = msg[1];
				String[] displayAd = simpleMP.displayAdvertisement(userID);
				String displayAd2 = "";
				if(displayAd == null)
				{
					displayAd2 = "NO-AD";
				}
				else
				{				
				for(int i=0;i<displayAd.length; i++){
					displayAd2 = displayAd2 + displayAd[i] + "@";
				}
				}
				
				sendReply(message, displayAd2);
			}
			
			else if( msg[0].equalsIgnoreCase("signOut"))
			{
				String userID = msg[1];
				boolean signO = simpleMP.signOut(userID);
				sendReply(message, signO);
			}
			
		}
		catch(JMSException JMSE)
		{
			System.out.println("JMS Exception: "+JMSE);
		}
	}
	
	public MarketPlaceServer()
	{
		try
		{
		    Properties properties = new Properties();
		    properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		    properties.put(Context.URL_PKG_PREFIXES, "org.jnp.interfaces");
		    properties.put(Context.PROVIDER_URL, "localhost");
		    
			InitialContext jndi = new InitialContext(properties);
			ConnectionFactory conFactory = (ConnectionFactory)jndi.lookup("XAConnectionFactory");
			connection = conFactory.createConnection();
			
			session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
			
			try
			{
				loginQueue = (Queue)jndi.lookup("LoginQueue");
				marketTopic = (Topic)jndi.lookup("MarketTopic");
			}
			catch(NamingException NE1)
			{
				System.out.println("NamingException: "+NE1+ " : Continuing anyway...");
			}
			
			if( null == loginQueue )
			{
				loginQueue = session.createQueue("LoginQueue");
				jndi.bind("LoginQueue", loginQueue);
			}
			
			if( null == marketTopic )
			{
				marketTopic = session.createTopic("MarketTopic");
				jndi.bind("MarketTopic", marketTopic);
			}
			
			consumer = session.createConsumer( loginQueue );
			consumer.setMessageListener(this);
			
			consumer = session.createConsumer( marketTopic );
			consumer.setMessageListener(this);
			
			System.out.println("Server started waiting for client requests");
			connection.start();
		}
		catch(NamingException NE)
		{
			System.out.println("Naming Exception: "+NE);
		}
		catch(JMSException JMSE)
		{
			System.out.println("JMS Exception: "+JMSE);
			JMSE.printStackTrace();
		}
	}
}

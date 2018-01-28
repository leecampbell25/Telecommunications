import java.util.ArrayList;
import java.net.InetAddress;

public class Clients 
{

		private int sequenceNo;
		private int srcID;
		private int dstID;
		private InetAddress address;
		private int port;

		Clients(int sequenceNo, int srcID)
		{
			this.sequenceNo = sequenceNo;
			this.srcID = srcID;
		}

		Clients(int sequenceNo, int srcID, int dstID, InetAddress address, int port)
		{
			this.sequenceNo = sequenceNo;
			this.srcID = srcID;
			this.dstID = dstID;
			this.address = address;
			this.port = port;
		}
		
		 public static Clients getClient(ArrayList<Clients> clients, int srcID)
			{
				Clients client = null;
				for(int i = 0; i < clients.size(); i ++)
				{
					Clients temp = clients.get(i);
					if(temp.getsrcID() == srcID) client = temp;
				}
				return client;
			}
			
			 public static boolean clientAlreadyOnList(ArrayList<Clients> clients, int srcID)
				{
					boolean clientAlreadyOnServer = false;
					for(int i = 0; i < clients.size(); i ++)
					{
						Clients temp = clients.get(i);
						if(temp.getsrcID() == srcID) clientAlreadyOnServer = true;
					}
					return clientAlreadyOnServer;
				}
			 
			 public static boolean clientAlreadyOnList(ArrayList<Clients> clients, int port, int sourceID)
				{
					boolean clientAlreadyOnServer = false;

					for(int i = 0; i < clients.size(); i++)
					{
						Clients temp = clients.get(i);
						if(port == temp.getPort() && sourceID == temp.getsrcID()) 
						{
							clientAlreadyOnServer = true;
						}
					}
					return clientAlreadyOnServer;
				}
			 
			public static void recordClient(ArrayList<Clients> clients, int sequenceNo, int srcID)
			{
				if(Clients.clientAlreadyOnList(clients, srcID) != true)
				{
					Clients client = new Clients (sequenceNo, srcID);
					clients.add(client);
				}
				else
				{
					if(Clients.clientAlreadyOnList(clients, srcID))
					{
						Clients client = Clients.getClient(clients, srcID);
						
						if(sequenceNo == (client.getSequenceNo() + 1))
						{
							client.setSequenceNo(sequenceNo);
						}
					}
				}
			}
			
			public static Clients getClientAtPort(ArrayList<Clients> clients, int port)
			{
				Clients client = null;
				for(int i = 0; i < clients.size(); i++)
				{
					Clients temp = clients.get(i);
					if(port == temp.getPort()) client = temp;
				}
				return client;
			}

		public int getsrcID()
		{
			return srcID;
		}
		public int getSequenceNo()
		{
			return sequenceNo;
		}

		public void setSequenceNo(int sequenceNo)
		{
			this.sequenceNo = sequenceNo;
		}

		public int getdstID()
		{
			return dstID;
		}

		public int getPort()
		{
			return port;
		}
		
		
	}


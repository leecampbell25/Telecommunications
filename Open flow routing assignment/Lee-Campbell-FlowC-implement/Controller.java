
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

import tcdIO.Terminal;

public class Controller extends Node
{

	InetSocketAddress routerAddress;
	DatagramPacket previousPacket;
	DatagramSocket socket;
	DatagramSocket controllerPort;
	int destinationPort;
		
	int inPort;
	int srcID;
	int outPort;
	int srcPort;
	int destinationClientPort;
	int portSet1[] = new int[8];
	int portSet2[] = new int[8];
	int clientSet[] = new int[2];
	int clientSetRouters[] = new int[2];
	int count;
	int count2;
	int countClient;
	boolean directionOne = false;
	boolean bool = false;
	boolean directionTwo = false;
	boolean finished = false; 
	Terminal terminal;
	
	HashMap<String, Routers> map = new HashMap<String, Routers>();
	DatagramSocket port[];
	DatagramPacket storedPacket;

	
	private class Routers
	{
		int router1[] = new int[2];
		int router2[] = new int[2];
		int router3[] = new int[2];
		
		public Routers(int router1[], int router2[], int router3[]) 
		{
			this.router1 = router1;
			this.router2 = router2;
			this.router3 = router3;
		}
		
	}


	Controller(String name, int localPort) 
	{
		try 
		{
			terminal= new Terminal(name);
			this.controllerPort = new DatagramSocket(Constant.CONTROLLER_PORT);
			new Listener(controllerPort).start();
			terminal.println("Controller Port No : " + localPort + "\n");
			terminal.println("Waiting for contact...");
			
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
	
	
	public synchronized void onReceipt(DatagramPacket packet) 
	{
		try
		{
			byte[] header1 = Header.get(packet);
			int portType = Header.deConstruct(header1, Constant.SRC_INDEX);
			int routerPort = Header.deConstruct(header1, Constant.NEXT_RECIEVE_PORT_INDEX);
			
			
		if (portType == Constant.ROUTER_TYPE_PORT_1 || portType == Constant.ROUTER_TYPE_PORT_2 || portType == Constant.CLIENT_TYPE_PORT)
		{
		
		//if packet is from a Client add that to the array of Clients
		if (portType == Constant.CLIENT_TYPE_PORT)
		{
			int adr = packet.getPort();
			clientSet[countClient] = adr;
			clientSetRouters[countClient] = routerPort;
			countClient++;
			terminal.println("Client added");

		}
	
		//if packet is a router going in direction 1, add it's 
		//local port and the receive port of the connected router or device 
		if (portType == Constant.ROUTER_TYPE_PORT_1)
		{
			int outPort = Header.deConstruct(header1, Constant.LOCAL_OUT_INDEX);
			int inPort = Header.deConstruct(header1, Constant.NEXT_RECIEVE_PORT_INDEX);
			
			portSet1[count] = outPort;
			count++;
			portSet1[count] = inPort;
			count++;
			terminal.println("Router added");
			int clientNo = 0; // Client 1
			int directionForward = 1;
			Routers client1ToClient2 = buildPath(clientNo, portSet1, directionForward);
			String dstSrcString = ("" + clientSet[0]+ ", " + clientSet[1]);
			map.put(dstSrcString, client1ToClient2);
			
			
		}
		
		//if packet is a router going in direction 2, add it's 
		//local port and the receive port of the connected router or device 
		if (portType == Constant.ROUTER_TYPE_PORT_2)
		{

			int outPort = Header.deConstruct(header1, Constant.LOCAL_OUT_INDEX);
			int inPort = Header.deConstruct(header1, Constant.NEXT_RECIEVE_PORT_INDEX);
			
			portSet2[count2] = outPort;
			count2++;
			portSet2[count2] = inPort;
			count2++;
			
			int clientNo = 1; // Client 2 
			int directionBackward = -1;
			Routers client2ToClient1 = buildPath(clientNo, portSet2, directionBackward);
			String dstSrcString = ("" + clientSet[1] + ", " + clientSet[0]);
			map.put(dstSrcString, client2ToClient1);

			
		}
		}
		
		else  
		{

			//Get port of router that routing request came from
			int port = packet.getPort();
			int firstDigit = Integer.parseInt(Integer.toString(port).substring(0, 1));
			terminal.println("\nPath request received from Router " + firstDigit);
			
			
			byte[] header = Header.get(packet);
			int sourcePort = Header.deConstruct(header, Constant.SRC_INDEX);
			
		    destinationPort = Header.deConstruct(header, Constant.DST_INDEX);
			String destinationSource = ("" + sourcePort + ", " + destinationPort);
			terminal.println("Client Source Port: " + sourcePort + "\nClient Destination Port: " + destinationPort);
			
			//get routing info for a client at given source port going to a given client destination port
			Routers routingInfo = this.map.get(destinationSource);
			
			int out;
			int in;
			
			//Send routing info for packet to Router 1 
			out = routingInfo.router1[0];
			in = routingInfo.router1[1];
			constructSendPacket(out, in, Constant.ROUTER_1_PORT);
			
			//Send routing info for packet to Router 2
			out = routingInfo.router2[0];
			in = routingInfo.router2[1];
			constructSendPacket(out, in, Constant.ROUTER_2_PORT);
			
			//Send routing info for packet to Router 3
			out = routingInfo.router3[0];
			in = routingInfo.router3[1];
			constructSendPacket(out, in, Constant.ROUTER_3_PORT);
			
			terminal.println("Routing tables forwarded to each router in the path");
			
			}
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
		
	}

	
	public Routers buildPath(int clientNo, int[] routerOutInPorts, int clientDirection)
	{
		
		int router1[] = new int[2];
		int router2[] = new int[2];
		int router3[] = new int[2];
		
		//find router that client 1 is conected to
		
		int clientRouter = clientSetRouters[clientNo];
		int portSet[] =  routerOutInPorts;
		
		int routerOneOutPort;
		int routerOneNextRecievePort = 0;
		
		int routerTwoOutPort;
		int routerTwoNextRecievePort = 0;
		
		int routerThreeOutPort;
		int routerThreeNextRecievePort;
		

		for (int i = 0; i < portSet.length; i++)
		{
			if (clientRouter + clientDirection == portSet[i])
			{
				routerOneOutPort = portSet[i];
				routerOneNextRecievePort = portSet[i+1];
				router1[0] = routerOneOutPort;
				router1[1] = routerOneNextRecievePort;
				i = portSet.length;
				
			}
		}
		
		for (int j = 0; j < portSet.length; j++)
		{
			if ((routerOneNextRecievePort + clientDirection) == portSet[j])
			{
				
				routerTwoOutPort = portSet[j];
				routerTwoNextRecievePort = portSet[j +1];
				router2[0] = routerTwoOutPort;
				router2[1] = routerTwoNextRecievePort;
				j = portSet.length;
				
			}
		}
		
		for (int k = 0; k < clientSetRouters.length; k++)
		{
			if ((routerTwoNextRecievePort + clientDirection) == clientSetRouters[k])
			{
				routerThreeOutPort = clientSetRouters[k];
				routerThreeNextRecievePort = clientSet[k];
				router3[0] = routerThreeOutPort;
				router3[1] = routerThreeNextRecievePort;
				k = clientSetRouters.length;
			}
		}
		
		
		Routers clientToClient;
		
		if (clientNo == 0)
		{
		  clientToClient = new Routers(router1, router2, router3);
		}
		else
		{
		   clientToClient = new Routers(router3, router2, router1);
		}
		
		return clientToClient;
	}
	
	
	public void constructSendPacket(int out, int in, int routerPort) throws IOException
	{
		DatagramPacket packet = null;
		
		byte[] header = new byte[Constant.HEADER_LENGTH];
		outPort = out;
		inPort = in;
		this.srcID = Constant.CONTROLLER_PORT;
		header = Header.construct(header, outPort, srcID, inPort, destinationPort);
		
		byte[] payload = null;
		payload = ("Routing Table").getBytes();
		

		byte[] buffer = new byte[header.length + payload.length];
		System.arraycopy(header, 0, buffer, 0, header.length);
		System.arraycopy(payload, 0, buffer, header.length, payload.length);
		
		
		this.routerAddress = new InetSocketAddress(Constant.DEFAULT_NODE, routerPort);
		packet = new DatagramPacket(buffer, buffer.length, routerAddress);
		controllerPort.send(packet);
		previousPacket = packet;
		
	}

	public synchronized void start() throws Exception {
		while(!finished)
		{
			this.wait();
		}
	}
	
}
		
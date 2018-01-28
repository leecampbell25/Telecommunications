
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

import tcdIO.Terminal;

public class Router extends Node
{
	int inPort;
	int srcID;
	int outPort;
	int srcPort;
	int destinationPort;
	InetSocketAddress routerAddress;
	DatagramSocket controllerPort;
	
	boolean finished = false; 
	boolean sentPacketToControl = false;
	Terminal terminal;
	int finalDSTPort;
	
	HashMap<Integer, portPairing> rules = new HashMap<Integer, portPairing>();
	DatagramSocket port[];
	DatagramPacket storedPacket;

	
	private class portPairing
	{
		int localOutPort;
		int nextReceivePort;
		
		public portPairing(int localOutPort, int nextRecievePort) 
		{
			this.localOutPort = localOutPort;
			this.nextReceivePort = nextRecievePort;
		}
		
	}

	Router(String name, int remotePorts[], int localPort) 
	{
		try 
		{
			terminal= new Terminal(name);
			port = new DatagramSocket[remotePorts.length];
			int socketCount = remotePorts.length;
			
			for(int i=0;i<socketCount;i++) 
			{
				port[i] = new DatagramSocket(localPort+i);
				port[i].connect(new InetSocketAddress("localhost", remotePorts[i]));
				new Listener(port[i]).start();
			} 
			
			
			// Update Controller with Router Direction 1
			int inPort = remotePorts[2];
			int outPort = port[2].getLocalPort();
			int routerPort = 2;
			sendRouterInfo(outPort, inPort, routerPort);
			
			// Update Controller with Router Direction 2
			inPort = port[2].getLocalPort();
			outPort = remotePorts[2];
			routerPort = 4;
			sendRouterInfo(outPort, inPort, routerPort);
			terminal.println("Router Port No : " + localPort + "\n");
			terminal.println("Waiting for contact...");
			
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
	
	
	public synchronized void onReceipt(DatagramPacket packet) 
	{
		try
		{
			
			byte[] header = Header.get(packet);
			int receivedPort = packet.getPort();
			
			// if packet comes form controller 
			if (receivedPort == Constant.CONTROLLER_PORT)
			{
				int out = Header.deConstruct(header, Constant.LOCAL_OUT_INDEX);
				int in = Header.deConstruct(header, Constant.NEXT_RECIEVE_PORT_INDEX);
				int clientDST = Header.deConstruct(header, Constant.CLIENT_DST_INDEX);
				
				//update routing table 
				portPairing ports = new portPairing(out, in);
				rules.put(clientDST, ports);
				terminal.println("Routing Table Updated\n");
				
				if (sentPacketToControl)
				   {
					  packet = storedPacket;
					  this.sentPacketToControl = false; 
					  terminal.println("Message forwarded...");
					  forward(packet, this.finalDSTPort); 
				   }
			}
			
			//if packet comes from client or router 
			else
			{
				
			StringContent message = new StringContent(packet);
			terminal.println("\nReceived Message: '" + message.toString() + "' ");
			byte[] header1 = Header.get(packet);
			this.finalDSTPort = Header.deConstruct(header1, Constant.NEXT_RECIEVE_PORT_INDEX);

				//if destination client port is in the routing table 
				if (rules.containsKey(finalDSTPort))
				{
					terminal.println("Message forwarded...");
					forward(packet, finalDSTPort);
				}
				// else forward packet to controller to receive routing info 
				else 
				{
					// send packet to controller 
					packet.setSocketAddress(new InetSocketAddress(Constant.DEFAULT_NODE, Constant.CONTROLLER_PORT));
					port[0].send(packet);
					storedPacket = packet;
					this.sentPacketToControl = true;
				}
			
			}

		}
		catch(java.lang.Exception e) {e.printStackTrace();}
		
	}


	private void forward (DatagramPacket packet, int finalDSTPort) throws IOException
	{
		
		packet.setSocketAddress(new InetSocketAddress(Constant.DEFAULT_NODE, this.rules.get(finalDSTPort).nextReceivePort));
		int localOutPort = this.rules.get(finalDSTPort).localOutPort;
		
		for (int i = 0; i < this.port.length; i++)
		{
			if (port[i].getLocalPort() == localOutPort)
			{
				port[i].send(packet);
				i = port.length;
			}
		}
	}
	
	
	public void sendRouterInfo(int out, int in, int routerPort) throws IOException
	{
		DatagramPacket packet = null;
		
		byte[] header = new byte[Constant.HEADER_LENGTH];
		outPort = out;
		inPort = in;
		this.srcID = routerPort;
		header = Header.construct(header, outPort, srcID, inPort);

		byte[] payload = null;
		payload = ("Router Info").getBytes();

		byte[] buffer = new byte[header.length + payload.length];
		System.arraycopy(header, 0, buffer, 0, header.length);
		System.arraycopy(payload, 0, buffer, header.length, payload.length);
		
		packet = new DatagramPacket(buffer, buffer.length);
		packet.setSocketAddress(new InetSocketAddress(Constant.DEFAULT_NODE, Constant.CONTROLLER_PORT));
		port[0].send(packet);
		
	}
	
		
}
		
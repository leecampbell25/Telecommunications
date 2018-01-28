
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.io.IOException;
import tcdIO.*;


public class Client extends Node implements Runnable {

	
	boolean finished = false;
	
	Terminal terminal;
	InetSocketAddress routerAddress;
	DatagramPacket previousPacket;
	DatagramSocket socket;
		
	int dstID;
	int sequenceNo;
	int srcPort;
	int destinationClientPort;
	int routerPort;
	int inPort;
	int outPort;
	int destinationPort;
	int portType;

	

	Client(String name, int srcPort, int destinationClientPort, int routerPort) throws SocketException

	{
		try 
		{
			this.srcPort = srcPort;
			this.routerPort = routerPort;
			this.routerAddress = new InetSocketAddress(Constant.DEFAULT_NODE, routerPort);
			this.dstID = destinationClientPort;
			
			this.terminal = new Terminal(name);	
	
			
			terminal.println("Client Port No : " + srcPort + "\n");
			

			socket = new DatagramSocket(srcPort);
			sendClientToController();
			new Listener(socket).start();
			new Thread(this).start();

		}
		
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	

	public synchronized void onReceipt(DatagramPacket packet) 
	{
		//if packet received print out it's Pay-load  
		  StringContent message = new StringContent(packet);
		  terminal.println("\nReceived Message: '" + message.toString() + "' " + "\n\nType Message:");
			
	}
	
	public void run() 
	{
		
	while(finished != true)
		{
			    terminal.println("Waiting for contact...");
				try {
					constructSendPacket();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				terminal.println("Packet sent\n");
	
		}
	}

	public void constructSendPacket() throws IOException
	{
		DatagramPacket packet = null;
		
		byte[] header = new byte[Constant.HEADER_LENGTH];
		header = Header.construct(header, sequenceNo, srcPort, dstID);
		
		byte[] payload = null;
		payload = (terminal.readString("Type Message: ")).getBytes();
		

		byte[] buffer = new byte[header.length + payload.length];
		System.arraycopy(header, 0, buffer, 0, header.length);
		System.arraycopy(payload, 0, buffer, header.length, payload.length);
		
		packet = new DatagramPacket(buffer, buffer.length, routerAddress);
		socket.send(packet);
		previousPacket = packet;
		
	}

	
	public void sendClientToController() throws IOException
	{
		DatagramPacket packet = null;
		
		byte[] header = new byte[Constant.HEADER_LENGTH];
		portType = Constant.CLIENT_TYPE_PORT;
		
		header = Header.construct(header, srcPort, portType, routerPort, destinationPort);
		
		byte[] payload = null;
		payload = ("Client Info").getBytes();
		

		byte[] buffer = new byte[header.length + payload.length];
		System.arraycopy(header, 0, buffer, 0, header.length);
		System.arraycopy(payload, 0, buffer, header.length, payload.length);
		
		packet = new DatagramPacket(buffer, buffer.length);
		packet.setSocketAddress(new InetSocketAddress(Constant.DEFAULT_NODE, Constant.CONTROLLER_PORT));
		socket.send(packet);
		
	}

}
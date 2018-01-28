import java.net.SocketException;

public class Setup {

	public static void main(String[] args) throws Exception 
	{
		
		int remotePorts1[] = {Constant.CONTROLLER_PORT, Constant.CLIENT_PORT_1, Constant.ROUTER_2_PORT + 1};
		int remotePorts2[] = {Constant.CONTROLLER_PORT, Constant.ROUTER_1_PORT + 2, Constant.ROUTER_3_PORT + 1}; 
		int remotePorts3[] = {Constant.CONTROLLER_PORT, Constant.ROUTER_2_PORT + 2, Constant.CLIENT_PORT_2};
		 
		//Initialise the Controller 
		Controller controller = new Controller("Controller", Constant.CONTROLLER_PORT);
		
		//Initialise Clients 
		Client client1 = new Client("Client 1", Constant.CLIENT_PORT_1, Constant.CLIENT_PORT_2, Constant.ROUTER_1_PORT + 1);
		Client client2 = new Client("Client 2", Constant.CLIENT_PORT_2, Constant.CLIENT_PORT_1, Constant.ROUTER_3_PORT + 2);
		
		//Initialise Routers
		Router router1 = new Router("Router 1", remotePorts1, Constant.ROUTER_1_PORT);
		Router router2 = new Router("Router 2", remotePorts2, Constant.ROUTER_2_PORT);
		Router router3 = new Router("Router 3", remotePorts3, Constant.ROUTER_3_PORT);
		



	}

}

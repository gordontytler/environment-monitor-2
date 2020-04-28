package monitor.implementation.session;

import monitor.model.Server;

public class ServerSessionPoolFactory {

	ServerSessionPool makeServerSessionPool(Server server) {
		return new ServerSessionPool(server);
	}
	
}

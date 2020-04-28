package monitor.implementation.session;

import java.util.HashMap;

import monitor.model.Server;

public class MockServerSessionPoolFactory extends ServerSessionPoolFactory {

	private HashMap<Server, ServerSessionPool> mockServerSessionPools = new HashMap<Server, ServerSessionPool>();
	
	@Override
	ServerSessionPool makeServerSessionPool(Server server) {
		ServerSessionPool serverSessionPool = mockServerSessionPools.get(server);
		if (serverSessionPool == null) {
			serverSessionPool = new ServerSessionPoolFactory().makeServerSessionPool(server);
			System.out.println("using real ServerSessionPool for " + server);
		} else {
			System.out.println("using MockServerSessionPool for " + server);
		}
		return serverSessionPool;
	}

	public void addMockServerSessionPool(Server server, ServerSessionPool mockServerSessionPool) {
		mockServerSessionPools.put(server, mockServerSessionPool);
	}
	
	

}

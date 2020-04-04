package monitor.implementation.session;

import monitor.model.Server;

public class MockServerSessionPool extends ServerSessionPool {

	public MockServerSessionPool(Server server) {
		super(server);
	}

	public void addSession(Session session) {
		sessions.add(session);
	}

}

package monitor.implementation.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.TimeUnit;

import monitor.model.Configuration;
import monitor.model.Server;

import org.junit.After;
import org.junit.Test;

public class ServerSessionPoolTest {

	static String host = Configuration.getInstance().getDefaultHostForTests();
	static Server server = new Server(host);
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	
	@After
	public void tearDown() throws Exception {
		allSessionPools.closeAllFinishedSessionsOnAllServers();
		int maxSessions = Configuration.getInstance().getMaximumServerSessions(); 
		allSessionPools.getServerSessionPool(server).setMaxSessions(maxSessions);
	}
	
	@Test
	public void testGetOneSession() {
		ServerSessionPool ssp = allSessionPools.getServerSessionPool(server);
		Session session = ssp.getSession("testGetOneSession");
		session.logout("testGetOneSession");
	}

	@Test
	public void testGetSession() throws Exception {
		System.out.println("\n\n----------------------------------------------------------");
		System.out.println("Start of ServerSessionPoolTest.testGetSession");
		allSessionPools.logoutAllSessionsOnAllServers("testGetSession");
		
		ServerSessionPool ssp = allSessionPools.getServerSessionPool(server);
		Session session = ssp.getSession("testGetSession");
		session.logout("testGetSession");
		List<Session> sessions = ssp.getSessions();
 
		assertEquals(0, sessions.size());
		
		int maxSessions = 2;
		ssp.setMaxSessions(maxSessions);
		
		for (int i = 0; i < maxSessions; i++) {
			ssp.getSession("testGetSession");
		}
		assertEquals(maxSessions, ssp.getSessions().size());
		try {
			ssp.getSession("testGetSession");
			fail("expected too many sessions exception");
		} catch (Exception e) {
		}
		allSessionPools.dumpAllSessionsOnAllServers("after too many sessions exception");		
		long timeOut = 1000;
		boolean wasClosed = ssp.getSessions().get(maxSessions - 1).close("testGetSession").await(timeOut, TimeUnit.MILLISECONDS);
		allSessionPools.dumpAllSessionsOnAllServers("after closing one");
		assertTrue("session was not closed after " + timeOut + "ms.", wasClosed);
		ssp.getSession("testGetSession");
		System.out.println("End of ServerSessionPoolTest.testGetSession");
		System.out.println("--------------------------------------------------------\n\n");
	}

	
}

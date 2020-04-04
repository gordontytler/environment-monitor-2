package monitor.implementation.environment;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import junit.framework.Assert;
import monitor.dao.EnvironmentViewDAO;
import monitor.dao.MockEnvironmentViewDAO;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.MockServerSessionPool;
import monitor.implementation.session.MockServerSessionPoolFactory;
import monitor.implementation.session.MockSession;
import monitor.implementation.session.ServerSessionPool;
import monitor.implementation.session.ServerSessionPoolFactory;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import monitor.model.Server;
import monitor.model.SessionType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EnvironmentViewBuilderTest {

	static Logger logger = Logger.getLogger(EnvironmentViewBuilderTest.class.getName());
	
	@Before @After
	public void resetAllServerSessionPools() {
		EnvironmentViewDAO.getInstance().resetCache();
		logger.info("logoutAllSessionsOnAllServers");
		AllSessionPools.getInstance().logoutAllSessionsOnAllServers("EnvironmentViewBuilderTest");
		AllSessionPools.getInstance().setAllServerSessionPools(new ConcurrentHashMap<String, ServerSessionPool>());
		AllSessionPools.getInstance().setServerSessionPoolFactory(new ServerSessionPoolFactory());
	}

	@Test
	public void testInsertAdhocJobs() throws Exception {
		String serverName = Configuration.getInstance().getDefaultHostForTests();
		Server thisServer = new Server(serverName);
		
		// create a fake running session
		MockSession runningSession = new MockSession();
		runningSession.setRunningCommand("Test command from testInsertAdhocJobs");
		runningSession.setSessionType(SessionType.TERMINAL);
		runningSession.setLastCommandStatus(CommandStatus.RUNNING);
		
		// add to a fake session pool
		MockServerSessionPool mockServerSessionPool = new MockServerSessionPool(thisServer);
		mockServerSessionPool.addSession(runningSession);
		
		// set a factory to return our fake pool with the fake session
		MockServerSessionPoolFactory mockServerSessionPoolFactory = new MockServerSessionPoolFactory();
		mockServerSessionPoolFactory.addMockServerSessionPool(thisServer, mockServerSessionPool);
		AllSessionPools allSessionPools = AllSessionPools.getInstance(); 
		allSessionPools.setServerSessionPoolFactory(mockServerSessionPoolFactory);
		
		// now the builder will get our mockServerSessionPool with the runningSession
		EnvironmentViewBuilder builder = EnvironmentViewBuilder.getInstance("Test environment");
		builder.setServerDAO(new MockEnvironmentViewDAO());
		builder.setRunningApplicationChecker(new MockRunningApplicationChecker());
		builder.setAllSessionPools(allSessionPools);
		EnvironmentView view = builder.getEnvironmentView(Long.MAX_VALUE);
		
		Assert.assertEquals(9, view.getRows().size());
		Assert.assertEquals("Test command from testInsertAdhocJobs", view.getRows().get(7).getOutputName());
	}	
	
	
	@Test
	public void testGetEnvironmentView() {
		EnvironmentViewBuilder builder = EnvironmentViewBuilder.getInstance("Test environment");
		builder.setServerDAO(new MockEnvironmentViewDAO());
		builder.setRunningApplicationChecker(new MockRunningApplicationChecker());
		EnvironmentView view = builder.getEnvironmentView(Long.MAX_VALUE);
		Assert.assertEquals(8, view.getRows().size());
	}

}

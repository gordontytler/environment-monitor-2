package monitor.implementation.application;

import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import monitor.dao.DataDirectory;
import monitor.dao.EnvironmentViewDAO;
import monitor.dao.MockEnvironmentViewDAO;
import monitor.implementation.environment.EnvironmentViewBuilder;
import monitor.implementation.environment.MockRunningApplicationChecker;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.ServerSessionPool;
import monitor.implementation.session.ServerSessionPoolFactory;
import monitor.implementation.session.Session;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import monitor.model.Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApplicationViewBuilderTest {

	static String host = Configuration.getInstance().getDefaultHostForTests();
	static Server server = new Server(host);
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();	

	@Before @After
	public void resetAllServerSessionPools() {
		DataDirectory.setTest(true);
		EnvironmentViewDAO.getInstance().resetCache();
		System.out.println("resetAllServerSessionPools");
		AllSessionPools.getInstance().setAllServerSessionPools(new ConcurrentHashMap<String, ServerSessionPool>());
		AllSessionPools.getInstance().setServerSessionPoolFactory(new ServerSessionPoolFactory());
	}	
	
	/**
	 * In environment-monitor/data/applications/test-application-with-long-name-001.txt
	 * the name of the application is applicationName = test application
	 * 
	 * But, in environment-monitor/data/environments/test-environment.txt
	 * the name is "Test Application with long name"
	 * 
	 * This tests that we get the nameInEnvironmentView and not the application name when the
	 * python script checks what is running.
	 */
	@Test
	public void testGetEnvironmentApplications() {
		EnvironmentViewBuilder builder = EnvironmentViewBuilder.getInstance("Test environment");
		builder.setServerDAO(new MockEnvironmentViewDAO());
		builder.setRunningApplicationChecker(new MockRunningApplicationChecker());
		EnvironmentView view = builder.getEnvironmentView(Long.MAX_VALUE);
		Assert.assertEquals("Test Application", view.getRows().get(2).getApplicationName());
		Assert.assertEquals("Test Application", view.getRows().get(2).getApplication().getNameInEnvironmentView());
		
		ServerSessionPool ssp = allSessionPools.getServerSessionPool(server);
		// if you get connection refused try installing openssh-server. On Ubuntu...
		//
		//   sudo apt update
		//   sudo apt install openssh-server
		//   sudo systemctl status ssh
		//
		// and generate your public/private rsa key pair.
		//
		//   ssh-keygen -t rsa
		//
		Session session = ssp.getSession("testGetEnvironmentApplications");
		session.setEnvironmentName("Test environment");
		
		ApplicationViewBuilder applicationViewBuilder = new ApplicationViewBuilder();
		String apps = applicationViewBuilder.getEnvironmentApplications(session.getSessionId());
		
		// It finds the environment from the sessionId and lists all applications on the server.
		
		Assert.assertEquals("sessionID............: " + session.getSessionId() + "\n" +
							"nameInEnvironmentView: Test Application\n" +
							"fileName.............: applications/test-application-with-long-name-001.txt\n" +
							"ps -ef | grep java...: test-application\n" +
							"and stdout '1' from..: if [ -d /var/log/test-application ] ; then echo \"1\" ; else  echo \"0\"; fi\n" +
							"and stdout '1' from..: echo 1\n" + 
							"<\n" +
							"sessionID............: " + session.getSessionId() + "\n" +
							"nameInEnvironmentView: Environment monitor\n" +
							"fileName.............: applications/environment-monitor-001.txt\n" +
							"ps -ef | grep java...: monitor.api.Main"
							, apps);
		allSessionPools.logoutAllSessionsOnAllServers("testGetEnvironmentApplications");
		
	}	
	
}

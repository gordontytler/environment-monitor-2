package monitor.dao;

import monitor.model.Action;
import monitor.model.Application;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ActionDAOTest {

	private static String SERVER_NAME;
	
	@BeforeClass
	public static void setUp() {
		DataDirectory.setTest(true);
		SERVER_NAME = Configuration.getInstance().getDefaultHostForTests();
		MockEnvironmentViewDAO mockDAO = new MockEnvironmentViewDAO();
		
		EnvironmentView environmentView = mockDAO.loadEnvironmentView(EnvironmentViewDAOTest.TEST_ENVIRONMENT_NAME);
		EnvironmentViewDAO fileDAO = EnvironmentViewDAO.getInstance();
		fileDAO.resetCache();
		fileDAO.saveEnvironmentView(environmentView);
	}
	
	
	@Test
	public void loadActionTest() {
		ActionDAO actionDAO = new ActionDAO();
		
		// This is in data-test/environments/test-environment.txt
		//your-machine,	Test Application with long name,	applications/test-application-with-long-name-001.txt,	Python Action
		
		Application application = new Application("Test Application", MockEnvironmentViewDAO.TEST_APPLICATION_FILENAME);
		EnvironmentViewRow environmentViewRow = new EnvironmentViewRow(SERVER_NAME, application, "Python Action");
		
		Action actual = actionDAO.loadAction(EnvironmentViewDAOTest.TEST_ENVIRONMENT_NAME, environmentViewRow);
		Action expected = new Action("Python Action", MockApplicationDAO.TEST_SCRIPT_FILE, application.getNameInEnvironmentView());
		
		Assert.assertEquals(expected.toString(), actual.toString());
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(MockApplicationDAO.TEST_SCRIPT_FILE, actual.getCommandLine());
	}

	
	@Test
	public void loadServerActionTest() {
		ActionDAO actionDAO = new ActionDAO();

		EnvironmentViewRow environmentViewRow = new EnvironmentViewRow(SERVER_NAME, null, "Discover Apps");
		
		Action actual = actionDAO.loadAction(EnvironmentViewDAOTest.TEST_ENVIRONMENT_NAME, environmentViewRow);
		Action expected = new Action("Discover Apps", MockEnvironmentViewDAO.TEST_DISCOVER_APPS_SCRIPT, null);
		
		Assert.assertEquals(expected.toString(), actual.toString());
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(MockEnvironmentViewDAO.TEST_DISCOVER_APPS_SCRIPT, actual.getCommandLine());
	}
	
	
}

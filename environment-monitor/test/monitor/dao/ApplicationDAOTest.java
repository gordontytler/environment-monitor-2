package monitor.dao;


import monitor.model.Action;
import monitor.model.Application;

import org.junit.Assert;
import org.junit.Test;


public class ApplicationDAOTest {

	@Test
	public void saveAndLoadApplication() {
		
		DataDirectory.setTest(true);
		
		String applicationFile = MockEnvironmentViewDAO.TEST_APPLICATION_FILENAME;
		String nameInEnvironmentView = MockEnvironmentViewDAO.TEST_APPLICATION_NAME;
		
		MockApplicationDAO mockDAO = new MockApplicationDAO();
		ApplicationDAO fileDAO = new ApplicationDAO();
		
		Application expected = mockDAO.loadApplicationByFileName(applicationFile, nameInEnvironmentView);
		Application actual = fileDAO.loadApplicationByFileName(applicationFile, nameInEnvironmentView);
		
		Assert.assertEquals(expected.toString(), actual.toString());
		Assert.assertEquals(expected.getName(), actual.getName());
		StringBuilder message = new StringBuilder();
		for (Action expectedAction : expected.getActions()) {
			Action actualAction = actual.getActionByOutputName(expectedAction.getOutputName());
			if (!expectedAction.equals(actualAction)) {
				message.append("Did not find expected output: " + expectedAction.getOutputName() + "\n");
			}
		}
		for (Action actualAction : actual.getActions()) {
			Action expectedAction = expected.getActionByOutputName(actualAction.getOutputName());
			if (!actualAction.equals(expectedAction)) {
				message.append("Found output that was not expected: " + actualAction.getOutputName() + "\n");
			}
			if (actualAction.getOutputName().equals("Direct Action") || actualAction.getOutputName().equals("Python Action")) {
				Assert.assertTrue("expected \"" + actualAction.getOutputName() + "\" to be selectedByDefault", actualAction.isSelectedByDefault());
			} else {
				Assert.assertFalse("did not expect \"" + actualAction.getOutputName() + "\" to be selectedByDefault", actualAction.isSelectedByDefault());				
			}
			if (actualAction.getOutputName().equals("start") || actualAction.getOutputName().equals("stop")) {
				Assert.assertTrue("expected \"" + actualAction.getOutputName() + "\" to be a command", actualAction.isCommand());
			} else {
				Assert.assertFalse("did not expected \"" + actualAction.getOutputName() + "\" to be a command", actualAction.isCommand());				
			}			
		}		
		
		if (message.length() > 0) {
			Assert.fail(message.toString());
		}
		Assert.assertEquals(expected.getActions().size(), actual.getActions().size());			
		Assert.assertEquals(expected, actual);
		
	}
	
}

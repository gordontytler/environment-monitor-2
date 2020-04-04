package monitor.dao;

import java.util.ArrayList;
import java.util.List;

import monitor.model.Action;
import monitor.model.Application;
import monitor.model.Configuration;

public class MockApplicationDAO extends ApplicationDAO {

	public static final String TEST_SCRIPT_FILE = "scripts/test-application-with-long-name-test-action.py";
	public static final String APP_NAME_IN_FILE = "Test Application";

	@Override
	public Application loadApplicationByFileName(String fileName, String nameInEnvironmentView) {
		Application application = new Application(MockEnvironmentViewDAO.TEST_APPLICATION_NAME, MockEnvironmentViewDAO.TEST_APPLICATION_FILENAME);
		application.setName(APP_NAME_IN_FILE);
		
		List<String> discoveryChecks = new ArrayList<String>();
		discoveryChecks.add("test-application");
		discoveryChecks.add("if [ -d /var/log/test-application ] ; then echo \"1\" ; else  echo \"0\"; fi");
		discoveryChecks.add("echo 1");
		application.setDiscoveryChecks(discoveryChecks);
		
		List<Action> actions = new ArrayList<Action>();		
		actions.add(new Action("test error 2", "scripts/this-file-does-not-exist.py", application.getNameInEnvironmentView()));
		actions.add(new Action("request.log", "scripts/request-log.py", application.getNameInEnvironmentView()));
		actions.add(new Action("Direct Action", "sh logger.sh", application.getNameInEnvironmentView()));
		actions.add(new Action("Python Action", TEST_SCRIPT_FILE, application.getNameInEnvironmentView()));
		actions.add(new Action("Direct tail", "ls /var/log/*uth* -ltr  | tail -1 | awk '{print $NF}'  | xargs tail -F", application.getNameInEnvironmentView()));
		actions.add(new Action("log", "ls /var/log/test-application/*server.log* -ltr  | tail -1 | awk '{print $NF}'  | xargs tail -F", application.getNameInEnvironmentView()));
		Action start = new Action("start", "cd " + Configuration.getInstance().getFullPathToInstallDirectory() + "; java -Dlog.dir=/var/log/test-application -classpath ./bin monitor.implementation.shell.LogFileWriterApplication &", application.getNameInEnvironmentView());
		start.setCommand(true);
		actions.add(start);
		application.setActions(actions);
		return application;

	}
	
	
}

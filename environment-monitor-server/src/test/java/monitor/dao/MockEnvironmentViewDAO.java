package monitor.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import monitor.implementation.environment.ExtendedRow;
import monitor.model.Application;
import monitor.model.CommandResult;
import monitor.model.Configuration;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;

public class MockEnvironmentViewDAO extends EnvironmentViewDAO {

	public static final String THIS_TEST_COMPUTER = Configuration.getInstance().getDefaultHostForTests();

	public static final String TEST_APPLICATION_FILENAME = "applications/test-application-with-long-name-001.txt";
	public static final String TEST_APPLICATION_NAME = "Test Application";
	public static final String TEST_DISCOVER_APPS_SCRIPT = "scripts/discoverApps001.py";

	@Override
	public EnvironmentView loadEnvironmentView(String environmentName) {

		//Application application = new Application(TEST_APPLICATION_NAME, "applications/app-file-does-not-exist-000.txt");
		Application applicationV2 = new Application(TEST_APPLICATION_NAME, TEST_APPLICATION_FILENAME);
		Application thisApplication = new Application("Environment monitor", "applications/environment-monitor-001.txt");

		EnvironmentView view = new EnvironmentView(environmentName);
		view.setFileName(EnvironmentViewDAO.ENVIRONMENTS + FileDAOHelper.toFileName(environmentName) + ".txt");
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("discover-apps", TEST_DISCOVER_APPS_SCRIPT);
		view.setProperties(properties);
		List<EnvironmentViewRow> rows = new ArrayList<EnvironmentViewRow>();
		rows.add(new ExtendedRow(THIS_TEST_COMPUTER, new Application("", ""), ""));
		//rows.add(new ExtendedRow(THIS_TEST_COMPUTER, application, "test error 1"));
		rows.add(new ExtendedRow(THIS_TEST_COMPUTER, applicationV2, "test error 2"));
		rows.add(new ExtendedRow(THIS_TEST_COMPUTER, applicationV2, "request.log"));		
		rows.add(new ExtendedRow(THIS_TEST_COMPUTER, applicationV2, "Direct Action"));
		rows.add(new ExtendedRow(THIS_TEST_COMPUTER, applicationV2, "Python Action"));
		rows.add(new ExtendedRow(THIS_TEST_COMPUTER, applicationV2, "Direct tail"));
		rows.add(new ExtendedRow(THIS_TEST_COMPUTER, thisApplication, "log"));

		rows.add(new ExtendedRow("some-other-machine", new Application("", ""), ""));
		view.setRows(rows);
		return view;
	}

	@Override
	public CommandResult addApplication(String environmentName, String serverName, String nameInEnvironmentView, String fileName, String outputName) {
		return null;
	}

	@Override
	public void resetCache() {
	}

	@Override
	public void saveEnvironmentView(EnvironmentView environmentView) {
	}

	@Override
	public CommandResult addServer(String environmentName, String serverName) {
		return null;
	}

	@Override
	public CommandResult deleteRow(String environmentName, int index) {
		return null;
	}

	@Override
	public List<String> getCachedNames() {
		return null;
	}

	@Override
	public void deleteEnvironmentView(String environmentName) {
	}

	@Override
	public void saveEnvironmentView(String environmentName) {
	}

	@Override
	public void renameEnvironmentView(String oldName, String newName) {
	}

	@Override
	public void removeFromCache(String environmentName) {
	}

}

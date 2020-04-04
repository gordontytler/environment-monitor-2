package monitor.implementation.environment;

import monitor.model.EnvironmentView;

public class MockRunningApplicationChecker extends RunningApplicationChecker {

	@Override
	public synchronized void checkIfApplicationsAreRunning(EnvironmentView view) {
		return;
	}

	@Override
	public String finishedHeartbeat(String environmentName, String host) {
		return "resultFrom-MockRunningApplicationChecker";
	}

}

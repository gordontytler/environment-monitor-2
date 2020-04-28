package monitor.implementation.shell;

import monitor.implementation.session.PseudoTerminal;
import monitor.model.Server;
import monitor.model.User;

@SuppressWarnings("deprecation")
public class MockCommandExecuterFactory extends CommandExecuterFactory {

	@Override
	public BashExecuter createBashExecuter(Server server, String sessionId,	User user) {
		return null;
	}

	@Override
	public SSHExecuter createSSHExecuter(Server server, PseudoTerminal terminal, int testCommandTimeoutMillis) {
		return null;
	}

}

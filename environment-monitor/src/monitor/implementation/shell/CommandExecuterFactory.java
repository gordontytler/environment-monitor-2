package monitor.implementation.shell;

import monitor.implementation.session.PseudoTerminal;
import monitor.model.Server;
import monitor.model.User;

@SuppressWarnings("deprecation")
public class CommandExecuterFactory {

	public SSHExecuter createSSHExecuter(Server server, PseudoTerminal terminal, int testCommandTimeoutMillis) {
		return new SSHExecuter(server, terminal, testCommandTimeoutMillis);
	}
	public BashExecuter createBashExecuter(Server server, String sessionId, User user) {
		return new BashExecuter(server, sessionId, "ssh", "-v", user.getName() + '@' + server.getHost());
	}
	
}

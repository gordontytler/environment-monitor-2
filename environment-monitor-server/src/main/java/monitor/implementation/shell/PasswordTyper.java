package monitor.implementation.shell;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Command;

public class PasswordTyper {

	/**
	   Detects that a password is being asked for and types it in.
	   It may not type in the correct password though.
	   
	   
		$ su devops
		Password:

		$ sudo -k whoami
		[sudo] password for gordon:
		root

		$ sudo -k -u devops whoami
		[sudo] password for gordon: 
		devops
	  
	*/
	public boolean typePasswordIfAsked(CommandExecuter commandExecuter, int len, byte[] buff, Command command) {
		String question = new String(buff, 0, len);
		if (question.contains("assword")) {
			String password = commandExecuter.getLoggedOnUserPassword();
			if (password == null) {
				throw new MonitorRuntimeException(String.format("Don't know password for user %s on %s", commandExecuter.getLoggedOnUserName(), commandExecuter.getHostName()));
			}
			commandExecuter.writeToShell(password + '\n');
			return true;
		}
		return false;
	}
}

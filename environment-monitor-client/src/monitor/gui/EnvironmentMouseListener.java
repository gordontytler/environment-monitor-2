package monitor.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import monitor.ClientConfiguration;
import monitor.gui.EnvironmentScrollPane.EnvironmentTable;
import monitor.gui.EnvironmentScrollPane.EnvironmentTableModel;
import monitor.gui.EnvironmentScrollPane.RefreshEnvironmentViewWorker;
import monitor.soap.client.MonitorServiceProxy;
import monitorservice.CommandResult;
import monitorservice.CommandStatus;
import monitorservice.EnvironmentViewRow;
import monitorservice.LogonResult;

public class EnvironmentMouseListener implements ActionListener {

	static Logger logger = Logger.getLogger(EnvironmentMouseListener.class.getName());

	private String environmentName;
	private EnvironmentTableModel environmentDataModel;
	private EnvironmentScrollPane environmentScrollPane;
	private CloseableTabbedPane environmentTabbedPane;
	private RefreshEnvironmentViewWorker refreshEnvironmentViewWorker;
	private EnvironmentTable table;	
	
	private ExecutorService restartOutputsExecutor = Executors.newSingleThreadExecutor();
	private ServerNameParser serverNameParser = new ServerNameParser();	
	
	
	public EnvironmentMouseListener(String environmentName, EnvironmentTableModel environmentDataModel,
			EnvironmentScrollPane environmentScrollPane, CloseableTabbedPane environmentTabbedPane,
			RefreshEnvironmentViewWorker refreshEnvironmentViewWorker, EnvironmentTable table) {
		super();
		this.environmentName = environmentName;
		this.environmentDataModel = environmentDataModel;
		this.environmentScrollPane = environmentScrollPane;
		this.environmentTabbedPane = environmentTabbedPane;
		this.refreshEnvironmentViewWorker = refreshEnvironmentViewWorker;
		this.table = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		logger.info("Action command: " + e.getActionCommand());
		if (EnvironmentMouseAdapter.DOUBLE_CLICK.equals(e.getActionCommand()) ||
			"View Output".equals(e.getActionCommand()) ) {
			openOutputStreamFrame(true);
		} else if (EnvironmentMouseAdapter.OPEN_TERMINAL.equals(e.getActionCommand())) {
			openOutputStreamFrame(false);
		} else if (EnvironmentMouseAdapter.DISCOVER_APPS.equals(e.getActionCommand())) {
			discoverApplications();
		} else if (EnvironmentMouseAdapter.DISCOVER_ALL_APPS.equals(e.getActionCommand())) {
			discoverAllApplications();			
 		} else if (EnvironmentMouseAdapter.ADD_SERVER.equals(e.getActionCommand())) {
			addServer();
		} else if (EnvironmentMouseAdapter.DELETE_ROW.equals(e.getActionCommand())) {
			deleteRow();
		} else if (EnvironmentMouseAdapter.RENAME.equals(e.getActionCommand())) {
			renameEnvironment();
		} else if (EnvironmentMouseAdapter.SAVE.equals(e.getActionCommand())) {
			saveEnvironment();
		} else if (EnvironmentMouseAdapter.RESTART_OUTPUTS.equals(e.getActionCommand())) {
			restartOutputs();
		} else if (EnvironmentMouseAdapter.DELETE.equals(e.getActionCommand())) {
			deleteEnvironment();
		} else if (e.getActionCommand().startsWith("Menu=application")) {
			applicationMenuAction(e.getActionCommand());
		}  else if (e.getActionCommand().startsWith("Menu=environment")) {
			environmentMenuAction(e.getActionCommand());
		}
		
	}

	public void restartOutputs() {
		logger.info("About to start RestartOutputsWorker for " + environmentName);
		restartOutputsExecutor.execute(new RestartOutputsWorker());
	}
	
	
	public void saveEnvironment() {
		CommandResult result = MonitorServiceProxy.getInstance().saveEnvironment(environmentName);
		if (CommandStatus.ERROR == result.getCommandStatus()) {
			JOptionPane.showMessageDialog(environmentScrollPane, result.getOutput(), "Save Environment Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	public void deleteEnvironment() {
		int option = JOptionPane.showOptionDialog(environmentScrollPane, "Are you sure you want to delete this view of '" + environmentName + "' ?",
				"Environment Monitor", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE , null, null, null);

		if (option == JOptionPane.YES_OPTION) {
			CommandResult result = MonitorServiceProxy.getInstance().deleteEnvironment(environmentName);
			if (CommandStatus.ERROR == result.getCommandStatus()) {
				JOptionPane.showMessageDialog(environmentScrollPane, result.getOutput(), "Delete Environment Error", JOptionPane.ERROR_MESSAGE);
			}
			int index = environmentTabbedPane.getSelectedIndex();
			environmentTabbedPane.remove(index);
			refreshEnvironmentViewWorker.cancel(true);
		}
	}


	public void openOutputStreamFrame(boolean showExistingStream) {
		int rowIndex = table.getSelectedRow();
		if (rowIndex == -1) {
			logger.info("double click but no selected row");
		} else {
			EnvironmentViewRow row = environmentDataModel.getEnvironmentViewRows().get(rowIndex);
			if (!showExistingStream && row.getOutputName().length() > 0) {
				EnvironmentViewRow copy = new EnvironmentViewRow();
				copy.setServerName(row.getServerName());
				copy.setOutputName("");
				copy.setApplicationName("");
				row = copy;
			}
			new OutputStreamFrame(environmentName, row);
		}
	}

	// Menu=environment&outputName=%s	
	private void environmentMenuAction(String actionCommand) {
		HttpQueryStringParser parser = new HttpQueryStringParser(actionCommand);
		String outputName = parser.getParameterValue("outputName");
		
		if ("Execute...".equals(outputName)) {
        	String command = (String)JOptionPane.showInputDialog(
                    MainFrame.getFrame(),
                    "Enter command to run on all servers.\n\n" + "" +
                    "Or, copy a script to the data/scripts directory on \n" + ClientConfiguration.getInstance().getMonitorURL() + "\n" + 
                    "See comments in existing scripts or the \n" + 
                    "comments for ActionOutputRunnable.makeCommand \n\ne.g.\n\n" + 
                    "  scripts/automationTask.py \"any parameters\"\n\n" +
                    "The URL for REST commands and a comma separated list of\n" +
                    "sessionIDs will be inserted before the \"any parameters\".\n  \n",
                    "Environment monitor",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    "uptime");
        	if (command != null && command.trim().length() > 0) {
        		outputName = outputName + command;
        	} else {
        		return;
        	}
		}
		
		EnvironmentViewRow serverRow = new EnvironmentViewRow();
		// Note: it must begin with "all servers"
		serverRow.setServerName("all servers in environment " + environmentName);
		serverRow.setOutputName(outputName);
		serverRow.setApplicationName("");
		new OutputStreamFrame(environmentName, serverRow);
	}		
	
	
	
	// Menu=application&server=%s&nameInEnvironmentView=%s&outputName=%s&checked=%b&fileName=%s
	private void applicationMenuAction(String actionCommand) {
		HttpQueryStringParser parser = new HttpQueryStringParser(actionCommand);
		String serverName = parser.getParameterValue("server");
		String nameInEnvironmentView = parser.getParameterValue("nameInEnvironmentView");
		String fileName = parser.getParameterValue("fileName");
		String outputName = parser.getParameterValue("outputName");
		String checked = parser.getParameterValue("checked");
		String isCommand = parser.getParameterValue("isCommand");
		
		if (isCommand.equals("true")) {
			EnvironmentViewRow serverRow = new EnvironmentViewRow();
			serverRow.setServerName(serverName);
			serverRow.setOutputName(outputName);
			serverRow.setApplicationName(nameInEnvironmentView);
			new OutputStreamFrame(environmentName, serverRow);
		} else {
			if (checked.equals("true")) {
				LogonResult logonResult = MonitorServiceProxy.getInstance().logon(serverName, serverName, environmentName);
				if (CommandStatus.ERROR == logonResult.getCommandStatus()) {
					JOptionPane.showMessageDialog(null, logonResult.getErrorMessage(), "Error adding output " + outputName, JOptionPane.ERROR_MESSAGE);
				} else {
					MonitorServiceProxy.getInstance().addApplication(logonResult.getSessionId(), nameInEnvironmentView, fileName, outputName);	
				}
			} else {
				List<EnvironmentViewRow> rows = environmentDataModel.getEnvironmentViewRows();
				for (int index=0; index<rows.size(); index++) {
					EnvironmentViewRow row = rows.get(index);
					if (row.getServerName() != null && row.getServerName().equals(serverName) &&
							row.getApplication() != null && row.getApplication().getNameInEnvironmentView().equals(nameInEnvironmentView) &&
							row.getOutputName() != null &&	row.getOutputName().equals(outputName)) {
						MonitorServiceProxy.getInstance().deleteRow(environmentName, index);
						break;
					}
				}
			}
		}
	}


	private final class RestartOutputsWorker extends SwingWorker<Void, Integer> {
		@Override
		protected Void doInBackground() throws Exception {
			logger.info(String.format("\n\nRestarting '%s' environment. Expect some \"histories will be wiped\" errors.\n\n", environmentName));
			CommandResult result = MonitorServiceProxy.getInstance().restartOutputs(environmentName);
			if (CommandStatus.ERROR == result.getCommandStatus()) {
				JOptionPane.showMessageDialog(null, result.getOutput(), "Restart Outputs Error", JOptionPane.ERROR_MESSAGE);
			}
			return null;
		}
	}
	
	
	private void renameEnvironment() {
		String newName = JOptionPane.showInputDialog(null,
				"Enter new name",
				"Rename Environment",
				JOptionPane.QUESTION_MESSAGE);
		if (newName != null && newName.trim().length() > 0) {
			CommandResult result = MonitorServiceProxy.getInstance().renameEnvironment(environmentName, newName);
			if (CommandStatus.ERROR == result.getCommandStatus()) {
				JOptionPane.showMessageDialog(environmentScrollPane, result.getOutput(), "Rename Environment Error", JOptionPane.ERROR_MESSAGE);
			}
			this.environmentName = newName;
			int index = environmentTabbedPane.getSelectedIndex();
			environmentTabbedPane.setTitleAt(index, newName);
		}
	}

	private void deleteRow() {
		int index = table.getSelectedRow();
		deleteRowUsingIndex(index);
	}

	private void deleteRowUsingIndex(int index) {
		CommandResult result = MonitorServiceProxy.getInstance().deleteRow(environmentName, index);
		if (CommandStatus.ERROR == result.getCommandStatus()) {
			JOptionPane.showMessageDialog(environmentScrollPane, result.getOutput(), "Delete Row Error", JOptionPane.ERROR_MESSAGE);
		}
	}	
	
	private void addServer() {
		String serverName = JOptionPane.showInputDialog(null,
			"Enter server host name",
			"Add Server",
			JOptionPane.QUESTION_MESSAGE);
		logger.info("server name:" + serverName);
		List<String> serverNames = serverNameParser.parse(serverName);
		StringBuilder errors = new StringBuilder();
		for (String aServerName : serverNames) {
			CommandResult result = MonitorServiceProxy.getInstance().addServer(environmentName, aServerName);
			if (CommandStatus.ERROR == result.getCommandStatus()) {
				errors.append(result.getOutput() + "\n");
			}
		}
		if (errors.length() > 0) {
			JOptionPane.showMessageDialog(environmentScrollPane, errors.toString(), "Add Server Error", JOptionPane.ERROR_MESSAGE);	
		}
		
	}
	
	private void discoverApplications() {
		int rowIndex = table.getSelectedRow();
		if (rowIndex == -1) {
			logger.info("discover apps but no selected row");
		} else {
			EnvironmentViewRow row = environmentDataModel.getEnvironmentViewRows().get(rowIndex);
			EnvironmentViewRow serverRow = new EnvironmentViewRow();
			serverRow.setServerName(row.getServerName());
			serverRow.setOutputName("Discover Apps");
			serverRow.setApplicationName("");
			new OutputStreamFrame(environmentName, serverRow);
		}
	}	

	private void discoverAllApplications() {
		EnvironmentViewRow serverRow = new EnvironmentViewRow();
		// Note: it must begin with "all servers"
		serverRow.setServerName("all servers in environment " + environmentName);
		serverRow.setOutputName("Discover Apps All");
		serverRow.setApplicationName("");
		new OutputStreamFrame(environmentName, serverRow);
	}
	
}

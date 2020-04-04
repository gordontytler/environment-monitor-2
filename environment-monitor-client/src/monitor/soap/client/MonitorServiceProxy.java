package monitor.soap.client;


import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.xml.namespace.QName;

import monitor.ClientConfiguration;
import monitor.gui.MainFrame;
import monitorservice.Application;
import monitorservice.CommandResult;
import monitorservice.CommandStatus;
import monitorservice.EnvironmentView;
import monitorservice.EnvironmentViewRow;
import monitorservice.LogonResult;
import monitorservice.MonitorService;
import monitorservice.MonitorService_Service;
import monitorservice.OutputChunkResult;

public class MonitorServiceProxy implements MonitorService {

	static Logger logger = Logger.getLogger(MonitorService.class.getName());	
	
	private long timeoutSeconds = 60;	
	static String monitorServiceURL = null;
	static MonitorService_Service service;
	static MonitorService port;	

	public static void setMonitorServiceURL(String url) {
		MonitorServiceProxy.monitorServiceURL = url;
	}
	
	private static MonitorServiceProxy theInstance = new MonitorServiceProxy();
	
	private MonitorServiceProxy() {
	}

	public static MonitorServiceProxy getInstance() {
		return theInstance;
	}

	private void initialiseWebServiceClient()  {
		try {
			Date start = new Date();
			if (monitorServiceURL == null) {
				monitorServiceURL = ClientConfiguration.getInstance().getMonitorURL() + "/?wsdl";
			}
			logger.info("Creating client view of web service at " + monitorServiceURL);			
			service = new MonitorService_Service(new URL(monitorServiceURL), new QName("http://MonitorService", "MonitorService"));
			port = service.getMonitorServicePort();
			logger.info("Got client view of web service  after " + (new Date().getTime() - start.getTime()) + " millis.");			
		} catch (MalformedURLException e) {
			String message = e.toString();
			logger.severe(message);
			showErrorDialogue(message, true);
		}
	}

	private MonitorService getPort() {
		if (port == null) {
			initialiseWebServiceClient();
		}
		return port;
	}
	
	@Override
	public List<String> getEnvironmentNames() {
		List<String> names = new ArrayList<String>();
		try {
			Date start = new Date();
			names = getPort().getEnvironmentNames();
			logger.info("Got " + names.size() + " environment names after " + (new Date().getTime() - start.getTime()) + " millis.");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Problem communicating with the server.", e);
			showErrorDialogue("Problem communicating with the server.\n\n" + e.toString(), true);
		}
		return names;
	}
	
	
	@Override
	public LogonResult logon(String host, String hostName, String environmentName) {
		LogonResult logonResult = null;
		try {
			logonResult = getPort().logon(host, hostName, environmentName);
		} catch (Exception e) {
			logonResult = new LogonResult();
			logonResult.setCommandStatus(CommandStatus.ERROR);
			logonResult.setErrorMessage(getStackTrace("Could not log on.", e));
		}
		return logonResult;
	}
	
	
	@Override
	public CommandResult executeCommand(String command, String sessionId) {
		CommandResult commandResult = null;

		try {
			if (port == null) {
				commandResult = new CommandResult();
				commandResult.setOutput("No connection to " + monitorServiceURL + "\n");
				commandResult.setCommandStatus(CommandStatus.ERROR);
			} else {
				commandResult = port.executeCommand(command, sessionId);	
			}
		} catch (Exception e) {
			commandResult = new CommandResult();
			commandResult.setOutput(getStackTrace(("\n\nClient caught exception processing response from command '" + command + "'.\n"), e));
			commandResult.setCommandStatus(CommandStatus.ERROR);
		}
		return commandResult;
	}

	
	@Override
	public OutputChunkResult getOutputChunk(String sessionId, int chunkNumber) {
		OutputChunkResult outputChunkResult = null;
		try {
			outputChunkResult = getPort().getOutputChunk(sessionId, chunkNumber);
		} catch (Exception e) {
			outputChunkResult = new OutputChunkResult();
			outputChunkResult.setOutput(getStackTrace("Could not get output.", e));
			outputChunkResult.setCommandStatus(CommandStatus.ERROR);
		}
		return outputChunkResult;
	}

	@Override
	public CommandResult killRunningCommand(String sessionId) {
		return getPort().killRunningCommand(sessionId);
	}
	
	ExecutorService threadPoolExecutorService = Executors.newFixedThreadPool(2);
	
	class GetEnvironmentViewCallable implements Callable<EnvironmentView>  {
		private String environmentName;
		private long oldestHistoryTimeStamp;
		
		public GetEnvironmentViewCallable(String environmentName, long oldestHistoryTimeStamp) {
			this.environmentName = environmentName;
			this.oldestHistoryTimeStamp = oldestHistoryTimeStamp;
		}

		@Override
		public EnvironmentView call() {
			return getPort().getEnvironmentView(environmentName, oldestHistoryTimeStamp);
		}
	}
	
	@Override
	public EnvironmentView getEnvironmentView(String environmentName, long oldestHistoryTimeStamp) {
		EnvironmentView view = null;
		String message;

		//logger.info("getting view " + environmentName);
		
		GetEnvironmentViewCallable callable = new GetEnvironmentViewCallable(environmentName, oldestHistoryTimeStamp);
		FutureTask<EnvironmentView> future = new FutureTask<EnvironmentView>(callable); 
		
		threadPoolExecutorService.execute(future);
		try {
			view = future.get(timeoutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			message = "Interupted while waiting for server response. Tab may be closing. Rethrowing exception to halt the calling thread.";
			logger.warning(message);
			throw new RuntimeException(message, e);
		} catch (TimeoutException e) {
			message = "No response from server after " + timeoutSeconds + " seconds.";
			logger.severe(message);
			showErrorDialogue(message, true);
		} catch (Exception e) {
			message = e.toString();
			logger.severe(message);
			showErrorDialogue(message, true);
		}
		//logger.info("got view " + environmentName);
		return view;
	}

	
	private void showErrorDialogue(String message, boolean exit) {
		Frame frame = MainFrame.getFrame();
		JDialog dialog = new JDialog(frame, true);
		dialog.setModal(true);
        dialog.pack();
        StringBuilder sb = new StringBuilder();
        String[] lines = message.split(": ");
        for (String s : lines) {
        	sb.append(s).append(":\n");
        }
        JOptionPane.showMessageDialog(dialog, sb.toString() + (exit ? "\n\nApplication will stop." : ""),"Environment Monitor", JOptionPane.ERROR_MESSAGE );
        if (exit) {
        	System.exit(-1);	
        }
	}

	
	@Override
	public CommandResult executeAction(String environmentName, EnvironmentViewRow action) {
		return getPort().executeAction(environmentName, action);
	}

	@Override
	public CommandResult addApplication(String sessionId, String nameInEnvironmentView, String fileName, String outputName) {
		return getPort().addApplication(sessionId, nameInEnvironmentView, fileName, outputName);
	}

	@Override
	public CommandResult addServer(String environmentName, String serverName) {
		return getPort().addServer(environmentName, serverName);
	}

	@Override
	public CommandResult deleteRow(String environmentName, int index) {
		return getPort().deleteRow(environmentName, index);
	}	
	
	
	private String getStackTrace(String message, Exception e) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(out, true);
		e.printStackTrace(s);

		try {
			out.write(message.getBytes());
		} catch (IOException e2) {
			logger.log(Level.SEVERE, "Problem attempting to display exception.", e2);
		}
		return out.toString();
	}

	@Override
	public void close(String sessionId) {
		getPort().close(sessionId);
	}

	@Override
	public CommandResult deleteEnvironment(String environmentName) {
		return getPort().deleteEnvironment(environmentName);
	}

	@Override
	public CommandResult renameEnvironment(String oldName, String newName) {
		return getPort().renameEnvironment(oldName, newName);
	}

	@Override
	public CommandResult restartOutputs(String environmentName) {
		return getPort().restartOutputs(environmentName);
	}

	@Override
	public CommandResult saveEnvironment(String environmentName) {
		return getPort().saveEnvironment(environmentName);
	}

	@Override
	public Application loadApplicationByFileName(String fileName, String nameInEnvironmentView) {
		return getPort().loadApplicationByFileName(fileName, nameInEnvironmentView);
	}

}

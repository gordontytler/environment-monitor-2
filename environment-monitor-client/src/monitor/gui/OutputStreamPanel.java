package monitor.gui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import monitor.ClientConfiguration;
import monitor.soap.client.AdaptiveSleeper;
import monitor.soap.client.MonitorServiceProxy;
import monitorservice.CommandResult;
import monitorservice.CommandStatus;
import monitorservice.EnvironmentViewRow;
import monitorservice.LogonResult;
import monitorservice.OutputChunkResult;


@SuppressWarnings("serial")
public class OutputStreamPanel  extends JPanel implements ActionListener {

	static Logger logger = Logger.getLogger(OutputStreamPanel.class.getName());
	private static final boolean logFine = ClientConfiguration.getInstance().isLogFine();	
	MonitorServiceProxy monitorServiceProxy = MonitorServiceProxy.getInstance();

	// Fix for the SwingWorker executor corePoolSize = 1 bug introduced with JDK-1.6.0_18
	// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6880336
	// instead of worker.execute   use executer.execute(worker)
	ExecutorService executor = Executors.newCachedThreadPool();
	
	OutputStreamFrame outputStreamFrame;
	String environmentName;
	EnvironmentViewRow environmentViewRow;
    protected JTextField textField;
    protected JTextArea textArea;
    private final static String newline = "\n";
    String sessionId = null;
    String host = null;
    CommandStatus lastCommandStatus = CommandStatus.RUNNING;

    OutputChunkWorker outputChunkWorker = new OutputChunkWorker(OutputChunkWorker.ALL_OUTPUT);
    LogonWorker logonWorker = new LogonWorker();
    ExecuteActionWorker executeActionWorker = new ExecuteActionWorker();
	private boolean paused;
	private StringBuilder outputWhilePaused = new StringBuilder();
	
	OutputChunkResult closedSessionOutputChunkResult = new OutputChunkResult(); 
	{
		closedSessionOutputChunkResult.setOutput("This session is closed.\n"); // Ctrl+C was pressed
		closedSessionOutputChunkResult.setCommandStatus(CommandStatus.FINISHED);
	}

	CommandResult closedSessionCommandResult = new CommandResult();
	{
		closedSessionCommandResult.setOutput("This session is closed.\n");
		closedSessionCommandResult.setCommandStatus(CommandStatus.FINISHED);
	}
	
    
    public OutputStreamPanel(OutputStreamFrame outputStreamFrame, String environmentName, EnvironmentViewRow environmentViewRow)  {
        super(new GridBagLayout());
        this.outputStreamFrame = outputStreamFrame;
        this.environmentName = environmentName;
    	this.environmentViewRow = environmentViewRow;  
		layoutPanel();
		logonOrShowExistingOutput();
    }
    

	private void layoutPanel() {
        textArea = new JTextArea(20, 80);
        textArea.setEditable(false);      
        textArea.setFocusable(false); // so that caret flashes in the textField
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        textField = new JTextField(80) {
        	static final char CTRL_C = 3;
			@Override
			// TODO better to use a button because ctrl+C is also cut 
			protected void processKeyEvent(KeyEvent e) {
				char c = e.getKeyChar();
				if (e.getID() == KeyEvent.KEY_TYPED && c == CTRL_C) {
					e.consume();
					textArea.append("^C\n");
			        javax.swing.SwingUtilities.invokeLater(new Runnable() {
			            public void run() {
							CommandResult commandResult = monitorServiceProxy.killRunningCommand(sessionId);
							sessionId = commandResult.getSessionId();
							textArea.append(commandResult.getOutput());
			            }
			        });
					
				} else {
					super.processKeyEvent(e);	
				}
			}
        };
        textField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textField.addActionListener(this);
        JScrollPane scrollPane = new JScrollPane(textArea);

        //Add textArea inside scrollPane to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollPane, c);
        // change gridBad and add textField
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        
        // Only add the JTextArea when opening a terminalA
        if (environmentViewRow.getOutputName().length() == 0) {
        	add(textField, c);
        } else {
            textArea.setFocusable(true); 
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
	
    private void logonOrShowExistingOutput() {
    	host = environmentViewRow.getServerName();
    	// no output name so open a terminal
    	if (environmentViewRow.getOutputName().length() == 0) {
	        textArea.append("Logging on to " + host +"\n");
	        textArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	        logonWorker.addPropertyChangeListener(new LogonPropertyChangeListener());
	        executor.execute(logonWorker);
    	} else {
    		if (environmentViewRow.getSessionId() != null && environmentViewRow.getSessionId().equals("RESET")) {
    			logger.info("Something went wrong because sessionId was RESET for outputName " + environmentViewRow.getOutputName());
    			environmentViewRow.setSessionId(null);
    		}
    		// if the named output has a session display the output 
    		if (environmentViewRow.getSessionId() != null) {
        		sessionId = environmentViewRow.getSessionId();
        		logger.info("about to outputChunkWorker.execute() for sessionId " + sessionId + " outputName " + environmentViewRow.getOutputName());
    	        executor.execute(outputChunkWorker);        		
    		} else {
    	        textArea.append(String.format("%s on %s\n", environmentViewRow.getOutputName(), environmentViewRow.getServerName()));
    	        textArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	        executeActionWorker.addPropertyChangeListener(new ExecuteActionPropertyChangeListener());
    	        executor.execute(executeActionWorker);    	        
    		}
    	}
	}


	private final class ExecuteActionWorker extends SwingWorker<CommandResult, Void> {
		@Override
		protected CommandResult doInBackground() throws Exception {
			logger.info(Thread.currentThread().toString());				
        	return monitorServiceProxy.executeAction(environmentName, environmentViewRow);
		}
	}

	private final class ExecuteActionPropertyChangeListener implements PropertyChangeListener {
		public  void propertyChange(PropertyChangeEvent evt) {
			if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
		        // wait for action to complete
		        if (sessionId == null) {
		        	try {
		        		CommandResult commandResult = executeActionWorker.get();
		        		logger.info(String.format("%s %s ",environmentViewRow.getOutputName(), commandResult.getCommandStatus().toString()));
		        		if (CommandStatus.ERROR == commandResult.getCommandStatus()) {
		        			textArea.append(commandResult.getOutput());
		        		} else {
			        		sessionId = commandResult.getSessionId();
			        		executor.execute(outputChunkWorker);
		            		//outputChunkWorker.execute();
		        		}
					} catch (Exception e) {
						textArea.append(e.getMessage() + "\n");
						logger.log(Level.SEVERE, "", e);
					}
					textArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		        }
			}
		}
	}	

	
	
    
    //*********************************************************************************************
    // Worker thread to logon and get a sessionId
    //*********************************************************************************************    

	private final class LogonWorker extends SwingWorker<LogonResult, Void> {
		@Override
		protected LogonResult doInBackground() throws Exception {
			logger.info(Thread.currentThread().toString());				
        	return monitorServiceProxy.logon(host, host, environmentName);
		}
	}
	
	private final class LogonPropertyChangeListener implements PropertyChangeListener {
		public  void propertyChange(PropertyChangeEvent evt) {
			if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
		        // wait for logon to complete
		        if (sessionId == null) {
		        	try {
						LogonResult logonResult = logonWorker.get();
			        	if (CommandStatus.FINISHED == logonResult.getCommandStatus()) {
			        		sessionId = logonResult.getSessionId();
			        		executor.execute(outputChunkWorker);
			        	} else {
			        		textArea.append(logonResult.getErrorMessage());
			        	}
			        	
					} catch (Exception e) {
						textArea.append(e.getMessage() + "\n");
						logger.log(Level.SEVERE, "", e);
					}
					textArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		        }
				
			}
		}
	}	

    //*********************************************************************************************
    // Worker thread to get and display continuous output for an existing session
    //*********************************************************************************************    
	
	private final class OutputChunkWorker extends SwingWorker<OutputChunkResult, String> {

		static final int ALL_OUTPUT = 0; 
		private AdaptiveSleeper sleeper = new AdaptiveSleeper(5000, 200, 200);
		private int lastChunkPublished;
		private boolean firstCallSoGettingAll = false;
		
		public OutputChunkWorker(int lastChunk) {
			super();
			lastChunkPublished = lastChunk;
			if (lastChunk == ALL_OUTPUT) {
				firstCallSoGettingAll = true;
			}
		}

		@Override
		protected OutputChunkResult doInBackground() throws Exception {
			OutputChunkResult outputChunkResult = null;
			if (firstCallSoGettingAll) {
				logger.info("firstCallSoGettingAll");
				outputChunkResult = monitorServiceProxy.getOutputChunk(sessionId, ALL_OUTPUT); 	
				//lastChunkPublished = outputChunkResult.getHighestChunk() + 1;
				lastChunkPublished = outputChunkResult.getHighestChunk();
				if (logFine) logger.info(String.format("firstCallSoGettingAll sessionId:%s lastChunkPublished: %d", sessionId, lastChunkPublished));
				publish(outputChunkResult.getOutput());
				firstCallSoGettingAll = false;
			} else {
				if (logFine) logger.info("getting next chunk " + (lastChunkPublished + 1));
				outputChunkResult = getOutputChunk(sessionId, lastChunkPublished + 1);	
			}
			lastCommandStatus = outputChunkResult.getCommandStatus();
			if (logFine) logger.info("lastCommandStatus: " + lastCommandStatus.toString());
			
			while (CommandStatus.RUNNING.equals(lastCommandStatus)) {
				if (outputChunkResult.getLowestChunk() > lastChunkPublished) {
					lastChunkPublished = outputChunkResult.getLowestChunk() - 1; 
				} else {
					if (outputChunkResult.getHighestChunk() > lastChunkPublished) {
						publish(outputChunkResult.getOutput());
						lastChunkPublished++;
						sleeper.decreaseDelay();
					} else {
						sleeper.increaseDelay();
					}
				}				
				sleeper.sleep();
				if (logFine) logger.info("command still running. getting next chunk " + (lastChunkPublished + 1));
				outputChunkResult = getOutputChunk(sessionId, lastChunkPublished + 1);
				lastCommandStatus = outputChunkResult.getCommandStatus();
			}
			String errorOutput = "";
			if (CommandStatus.ERROR == lastCommandStatus) {
				errorOutput = outputChunkResult.getOutput();
			}
			// its finished running now catch up with the output
			logger.info(String.format("%s now catching up. lastChunkPublished: %d HighestChunk: %d", lastCommandStatus.toString(), lastChunkPublished , outputChunkResult.getHighestChunk()));
			boolean morePublishedWhileCatchingUp = false;
			while (outputChunkResult.getHighestChunk() > lastChunkPublished) {
				if (outputChunkResult.getLowestChunk() > lastChunkPublished) {
					lastChunkPublished = outputChunkResult.getLowestChunk() - 1; 
				} else {
					publish(outputChunkResult.getOutput());
					morePublishedWhileCatchingUp = true;
					lastChunkPublished++;
				}
				sleeper.minimumSleep();				
				outputChunkResult = getOutputChunk(sessionId, lastChunkPublished + 1);
				if (logFine) logger.info(String.format("catching up. LowestChunk: %d lastChunkPublished: %d HighestChunk: %d", outputChunkResult.getLowestChunk(), lastChunkPublished , outputChunkResult.getHighestChunk()));
			}
			if (!morePublishedWhileCatchingUp && errorOutput.length() > 0) {
				publish(errorOutput);
			}
			return outputChunkResult;
		}

		private OutputChunkResult getOutputChunk(String sessionId, int chunkNumber) {
			OutputChunkResult result;
			if (sessionId == null) {
				result = closedSessionOutputChunkResult;
			} else {
				result = monitorServiceProxy.getOutputChunk(sessionId, chunkNumber);
			}
			return result;
		}
		
		@Override
		protected void process(List<String> chunks) {
			for (String chunk : chunks) {
				if (paused) {
					outputWhilePaused.append(chunk);
				} else {
					textArea.append(chunk);	
				}
				
			}
		}
	}
	



    //*********************************************************************************************
    // Action listener for commands
    //*********************************************************************************************    
    
    public void actionPerformed(ActionEvent evt) {
        // echo the command
        String text = textField.getText();
        textArea.append("> " + text + newline);
        textField.selectAll();
        
        if ("exit".equals(text) || "logout".equals(text)) {
        	windowClosing(null);
        	outputStreamFrame.dispose();
        	return;
        }
        
        // send command to remote server
        CommandResult commandResult;
        if (sessionId == null) {
        	commandResult = closedSessionCommandResult;
        } else {
        	commandResult = monitorServiceProxy.executeCommand(text, sessionId);
        }
        
        lastCommandStatus = commandResult.getCommandStatus();        
        sessionId = commandResult.getSessionId();
        textArea.append(commandResult.getOutput());
        
        if (CommandStatus.RUNNING.equals(lastCommandStatus)) {
        	if (outputChunkWorker == null || outputChunkWorker.isDone()) {
        		outputChunkWorker = new OutputChunkWorker(commandResult.getChunkNumber());
        		outputChunkWorker.execute();
        	}
        }
        // for selection
        textArea.setFocusable(true); 
        //Make sure the new text is visible, even if there was a selection in the text area.
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }


	public void windowClosing(WindowEvent e) {
		logger.info(String.format("windowClosing \"%s %s %s\"", environmentViewRow.getServerName(), environmentViewRow.getApplicationName(), environmentViewRow.getOutputName()));
		if (!CommandStatus.RUNNING.equals(lastCommandStatus)) {
			// close the ssh session so another window can use it later
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                monitorServiceProxy.close(sessionId);
	            }
	        });
		}
		boolean mayInterruptIfRunning = true;
		outputChunkWorker.cancel(mayInterruptIfRunning);	
		logonWorker.cancel(mayInterruptIfRunning);
		executeActionWorker.cancel(mayInterruptIfRunning);	
	}


	public void togglePauseOutput() {
		if (paused) {
			textArea.append(outputWhilePaused.toString());
			outputWhilePaused = new StringBuilder();
		}
		paused = !paused;
	}

	public String getText() {
		return textArea.getText();
	}

}

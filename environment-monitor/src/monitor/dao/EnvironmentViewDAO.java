package monitor.dao;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import monitor.implementation.MonitorRuntimeException;
import monitor.implementation.environment.ExtendedRow;
import monitor.model.Action;
import monitor.model.Application;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.EnvironmentView;
import monitor.model.EnvironmentViewRow;
import monitor.model.UpDownState;

public class EnvironmentViewDAO  {

	static Logger logger = Logger.getLogger(EnvironmentViewDAO.class.getName());
	private static EnvironmentViewDAO theInstance = new EnvironmentViewDAO();
	private ApplicationCache applicationCache = ApplicationCache.getInstance();
	private ConcurrentHashMap<String, EnvironmentView> cache = new ConcurrentHashMap<String, EnvironmentView>();
	
	protected EnvironmentViewDAO() {
	}
	
	public static EnvironmentViewDAO getInstance() {
		return theInstance;
	}
	
	/** 
	 * There is a nasty bug to avoid here. For example, ActionExecuter calls ActionDAO.loadAction which calls
	 * this method to get the EnvironmentView.properties. Meanwhile, EnvironmentViewBuilder is in the middle of doing its stuff
	 * so finds that one or more of the transient properties get reset.
	 * 
	 * To be safe from modification a deep copy should be returned. The EnvironmentViewBuilder can get the CommandStatus, HighestChunk,
	 * LowestChunk, SessionId, and OutputHistory from the ServerSessionPool.  
	 **/
	public EnvironmentView loadEnvironmentView(String environmentName) {
		EnvironmentView view = loadUnsafeEnvironmentView(environmentName);
		// The cached rows should be immutable but are not so updates done after the load from file need to undone. 
		for (EnvironmentViewRow row : view.getRows()) {
			row.resetNonMetaDataFields();
		}
		return view;
	}

	/** Unsafe because any changes could mess up EnvironmentViewBuilder. loadEnvironmentView is worse because it does a row.resetNonMetaDataFields() */
	public EnvironmentView loadUnsafeEnvironmentView(String environmentName) {
		EnvironmentView view = cache.get(environmentName);
		if (view == null) {
			view = loadAndCacheView(environmentName);
		}
		return view;
	}
	
	public List<String> getServerNames(String environmentName) {
		List<String> names = new ArrayList<String>();
		if (cache.get(environmentName) != null) {
			String lastAdded = "";
			for (EnvironmentViewRow row : cache.get(environmentName).getRows()) {
				String serverName = row.getServerName();
				if (!lastAdded.equals(serverName)) {
					names.add(serverName);
					lastAdded = serverName;
				}
			}
		}
		return names;
	}
	
	private synchronized EnvironmentView loadAndCacheView(String environmentName) {
		EnvironmentView view = cache.get(environmentName);
		if (view != null) {
			return view;
		}
		view = load(environmentName);
		cache.put(environmentName, view);
		return view;
	}	
	
	public synchronized CommandResult addServer(String environmentName, String serverName) {
		EnvironmentView view = cache.get(environmentName);
		ExtendedRow newRow = new ExtendedRow(serverName, null, "");
		view.getRows().add(newRow);
		view.setRowsModified(true);
		return new CommandResult(CommandStatus.FINISHED, "", 0, null);
	}
	
	public synchronized CommandResult deleteRow(String environmentName, int index) {
		EnvironmentView view = cache.get(environmentName);
		view.getRows().remove(index);
		view.setRowsModified(true);
		return new CommandResult(CommandStatus.FINISHED, "", 0, null);
	}
	
	
	public synchronized CommandResult addApplication(String environmentName, String serverName, String nameInEnvironmentView, String fileName, String outputName) {
		EnvironmentView view = cache.get(environmentName);
		List<EnvironmentViewRow> existingRows = view.getRows();
		List<EnvironmentViewRow> newRows = new ArrayList<EnvironmentViewRow>();
		// copy rows until we find the server
		int from = 0;
		while (from < existingRows.size() && !serverName.equals(existingRows.get(from).getServerName())) {
			newRows.add(existingRows.get(from));
			from++;
		}
		if (from == existingRows.size()) {
			return new CommandResult(CommandStatus.ERROR, 
					("ERROR Did not find server " + serverName + " in " + environmentName), 0, null);
		}

		// if we are adding an output to an existing application copy rows until we find the application
		if (outputName != null) {		
			while (from < existingRows.size() && 
				   serverName.equals(existingRows.get(from).getServerName()) && 
				   (existingRows.get(from).getApplication() == null ||
					   !nameInEnvironmentView.equals(existingRows.get(from).getApplication().getNameInEnvironmentView())  )) {
				newRows.add(existingRows.get(from));
				from++;
			}
			if (from == existingRows.size()) {
				return new CommandResult(CommandStatus.ERROR, 
						("ERROR Did not find server " + serverName + " in " + environmentName), 0, null);
			}
		}
		
		// copy existing outputs for this server
		HashMap<String, String> existingOutputs = new HashMap<String, String>(0);
		StringBuilder sb = new StringBuilder();
		Application application = applicationCache.loadApplicationByFileName(fileName, nameInEnvironmentView);
		
		
		if (outputName != null) {
			// a single output needs to be inserted at the right place
			for (Action action : application.getActions()) {
				if (action.getOutputName().equals(outputName)) {
					addRow(serverName, nameInEnvironmentView, fileName, view, newRows, sb, action, outputName);
				} else {
					if (from < existingRows.size() && serverName.equals(existingRows.get(from).getServerName())) {
						String existingOutputName = existingRows.get(from).getOutputName();
						if (existingOutputName.length() == 0) {
							from++;
						} else {
							Action existingAction = application.getActionByOutputName(existingOutputName);
							if (existingAction == null) {
								// the action is missing but copy it anyway
								newRows.add(existingRows.get(from));
								existingOutputs.put(existingRows.get(from).getApplicationName() + existingOutputName, existingOutputName);
								from++;
							} else {
								// the existing row is one of the actions for this application - copy it at the right place
								if (action.getOutputName().equals(existingOutputName)) {
									newRows.add(existingRows.get(from));
									existingOutputs.put(existingRows.get(from).getApplicationName() + existingOutputName, existingOutputName);
									from++;
								}
							}
						}
					}
				}
			}
		}
		while (from < existingRows.size() && serverName.equals(existingRows.get(from).getServerName())) {
			String existingOutputName = existingRows.get(from).getOutputName();
			if (existingOutputName.length() > 0) {
				newRows.add(existingRows.get(from));
				existingOutputs.put(existingRows.get(from).getApplicationName() + existingOutputName, existingOutputName);
			}
			from++;
		}		
		
		// add the new application outputs if we don't already have them and they are selected by default
		if (outputName == null) {
			for (Action action : application.getActions()) {
				String existingOutputName = action.getOutputName();
				if (action.isSelectedByDefault()) {
					if (existingOutputs.get(nameInEnvironmentView + existingOutputName) == null) {
						addRow(serverName, nameInEnvironmentView, fileName, view, newRows, sb, action, existingOutputName);	
					} else {
						sb.append(String.format("'%s' application on %s already has '%s' output.", nameInEnvironmentView, serverName, action.getOutputName()));
					}
				}
				if (application.getActions().size() > 1 && outputName == null) {
					sb.append('\n');
				}
			}
		}

		// copy remaining rows
		while (from < existingRows.size()) {
			newRows.add(existingRows.get(from));
			from++;
		}
		// put them in the cached view
		view.setRows(newRows);
		return new CommandResult(CommandStatus.FINISHED, sb.toString(), 0, null);
	}

	private void addRow(String serverName, String nameInEnvironmentView,
			String fileName, EnvironmentView view,
			List<EnvironmentViewRow> newRows, StringBuilder sb, Action action,
			String existingOutputName) {
		ExtendedRow newRow = new ExtendedRow(serverName, new Application(nameInEnvironmentView, fileName), existingOutputName);
		newRow.setOutputStartupAttempted(false);
		newRows.add(newRow);
		sb.append(String.format("added '%s' output for '%s' application on %s server.", action.getOutputName(), nameInEnvironmentView, serverName));
		view.setRowsModified(true);
	}
	
	public synchronized void changeApplicationUpDownState(String environmentName, String serverName, String nameInEnvironmentView, UpDownState upDownState) {
		EnvironmentView view = cache.get(environmentName);
		for (EnvironmentViewRow row : view.getRows()) {
			if (serverName.equals(row.getServerName()) && nameInEnvironmentView.equals(row.getApplicationName())) {
				row.getApplication().setUpDownState(upDownState);
				logger.fine(String.format("Application '%s' on '%s' in environment '%s' is now %s.", nameInEnvironmentView, serverName, environmentName, upDownState));
			}
		}
	}

	
	private EnvironmentView load(String environmentName) {

		EnvironmentView view = new EnvironmentView(environmentName);		
		List<EnvironmentViewRow> rows = new ArrayList<EnvironmentViewRow>();
		

		try {
			String fileName = getDir() + FileDAOHelper.toFileName(environmentName) + ".txt";
			File file = new File(fileName);
			view.setFileName(fileName);
			boolean fileExists = true;
			BufferedReader reader = getReaderForExistingFile(file);
			if (reader == null) {
				fileName = getDefaults();
				reader = getReaderForDefaultValues(fileName);
				fileExists = false;
			}
			
			String[] parts = null;
			LineNumber lineNumber = new LineNumber();
			String line = null;

			// environment properties
			if (fileExists) {
				line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
				if (line == null) {
					parts = new String[]{};
				} else {
					parts = line.split("=");	
				}
				if (! (parts.length == 2 && parts[0].trim().equals("environmentName") && parts[1].trim().length() > 0) ) {
					throw new MonitorRuntimeException(file + " expected line beginning 'environmentName = ' but found " + line);
				}
				if (!environmentName.equals(parts[1].trim())) {
					logger.warning(String.format("file '%s' line %d. Environment name in file is '%s'. It should be the same as the parameter '%s'.",
							file, lineNumber, parts[1].trim(), environmentName));
				}
			}
			PropertiesAndNextLine propertiesAndNextLine = loadProperties(reader, fileName, lineNumber);
			view.setProperties(propertiesAndNextLine.properties);
			line = propertiesAndNextLine.line;
			// servers
			while (line != null && fileExists) {
				parts = line.split(",");
				if (parts.length > 0) {
					String server = parts[0].trim();
					String nameInEnvironmentView = "";
					String applicationConfigFileName = "";
					String outputName = "";
					if (parts.length > 1) {
						nameInEnvironmentView = parts[1].trim();
					}
					if (parts.length > 2) {
						applicationConfigFileName = parts[2].trim();
					}
					if (parts.length > 3) {
						outputName = parts[3].trim();
					}
					rows.add(new ExtendedRow(server, new Application(nameInEnvironmentView, applicationConfigFileName), outputName));
				} else {
					logger.warning(String.format("file '%s' line %d. Expected line with a server name but was '%s'.",
							file, lineNumber, line));
				}
				line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
			}
		} catch (Exception e) {
			throw new MonitorRuntimeException(e);
		}

		view.setRows(rows);
		return view;
	}

	private BufferedReader getReaderForExistingFile(File file)  {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException fnfe) {
		}
		return reader;
	}	

	private BufferedReader getReaderForDefaultValues(String fileName) throws FileNotFoundException {
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(fileName));
		return reader;
	}	

	private class PropertiesAndNextLine {
		HashMap<String, String> properties = new HashMap<String, String>();
		String line;
	}
	
	private PropertiesAndNextLine loadProperties(BufferedReader reader, String fileName, LineNumber lineNumber) {
		PropertiesAndNextLine result = new PropertiesAndNextLine();
		String[] parts = null;
		result.line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		while (result.line != null && result.line.lastIndexOf("=") > 0) {
			parts = result.line.split("=");
			if (! (parts.length == 2 && parts[0].trim().length() > 0 && parts[1].trim().length() > 0) ) {
				throw new MonitorRuntimeException(fileName + " expected 'key = value' pair but found " + result.line);
			}
			result.properties.put(parts[0].trim(), parts[1].trim());
			result.line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);	
		}
		return result;
	}

	
	
	public synchronized void saveEnvironmentView(String environmentName) {
		EnvironmentView view = cache.get(environmentName);
		if (view != null) {
			saveEnvironmentView(view);
		}
	}
	
	public synchronized void renameEnvironmentView(String oldName, String newName) {
		EnvironmentView oldView = cache.get(oldName);
		if (oldView != null) {
			oldView.setEnvironmentName(newName);
			oldView.setFileName(getDir() + FileDAOHelper.toFileName(newName) + ".txt");
			saveEnvironmentView(oldView);
			deleteEnvironmentView(oldName);
		}
	}

	
	public synchronized void saveEnvironmentView(EnvironmentView environmentView) {
		try {
			// when saving add any missing default properties
			PropertiesAndNextLine defaultProperties = loadProperties(getReaderForDefaultValues(getDefaults()), getDefaults(), new LineNumber());
			for (Entry<String, String> defaultProperty : defaultProperties.properties.entrySet()) {
				if (environmentView.getProperties().get(defaultProperty.getKey()) == null) {
					environmentView.getProperties().put(defaultProperty.getKey(), defaultProperty.getValue());
					logger.info("Added missing property : " + defaultProperty.getKey() + " = " + defaultProperty.getValue());
				}
			}
			
			String fileName = getDir() + FileDAOHelper.toFileName(environmentView.getEnvironmentName()) + ".txt";
			logger.info("Saving " + fileName);
			File file = new File(getDir() + FileDAOHelper.toFileName(environmentView.getEnvironmentName()) + ".txt");
			file.createNewFile(); // it will be overwritten if it already exits

			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			p.println();
			p.println("environmentName = " + environmentView.getEnvironmentName());

			for (Entry<String, String> entry : environmentView.getProperties().entrySet()) {
				p.println(entry.getKey() + " = " + entry.getValue());
			}
			p.println();
			p.println("# serverName\tapplicationName\tapplicationFileName\toutputName");
			p.println("# ==========\t===============\t===================\t==========");
			for (EnvironmentViewRow row : environmentView.getRows()) {
				String applicationFileName;
				if (row.getApplication() == null) {
					applicationFileName = "";
				} else {
					applicationFileName = row.getApplication().getFileName();
				}
				p.format("%s,\t%s,\t%s,\t%s\n", row.getServerName(), row.getApplicationName(), applicationFileName, row.getOutputName() );	
			}
			p.println();			
			p.flush();
			p.close();
		} catch (Exception e) {
			throw new MonitorRuntimeException("Problem saving environment view: " + environmentView.getEnvironmentName(), e);
		}
		// remove from cache to force reload
		cache.remove(environmentView.getEnvironmentName());
	}
	
	public synchronized void resetCache() {
		cache = new ConcurrentHashMap<String, EnvironmentView>();
	}

	public synchronized void removeFromCache(String environmentName) {
		cache.remove(environmentName);
	}
	
	/** @return List of environmentName */
	public List<String> getCachedNames() {
		List<String> names = new ArrayList<String>();
		Enumeration<EnvironmentView> views = cache.elements();
		while (views.hasMoreElements()) {
			names.add(views.nextElement().getEnvironmentName());
		}
		return names;
	}

	public synchronized void deleteEnvironmentView(String environmentName) {
		try {
			File file = new File(getDir() + FileDAOHelper.toFileName(environmentName) + ".txt");
			file.delete();
			EnvironmentNamesDAO.getInstance().resetCache();
		} catch (Exception e) {
			throw new MonitorRuntimeException("Problem deleting environment: " + environmentName, e);
		}
		cache.remove(environmentName);
		if (cache.get(environmentName) != null) {
			logger.severe("Environment still in cache after delete: " + environmentName);
		}
	}

	protected String getDir() {
		return DataDirectory.getDataDirectory() + File.separatorChar + "environments" + File.separatorChar;
	}

	private String getDefaults() {
		return getDir() + "defaults.txt";
	}
	
	// spread over several lines to make easier to debug 
	public String substituteVariables(String unsubstitutedScriptFile, String applicationName) {
		if (applicationName.startsWith("unknown")) {
			return unsubstitutedScriptFile;
		} 
		if (unsubstitutedScriptFile.lastIndexOf("${applicationName}") == -1) {
			return unsubstitutedScriptFile;
		} else {
			String replacement = applicationName;
			String substituted = unsubstitutedScriptFile.replace("${applicationName}", replacement);
			return substituted;
		}
	}
	
}

package monitor.dao;

import monitor.implementation.MonitorRuntimeException;
import monitor.model.Action;
import monitor.model.Application;
import monitor.model.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * Loads an existing application using the file name name stored with the EnvironmentView. 
 * There is no need for a saveApplication method because applications have to be defined by hand.
 */

public class ApplicationDAO {

	static Logger logger = Logger.getLogger(ApplicationDAO.class.getName());	
	
	
	public Application loadApplicationByFileName(String fileName, String nameInEnvironmentView) {
		Application application = null;
		List<Action> actions = new ArrayList<Action>();		

		File file = new File(Configuration.getInstance().getDataDirectory() + fileName);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throwAndLog(e.getMessage());
		}

		LineNumber lineNumber = new LineNumber();
		String line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		
		if (line == null) {
			throwAndLog("not a valid application file:" + fileName);
		}
		
		//=====================================================================
		// Section "Application"
		//=====================================================================
		
		if (!"Section \"Application\"".equals(line.trim())) {
			throwAndLog(file + " expected 'Section \"Application\"' but found '" + line + "' at line " + lineNumber);
		}
		line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		
		String[] parts = line.split("=");
		if (! (parts.length == 2 && parts[0].trim().equals("applicationName") && parts[1].trim().length() > 0) ) {
			throwAndLog(file + " expected line beginning 'applicationName = ' but found '" + line + "' at line " + lineNumber);
		}
		application = new Application(nameInEnvironmentView, fileName); // we don't know the environment view name at this point
		application.setName(parts[1].trim());
		
		line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);

		if (!"SubSection \"Discovery\"".equals(line.trim())) {
			throwAndLog(file + " expected 'SubSection \"Discovery\"' but found '" + line + "' at line " + lineNumber);
		}
		line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		ArrayList<String> discoveryChecks = new ArrayList<String>();
		while (line != null && !"EndSubSection".equals(line.trim())) {
			discoveryChecks.add(line.trim());
			line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		}
		if (line == null) {
			throwAndLog(file + " found SubSection \"Discovery\" but did not find EndSubSection.");
		}
		application.setDiscoveryChecks(discoveryChecks);
		
		line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		if (!"EndSection".equals(line.trim())) {
			throwAndLog(file + " expected EndSection but found '" + line + "' at line " + lineNumber);
		}

		//=====================================================================
		// Section "Outputs"
		//=====================================================================
		
		line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		if (!"Section \"Outputs\"".equals(line.trim())) {
			throwAndLog(file + " expected 'Section \"Outputs\"' but found '" + line + "' at line " + lineNumber);
		}
		
		line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		
		while (line != null && !"EndSection".equals(line.trim())) {
			parts = line.split(",");
			if (parts.length == 2 && parts[0].trim().length() > 0 && parts[1].trim().length() > 3) {
				String outputName = parts[0].trim();
				String commandLine = parts[1].trim();
				actions.add(new Action(outputName, commandLine, nameInEnvironmentView));
			} else {
				logger.warning(String.format("file '%s' line %s. Expected line like this \noutput name,\tscripts\\script-file.py\nbut was '%s'.",
						fileName, lineNumber, line));
			}
			line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		}
		
		if (line == null || !"EndSection".equals(line.trim())) {
			throwAndLog(file + " expected EndSection but found '" + line + "' at line " + lineNumber);
		}			

		application.setActions(actions);

		//=====================================================================
		// Section "Menu"
		//=====================================================================
		
		line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
		if (line != null && "Section \"Menu\"".equals(line.trim())) {
			boolean ok = false;
			line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
			String[] equalsParts = line.split("selected = ");
			if (equalsParts.length == 2 && equalsParts[1].trim().length() > 0) {
				parts = equalsParts[1].split(",");
				if (parts.length > 0) {
					for (String part : parts) {
						Action action = application.getActionByOutputName(part.trim());
						if (action == null) {
							logger.warning(String.format("file '%s' line %s. Did not find \"%s\" in the outputs defined in Section \"Outputs\"", fileName, lineNumber, part.trim()));
						} else {
							action.setSelectedByDefault(true);
							ok = true;
						}
					}
				}
			}
			if (!ok) {
				application.getActions().get(0).setSelectedByDefault(true);
				logger.warning(String.format("file '%s' line %s. Expected line like this \nselected = server.log, request.log\nbut was '%s'.", fileName, lineNumber, line));
			}

			ok = false;
			line = FileDAOHelper.skipPastBlankLinesAndComments(reader, lineNumber);
			equalsParts = line.split("commands = ");
			if (equalsParts.length == 2 && equalsParts[1].trim().length() > 0) {
				parts = equalsParts[1].split(",");
				if (parts.length > 0) {
					for (String part : parts) {
						Action action = application.getActionByOutputName(part.trim());
						if (action == null) {
							// need to find it somewhere else
							logger.warning(String.format("file '%s' line %s. Did not find command \"%s\" in the outputs defined in Section \"Outputs\"", fileName, lineNumber, part.trim()));
						} else {
							action.setCommand(true);
							ok = true;
						}
					}
				}
			}
			if (!ok) {
				logger.warning(String.format("file '%s' line %s. Expected line like this \ncommands = start, stop\nbut was '%s'.", fileName, lineNumber, line));
			}
			
		} else {
			// no menu section
			application.getActions().get(0).setSelectedByDefault(true);			
			logger.warning(String.format("file '%s' line %s. Expected line like this \nSection \"Menu\"\nbut was '%s'.", fileName, lineNumber, line));			
		}
		
		return application;
	}

	private void throwAndLog(String msg) {
		logger.severe(msg);
		throw new MonitorRuntimeException(msg);
	}

}

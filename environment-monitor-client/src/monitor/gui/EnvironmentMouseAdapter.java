package monitor.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import monitor.soap.client.MonitorServiceProxy;
import monitorservice.Action;
import monitorservice.Application;
import monitorservice.EnvironmentViewRow;

/** Mouse Listener for {@link EnvironmentScrollPane}. */
class EnvironmentMouseAdapter extends MouseAdapter {
	
	static Logger logger = Logger.getLogger(MouseAdapter.class.getName());
	
	MonitorServiceProxy monitorServiceProxy = MonitorServiceProxy.getInstance();	

	// Action commands on serveral menus
	public static final String ADD_SERVER = "Add Server";
	public static final String DELETE_ROW = "Delete Row";
	public static final String VIEW_OUTPUT = "View Output";
	public static final String OPEN_TERMINAL = "Open Terminal";
	public static final String DISCOVER_APPS = "Discover Apps";
    // environmentSubMenu
	public static final String RENAME = "Rename";
	public static final String RESTART_OUTPUTS = "Restart Outputs";
	public static final String SAVE = "Save";
	public static final String DELETE = "Delete";
	public static final String DISCOVER_ALL_APPS = "Discover All Apps";	
	
	// mouse actions
	public static final String DOUBLE_CLICK = "Double Click";
	
	private EnvironmentScrollPane environmentScrollPane;
	
	JPopupMenu noServerMenu;
	JPopupMenu serverMenu;
	
	EnvironmentMouseAdapter(EnvironmentScrollPane environmentScrollPane) {
        this.environmentScrollPane = environmentScrollPane;
    }

	@SuppressWarnings("serial")
	private class MenuItem extends JMenuItem {
		public MenuItem(String text) {
			super(text);
			addActionListener(environmentScrollPane);
		}
	}
	
	private void createMenus() {
		// menu when EnvironmentScrollPane is right clicked 
        noServerMenu = new JPopupMenu();
        noServerMenu.add(new MenuItem(ADD_SERVER));
        noServerMenu.addSeparator();
        noServerMenu.add(createEnvironmentSubMenu());

        // serverMenu with View Output option
        serverMenu = new JPopupMenu();
        EnvironmentViewRow row = environmentScrollPane.getSelectedRow();
        if (row != null && row.getOutputName().length() > 0) {
	        serverMenu.add(new MenuItem(VIEW_OUTPUT));
	        serverMenu.addSeparator();
        }
        if (row != null && row.getApplication() != null && row.getApplication().getNameInEnvironmentView().length() > 0) {
        	serverMenu.add(createApplicationSubMenu(row));
	        serverMenu.addSeparator();
        }
        
        serverMenu.add(createServerSubMenu());
        serverMenu.addSeparator();
        serverMenu.add(createEnvironmentSubMenu());
        serverMenu.addSeparator();
        serverMenu.add(new MenuItem(DELETE_ROW));
	}

	private JMenuItem createApplicationSubMenu(EnvironmentViewRow row) {
		String fileName = row.getApplication().getFileName();
		String nameInEnvironmentView = row.getApplication().getNameInEnvironmentView(); 
		JMenu applicationSubMenu = new JMenu(nameInEnvironmentView);
		try {
			Application application = monitorServiceProxy.loadApplicationByFileName(fileName, nameInEnvironmentView);
			for (Action action : application.getActions()) {
				String outputName = action.getOutputName();
				boolean checked = environmentScrollPane.applicationHasOutput(row.getServerName(), nameInEnvironmentView, outputName);
				boolean isCommand = action.isCommand(); 
				JMenuItem jMenuItem = null;
				if (action.isCommand()) {
					jMenuItem = new JMenuItem(outputName);
				} else {
					jMenuItem = new JCheckBoxMenuItem(outputName, checked);	
				}
				jMenuItem.addActionListener(environmentScrollPane);
				// The HttpQueryStringParser format is used  name1=value1&name2=value2&name3=value3
				jMenuItem.setActionCommand(String.format("Menu=application&server=%s&nameInEnvironmentView=%s&outputName=%s&checked=%b&isCommand=%b&fileName=%s",
						row.getServerName(), nameInEnvironmentView, outputName, !checked, isCommand, fileName));
				applicationSubMenu.add(jMenuItem);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "probelm creating application menu using fileName:" + fileName, e);
		}
		return applicationSubMenu;
	}

	private JMenu createServerSubMenu() {
		JMenu serverSubMenu = new JMenu("Server");
        serverSubMenu.add(new MenuItem(OPEN_TERMINAL));
        serverSubMenu.add(new MenuItem(ADD_SERVER));
        serverSubMenu.add(new MenuItem(DISCOVER_APPS));
		return serverSubMenu;
	}

	private JMenu createEnvironmentSubMenu() {
		JMenu environmentSubMenu = new JMenu("Environment");
        environmentSubMenu.add(new MenuItem(RENAME));
        environmentSubMenu.add(new MenuItem(RESTART_OUTPUTS));
        environmentSubMenu.add(new MenuItem(SAVE));
        environmentSubMenu.add(new MenuItem(DELETE));
        environmentSubMenu.add(new MenuItem(DISCOVER_ALL_APPS));
        // initial hard coded prototype
        //MenuItem javaVersionAll = new MenuItem("Java Version all");
        //javaVersionAll.setActionCommand("Menu=environment&outputName=Java version all");
        //environmentSubMenu.add(javaVersionAll);
        
        MenuItem executeAll = new MenuItem("Execute...");
        executeAll.setActionCommand("Menu=environment&outputName=Execute...");
        environmentSubMenu.add(executeAll);
        
		return environmentSubMenu;
	}
	
    private JPopupMenu selectPopupMenu(String component, Point point) {
    	createMenus();    	
    	if ("EnvironmentScrollPane".equals(component)) {
            return noServerMenu;
    	} else if ("EnvironmentTable".equals(component)) {
    		return serverMenu;
    	}
    	logger.severe("Popup menu error. Unknown component: " + component);
    	return null;
    }
	
    @Override
	public void mouseClicked(MouseEvent e) {
    	logger.finest("mouse clicked, click count " + e.getClickCount());
    	if (e.getClickCount() == 2) {
    		ActionEvent actionEvent = new ActionEvent(this, MouseEvent.MOUSE_CLICKED, DOUBLE_CLICK);
			environmentScrollPane.actionPerformed(actionEvent);
    	}
	}

    @Override 
	public void mousePressed(MouseEvent e) {
    	logger.finest("mouse pressed, click count " + e.getClickCount());
        maybeShowPopup(e);
    }

    @Override    
    public void mouseReleased(MouseEvent e) {
    	logger.finest("mouse released, click count " + e.getClickCount());
    	maybeShowPopup(e);
    }

    private boolean maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
        	environmentScrollPane.selectTableRowAtPoint(e.getPoint());
            JPopupMenu popup = selectPopupMenu(e.getComponent().getClass().getSimpleName(), e.getPoint());
        	popup.show(e.getComponent(), e.getX(), e.getY());
            return true;
        }
        return false;
    }

}

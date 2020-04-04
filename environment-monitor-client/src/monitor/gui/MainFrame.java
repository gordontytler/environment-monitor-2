package monitor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import monitor.soap.client.MonitorServiceProxy;

public class MainFrame {

	private static final long serialVersionUID = 1L;
	private CloseableTabbedPane environmentTabbedPane;
	private static JFrame mainFrame;	
	private JToolBar toolBar;
	
    public MainFrame() {
	}

    /** URL is arg[0] e.g. http://10.2.8.82:8085/Monitor  if not passed it will be read from config.properties */
    public static void main(String[] args) {
    	if (args.length > 0) {
    		MonitorServiceProxy.setMonitorServiceURL(args[0]);
    	}
        try {
        	//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e1) {
			// Windows does not have GTK
	        try {
	        	//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (Exception e2) {
				// an older JDK might not have Nimbus
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}   			
		}    	
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    public static JFrame getFrame() {
    	return mainFrame;
    }
    
    /**
     * Create the GUI and show it. Should be invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window.
        mainFrame = new JFrame("Environment monitor");
        Image icon = Toolkit.getDefaultToolkit().createImage("icon.png");
        mainFrame.setIconImage(icon);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Prompt for environment
    	String environmentName = new EnvironmentSelector().getEnvironmentName(mainFrame);
    	if (environmentName == null) {
    		System.exit(0);
    	}
    	
        // Set up the content pane.
        MainFrame mainFrameBoxLayout = new MainFrame();
        mainFrameBoxLayout.addComponentsToPane(mainFrame.getContentPane(), environmentName);
        // Display the window.
        mainFrame.pack();
        mainFrame.setSize(700, 400);
        mainFrame.setLocationRelativeTo(null);        
        mainFrame.setVisible(true);
    }


	private void addComponentsToPane(Container pane, String environmentName) {
		pane.setLayout(new BorderLayout());
		makeToolBar();
		toolBar.add(makeEnvironmentButton());		
		toolBar.add(makeReloadButton());
		toolBar.add(makeSaveButton());		
		toolBar.add(makeDeleteButton());
		makeEnvironmentTabbedPane(environmentName);
		pane.add(toolBar, BorderLayout.PAGE_START);
		pane.add(environmentTabbedPane, BorderLayout.CENTER);
    }


	private void makeToolBar() {
		if (toolBar == null) {
			toolBar = new JToolBar();
			toolBar.setOpaque(false);
			toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);					
		}
	}

	private JButton makeEnvironmentButton() {
		JButton environmentButton = new JButton();
		ImageIcon icon = new ImageIcon("open.png");
		environmentButton.setIcon(icon);
		environmentButton.setFocusable(false); // so it wont get key events
		environmentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String environmentName = new EnvironmentSelector().getEnvironmentName(mainFrame);
				if (environmentName != null) {
					Component component = new EnvironmentScrollPane(environmentName, environmentTabbedPane);
					environmentTabbedPane.addTab(environmentName, component);
					environmentTabbedPane.setSelectedComponent(component);
				}
			}
		});
		return environmentButton;
	}

	private JButton makeReloadButton() {
		JButton reloadButton = new JButton();
		ImageIcon icon = new ImageIcon("reload.png");
		reloadButton.setIcon(icon);
		reloadButton.setFocusable(false); // so it wont get key events
		reloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EnvironmentScrollPane environmentScrollPane = (EnvironmentScrollPane) environmentTabbedPane.getSelectedComponent();
				environmentScrollPane.restartOutputs();
			}
		});
		return reloadButton;
	}
	
	private JButton makeDeleteButton() {
		JButton deleteButton = new JButton();
		ImageIcon icon = new ImageIcon("delete.png");
		deleteButton.setIcon(icon);
		deleteButton.setFocusable(false); // so it wont get key events
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EnvironmentScrollPane environmentScrollPane = (EnvironmentScrollPane) environmentTabbedPane.getSelectedComponent();
				environmentScrollPane.deleteEnvironment();
			}
		});
		return deleteButton;
	}

	private JButton makeSaveButton() {
		JButton saveButton = new JButton();
		ImageIcon icon = new ImageIcon("save.png");
		saveButton.setIcon(icon);
		saveButton.setFocusable(false); // so it wont get key events
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EnvironmentScrollPane environmentScrollPane = (EnvironmentScrollPane) environmentTabbedPane.getSelectedComponent();
				environmentScrollPane.saveEnvironment();
			}
		});
		return saveButton;
	}
	
	private void makeEnvironmentTabbedPane(String environmentName) {
		if (environmentTabbedPane == null) {
			environmentTabbedPane = new CloseableTabbedPane();
			Component component = new EnvironmentScrollPane(environmentName, environmentTabbedPane);
			environmentTabbedPane.addTab(environmentName, component);
			environmentTabbedPane.setSelectedComponent(component);
		}
	}

}

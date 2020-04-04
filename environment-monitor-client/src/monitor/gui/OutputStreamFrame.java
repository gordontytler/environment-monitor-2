package monitor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import monitorservice.EnvironmentViewRow;

@SuppressWarnings("serial")
public class OutputStreamFrame extends JFrame implements WindowListener, ClipboardOwner  {

	OutputStreamPanel panel;
	JToolBar toolBar;
	private String environmentName; 
	private EnvironmentViewRow row;
	private String title;

	public OutputStreamFrame(String environmentName, EnvironmentViewRow row) {
    	super(String.format("%s %s %s", row.getServerName(), row.getApplicationName(), row.getOutputName()));		
	   	title = String.format("%s %s %s", row.getServerName(), row.getApplicationName(), row.getOutputName());
	   	this.setIconImage(MainFrame.getFrame().getIconImage());
    	this.environmentName = environmentName;
		this.row = row;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
    private void createAndShowGUI() {
        //Create and set up the window.    	
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panel = new OutputStreamPanel(this, environmentName, row);
        
		toolBar = new JToolBar();
		toolBar.setOpaque(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(makeClipBoardButton(panel, this));
		toolBar.add(makePauseButton(panel));
        
        getContentPane().add(toolBar, BorderLayout.PAGE_START);
        getContentPane().add(panel, BorderLayout.CENTER);
        //Display the window.
        pack();
        setVisible(true);
        addWindowListener(this);
    }

	private JButton makeClipBoardButton(final OutputStreamPanel panel, final ClipboardOwner clipboardOwner) {
		JButton clipBoardButton = new JButton();
		ImageIcon icon = new ImageIcon("copy.png");
		clipBoardButton.setIcon(icon);
		clipBoardButton.setFocusable(false); // so it wont get key events
		clipBoardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String data = title + "\r\n" + panel.getText();
				StringSelection stringSelection = new StringSelection(data);
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    clipboard.setContents(stringSelection, clipboardOwner);
			}
		});
		return clipBoardButton;
	}    
    
	private JToggleButton makePauseButton(final OutputStreamPanel panel) {
		JToggleButton pauseButton = new JToggleButton();
		ImageIcon icon = new ImageIcon("pause.png");
		pauseButton.setIcon(icon);
		pauseButton.setFocusable(false); // so it wont get key events
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.togglePauseOutput();
			}
		});
		return pauseButton;
	}    

	
	
    @Override
    public void windowClosing(WindowEvent e) {
    	panel.windowClosing(e);
    }

    // window events we ignore
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}	
	
}
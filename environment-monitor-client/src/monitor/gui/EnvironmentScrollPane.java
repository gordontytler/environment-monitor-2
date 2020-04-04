package monitor.gui;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import monitor.soap.client.AdaptiveSleeper;
import monitor.soap.client.MonitorServiceProxy;
import monitorservice.EnvironmentView;
import monitorservice.EnvironmentViewRow;
import monitorservice.UpDownState;

public class EnvironmentScrollPane extends JScrollPane implements ActionListener, CloseableTabbedPaneListener {

	private static final long serialVersionUID = -3283603120202943125L;
	static Logger logger = Logger.getLogger(EnvironmentScrollPane.class.getName());
	
	private EnvironmentTable table;
	private EnvironmentTableModel environmentDataModel;	
	private String environmentName;
	private CloseableTabbedPane environmentTabbedPane;
	private boolean tabNotClosed = true;
	private RefreshEnvironmentViewWorker refreshEnvironmentViewWorker;
	ExecutorService executor = Executors.newSingleThreadExecutor();
	ExecutorService restartOutputsExecutor = Executors.newSingleThreadExecutor();
	//private ServerNameParser serverNameParser = new ServerNameParser();

	private EnvironmentMouseListener environmentMouseListener;
	
	EnvironmentScrollPane(final String environmentName,  CloseableTabbedPane environmentTabbedPane) {
		this.environmentName = environmentName;
		this.environmentTabbedPane = environmentTabbedPane;
		environmentTabbedPane.addCloseableTabbedPaneListener(this);
		table = new EnvironmentTable(this);
		table.setDefaultRenderer(ListOfOutputHistory.class, new PictureRenderer(table.getBackground(), table.getSelectionBackground()));
		environmentDataModel = new EnvironmentTableModel();
		table.setModel(environmentDataModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.initColumnSizes();
		setViewportView(table);
		refreshEnvironmentViewWorker = new RefreshEnvironmentViewWorker(this);
		logger.info(environmentName + " about to start refreshEnvironmentViewWorker");
		executor.execute(refreshEnvironmentViewWorker);
		addMouseListener(new EnvironmentMouseAdapter(this));
		environmentMouseListener = new EnvironmentMouseListener(environmentName, environmentDataModel, this, environmentTabbedPane, refreshEnvironmentViewWorker, table);
	}
	
	
	@SuppressWarnings("serial")
	class EnvironmentTable extends JTable {
		
		EnvironmentMouseAdapter environmentMouseAdapter;
		EnvironmentScrollPane environmentScrollPane;
		
		public EnvironmentTable(EnvironmentScrollPane environmentScrollPane) {
			super();
			this.environmentScrollPane = environmentScrollPane;
			environmentMouseAdapter = new EnvironmentMouseAdapter(environmentScrollPane);
			addMouseListener(environmentMouseAdapter);
		}

		@Override
		protected void processKeyEvent(KeyEvent e) {
			if (e.getKeyChar() == 10) {  
				e.consume();
				if (e.getID() == KeyEvent.KEY_TYPED) {
					openOutputStreamFrame(true);
				}
			} else {
				logger.info("key " + new Integer(e.getKeyChar()) + " keyCode " + e.getKeyCode());
				if (e.getKeyChar() == 65535 && e.getKeyCode() == 525) {
					PointerInfo info = MouseInfo.getPointerInfo();
					// TODO when the popup key is pressed the menu pops up in the wrong place - see also CloseableTabbedPane
					MouseEvent me = new MouseEvent(environmentScrollPane, MouseEvent.MOUSE_RELEASED, System.nanoTime(), 0, info.getLocation().x, info.getLocation().y, 2, true);
					environmentMouseAdapter.mouseReleased(me);
				}
				super.processKeyEvent(e);
			}
		};
		
		
		void initColumnSizes() {
	        int[] minWidth = 		{100, 80,  80,  40, 50};
	        int[] preferedWidth = 	{200, 160, 160, 60, 400};
	        int[] maxWidth = 		{400, 320, 320, 80, 20000};        
	        for (int i = 0; i < 5; i++) {
	        	TableColumn column = getColumnModel().getColumn(i);
	            column.setMinWidth(minWidth[i]);
	            column.setPreferredWidth(preferedWidth[i]);
	            column.setMaxWidth(maxWidth[i]);
	        }
	    }		
	}


	
	
	@SuppressWarnings("serial")
	class EnvironmentTableModel extends AbstractTableModel {
	    private String[] columnNames = new String[] {"Server","Application","Output","I/O","Image"};
	    private List<EnvironmentViewRow> rows = new ArrayList<EnvironmentViewRow>();
	    private List<ListOfOutputHistory> histories = new ArrayList<ListOfOutputHistory>();
	    
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}
		
		@Override
	    public String getColumnName(int col) {
	        return columnNames[col];
	    }
		
		@Override
		public  synchronized Object getValueAt(int rowIndex, int columnIndex) {
			EnvironmentViewRow row = rows.get(rowIndex);
			Object valueAt = null;
			switch (columnIndex) {
				case 0 : valueAt = row.getServerName(); break; 
				case 1 : valueAt = row.getApplicationName(); break;
				case 2 : valueAt = row.getOutputName(); break;
				case 3 : valueAt = getHighestChunkValueAt(row); break;
				case 4 : valueAt = histories.get(rowIndex); break;
			}
			return valueAt;
		}
		
		private synchronized Object getHighestChunkValueAt(EnvironmentViewRow row) {
			if (row.getApplication() != null && UpDownState.DOWN == row.getApplication().getUpDownState()) {
				return "DOWN";
			} else {
				return row.getHighestChunk();
			}
		}
		
		
		@Override
		public Class<? extends Object> getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

		public synchronized void reload(List<EnvironmentViewRow> newRows, List<ListOfOutputHistory> newHistories) {
			this.rows = newRows;
			this.histories = newHistories;
		}
		
		public  synchronized void reloadRow(int y, EnvironmentViewRow newRow, ListOfOutputHistory newHistory) {
			this.rows.set(y, newRow);
			this.histories.set(y, newHistory);
		}

		public List<EnvironmentViewRow> getEnvironmentViewRows() {
			return rows;
		}

	}

	/** see {@link CloseableTabbedPane#fireCloseTab(int)} */
	@Override
	public boolean closeTab(int tabIndexToClose) {
		Component component = environmentTabbedPane.getComponentAt(tabIndexToClose);
		if (component == this) {
			logger.info("\n\n\nGot closeTab message for tab " + tabIndexToClose);
			tabNotClosed = false;
			refreshEnvironmentViewWorker.cancel(true); // this may interrupt MonitorServiceProxy.getEnvironmentView or AdaptiveSleeper
		}
		return true; // allows the tab to close, it may be a different tab
	}	
	
	
	
	public final class RefreshEnvironmentViewWorker extends SwingWorker<Void, Integer> {

		MonitorServiceProxy monitorServiceProxy = MonitorServiceProxy.getInstance();
		EnvironmentView newView;
		EnvironmentView oldView = new EnvironmentView();
		{oldView.setOutputHistoryTimeStamp(Long.MAX_VALUE);}
		AdaptiveSleeper sleeper = new AdaptiveSleeper(10000, 500, 200);
		EnvironmentScrollPane thisTabComponent;
		
		public RefreshEnvironmentViewWorker(final EnvironmentScrollPane thisTabComponent) {
			super();
			this.thisTabComponent = thisTabComponent;
		}

		@Override
		protected Void doInBackground() throws Exception {

			logger.setLevel(Level.FINER);
			
			logger.info(environmentName + " in RefreshEnvironmentViewWorker, tabNotClosed is " + tabNotClosed);
			
			int loopCount = 0;
    		int selectedIndex  = 0;
    		EnvironmentScrollPane selected = null;
    		int tabCount = 0;
    		boolean thisTabIsSelected = false;
    		boolean thisTabWasSelected = false;
			
			try {
				do {
					boolean changeFound = false;

					// We can't ask the CloseableTabbedPane about components until it has had time to warm up
					if (loopCount > 3 || loopCount < 0) {
						selectedIndex  = environmentTabbedPane.getSelectedIndex();   
						// getTabComponentAt(selectedIndex) does not work
						if (selectedIndex > -1) {
							selected = (EnvironmentScrollPane) environmentTabbedPane.getComponentAt(selectedIndex);
						} else {
							selected = thisTabComponent;	
						}
						
						tabCount = environmentTabbedPane.getTabCount();
					}

					logger.finer(String.format("%s loopCount %d selected == thisTabComponent is %b tabCount %d selectedIndex %d"
							, environmentName, loopCount, selected == thisTabComponent, tabCount, selectedIndex));

					thisTabWasSelected = thisTabIsSelected;
					thisTabIsSelected = false;
					
					if (loopCount < 4 || (selected == thisTabComponent || (tabCount == 1) ) ) {
						
						thisTabIsSelected = true;
				    	newView = monitorServiceProxy.getEnvironmentView(environmentName, oldView.getOutputHistoryTimeStamp());
				    	
						logger.finer(String.format("%s gotEnvironmentView newRows: %d oldRows: %d oldOutputHistoryTimeStamp: %d newOutputHistoryTimeStamp: %d", environmentName, newView.getRows().size(), oldView.getRows().size(), oldView.getOutputHistoryTimeStamp(), newView.getOutputHistoryTimeStamp()));
				    	
				    	if (newView.getRows().size() != oldView.getRows().size() || (oldView.getOutputHistoryTimeStamp() == 0l && newView.getOutputHistoryTimeStamp() != 0l)) {
				    		changeFound = true;
				    		environmentDataModel.reload(newView.getRows(), ListOfOutputHistory.valueOf(environmentName, newView.getRows()));
				    		for (int x=0; x< newView.getRows().size(); x++) {
				    			publish(x);
				    		}
				    		environmentDataModel.fireTableDataChanged();
				    		sleeper.decreaseDelay();
				    		logger.fine(String.format("%s fireTableDataChanged newRows: %d oldRows: %d oldOutputHistoryTimeStamp: %d newOutputHistoryTimeStamp: %d", environmentName, newView.getRows().size(), oldView.getRows().size(), oldView.getOutputHistoryTimeStamp(), newView.getOutputHistoryTimeStamp()));
				    	} else {
				    		for (int y=0; y < newView.getRows().size(); y++) {
				    			EnvironmentViewRow newRow = newView.getRows().get(y);
				    			EnvironmentViewRow oldRow = oldView.getRows().get(y);
				    			boolean upDownStateHasChanged = false;
				    			if (newRow.getApplication() != null && oldRow.getApplication() != null 
				    					&& newRow.getApplication().getUpDownState() != oldRow.getApplication().getUpDownState()) {
				    				upDownStateHasChanged = true;
				    			}
				    			if (newRow.getHighestChunk() != oldRow.getHighestChunk() || upDownStateHasChanged) {
				    				changeFound = true;
				    				environmentDataModel.reloadRow(y, newRow, ListOfOutputHistory.valueOf(environmentName, newRow));
				    				environmentDataModel.fireTableRowsUpdated(y, y);
				    				if (newRow.getHighestChunk() == 0 && oldRow.getHighestChunk() > 0) {
				    					logger.severe(String.format("%s histories will be wiped new highestChunk %d sessionId %s but old highestChunk %d sessionId %s", environmentName, newRow.getHighestChunk(), newRow.getSessionId(), oldRow.getHighestChunk(), oldRow.getSessionId()));
				    				}
				    			}
				    		}
				    	}
				    	// The first request loads no history. The second loads all. After that only changes are loaded.
				    	if (oldView.getOutputHistoryTimeStamp() == Long.MAX_VALUE) {
				    		newView.setOutputHistoryTimeStamp(0l);
				    		logger.info("next refresh should load all history");
				    	}
				    	oldView = newView;
					}
					if (thisTabIsSelected != thisTabWasSelected) {
						logger.info(environmentName + (thisTabIsSelected ? " tab selected. Output will start." : " tab no longer selected. Output paused."));
						sleeper.resetToMinimum();
					} else {
						if (changeFound) {
							logger.finer(environmentName + " changeFound");
							sleeper.decreaseDelay();
						} else {
							sleeper.increaseDelay();
						}
					}
				   	sleeper.sleep();
				   	loopCount++;
				} while (tabNotClosed);
			} catch (Exception e) {
				if (!e.getMessage().contains("Sleep interupted. Window may be closing. Rethrowing exception to halt the calling thread!")) {
					logger.log(Level.SEVERE, "An exception here will kill the thread and freeze the display.", e);					
				}
			}
			return null;
		}
	}

	//******************************************************************
	// Helper methods for EnvironmentMouseAdapter and MainFrame buttons.
	//******************************************************************
	
	@Override
	public void actionPerformed(ActionEvent e) {
		environmentMouseListener.actionPerformed(e);
	}

	public void restartOutputs() {
		environmentMouseListener.restartOutputs();
	}

	public void saveEnvironment() {
		environmentMouseListener.saveEnvironment();
	}


	public void deleteEnvironment() {
		environmentMouseListener.deleteEnvironment();
	}

	public void selectTableRowAtPoint(Point p) {
		int rowNumber = table.rowAtPoint(p);
		ListSelectionModel model = table.getSelectionModel();
		model.setSelectionInterval(rowNumber, rowNumber);
	}
	
	private void openOutputStreamFrame(boolean showExistingStream) {
		environmentMouseListener.openOutputStreamFrame(showExistingStream);
	}

	public EnvironmentViewRow getSelectedRow() {
		int rowIndex = table.getSelectedRow();
		if (rowIndex == -1) {
			logger.info("right click but no selected row");
			return null;
		} else {
			return environmentDataModel.getEnvironmentViewRows().get(rowIndex);
		}
	}
	

	public boolean applicationHasOutput(String serverName, String nameInEnvironmentView, String outputName) {
		for (EnvironmentViewRow row : environmentDataModel.getEnvironmentViewRows()) {
			if (row.getServerName() != null && row.getServerName().equals(serverName) &&
				row.getApplication() != null && row.getApplication().getNameInEnvironmentView().equals(nameInEnvironmentView) &&
				row.getOutputName() != null &&	row.getOutputName().equals(outputName)) {
				return true;
			}
		}
		return false;
	}

	
}

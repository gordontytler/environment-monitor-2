package monitor.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import monitor.ClientConfiguration;
import monitorservice.EnvironmentViewRow;
import monitorservice.OutputHistory;

/** javax.swing.table.TableCellRenderer wont work with a list. It needs an object for the table data.
 *  This is the object. It also appends previous history so we only need to get new history with each refresh. 
 *  It separates the updating of history from the GUI paint events which frequently don't trigger when the table model is updated.
 *  We always paint the whole history. */
public class ListOfOutputHistory {
	
	private List<OutputHistory> outputHistories;
	private PictureIconKey pictureIconKey;
	private static ConcurrentHashMap<PictureIconKey, ListOfOutputHistory> allHistory = new ConcurrentHashMap<PictureIconKey, ListOfOutputHistory>();
	private static int maxHistory = ClientConfiguration.getInstance().getChunkedOutputArraySize();
	
	/** Add the new histories in the row to those already obtained or wipe history if highest chunk is zero. */
	public static ListOfOutputHistory valueOf(String environmentName, EnvironmentViewRow row) {
		PictureIconKey key = new PictureIconKey(environmentName, row.getServerName(), row.getApplicationName(), row.getOutputName());
		ListOfOutputHistory listOfOutputHistory = allHistory.get(key);
		if (listOfOutputHistory == null || row.getHighestChunk() == 0) {
			listOfOutputHistory = new ListOfOutputHistory(row.getOutputHistory(), key);
			allHistory.put(key, listOfOutputHistory);
		} else {
			List<OutputHistory> newHistory = new ArrayList<OutputHistory>(maxHistory);
			int x=0;
			for (; x < maxHistory && x < row.getOutputHistory().size(); x++) {
				newHistory.add(row.getOutputHistory().get(x));
			}
			for (int y=0; x < maxHistory && y < listOfOutputHistory.getOutputHistories().size(); x++, y++) {
				newHistory.add(listOfOutputHistory.getOutputHistories().get(y));
			}
			listOfOutputHistory.setOutputHistories(newHistory);
		}
		return listOfOutputHistory;
	}
	
	public static List<ListOfOutputHistory> valueOf(String environmentName, List<EnvironmentViewRow> rows) {
		List<ListOfOutputHistory> histories = new ArrayList<ListOfOutputHistory>(rows.size());
		for (EnvironmentViewRow row : rows) {
			histories.add(ListOfOutputHistory.valueOf(environmentName, row));
		}
		return histories;
	}
	
	public ListOfOutputHistory(List<OutputHistory> outputHistories, PictureIconKey pictureIconKey) {
		this.outputHistories = outputHistories;
		this.pictureIconKey =pictureIconKey;
	}

	public ListOfOutputHistory() {
		outputHistories = new ArrayList<OutputHistory>();
	}

	public List<OutputHistory> getOutputHistories() {
		return outputHistories;
	}

	public void setOutputHistories(List<OutputHistory> outputHistories) {
		this.outputHistories = outputHistories;
	}

	public PictureIconKey getPictureIconKey() {
		return pictureIconKey;
	}

	public void setPictureIconKey(PictureIconKey pictureIconKey) {
		this.pictureIconKey = pictureIconKey;
	}
	
}

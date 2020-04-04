package monitor.gui;

import java.awt.Frame;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import monitor.soap.client.MonitorServiceProxy;


public class EnvironmentSelector {

	public static final String NEW = "new...";
	
	public EnvironmentSelector() {
	}

	/** @return selected environment name or a new name or null if cancel pressed. */
	public String getEnvironmentName(Frame mainFrame) {
		String environmentName = "";
		JDialog dialog = new JDialog(mainFrame);
		dialog.setModal(true);
        dialog.pack();
        List<String> names = MonitorServiceProxy.getInstance().getEnvironmentNames();
        names.add(0, NEW);
        Object[] possibilities = names.toArray();

        while (environmentName=="") {
        	environmentName = (String)JOptionPane.showInputDialog(
                    dialog,
                    "Please select an environment...",
                    "Environment monitor",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    possibilities,
                    possibilities[0]);

		    if (environmentName == NEW) {
				environmentName = JOptionPane.showInputDialog(dialog,
						"Enter environment name",
						"Environment Name",
						JOptionPane.QUESTION_MESSAGE);
				if (environmentName != null) {
					environmentName = environmentName.trim();
					if (names.contains(environmentName)) {
						JOptionPane.showMessageDialog(dialog, "\"" + environmentName + "\" already exists.", "Environment Name", JOptionPane.ERROR_MESSAGE);
						environmentName = "";
					} else {
						if (environmentName == "new..." || environmentName.length() == 0) {
							JOptionPane.showMessageDialog(dialog, "\"" + environmentName + "\" is not a valid environment name.", "Environment Name", JOptionPane.ERROR_MESSAGE);
							environmentName = "";
						}
					}
				} else {
					environmentName = "";
				}
			}
        }
        return environmentName;
	}
}



package monitor.model;

import java.util.HashMap;
import java.util.List;

public class EnvironmentView {

	/** Environment names must be unique even after FileNameHelper.toFileName(environmentName) */
	private String environmentName;
	/** fileName is created from the environmentName and saved here for error messages */
	private String fileName;
	/** the time when environment histories were saved */
	private long outputHistoryTimeStamp;
	private List<EnvironmentViewRow> rows;
	private HashMap<String, String> properties;
	private boolean rowsModified = false;
	
	// no arg constructor required by at com.sun.xml.internal.ws.spi.ProviderImpl.createAndPublishEndpoint(Unknown Source)
	public EnvironmentView() {
	}
	
	public EnvironmentView(String environmentName) {
		this.environmentName = environmentName;
	}

	public List<EnvironmentViewRow> getRows() {
		return rows;
	}

	public void setRows(List<EnvironmentViewRow> rows) {
		this.rows = rows;
	}
	
	public String getEnvironmentName() {
		return environmentName;
	}
	
	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getOutputHistoryTimeStamp() {
		return outputHistoryTimeStamp;
	}

	public void setOutputHistoryTimeStamp(long outputHistoryTimeStamp) {
		this.outputHistoryTimeStamp = outputHistoryTimeStamp;
	}

	public HashMap<String, String> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
	}

	public boolean isRowsModified() {
		return rowsModified;
	}

	public void setRowsModified(boolean rowsModified) {
		this.rowsModified = rowsModified;
	}

	@Override
	public String toString() {
		return "EnvironmentView [environmentName=" + environmentName
				+ properties + ", rows=" + rows + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((environmentName == null) ? 0 : environmentName.hashCode());
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((rows == null) ? 0 : rows.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnvironmentView other = (EnvironmentView) obj;
		if (environmentName == null) {
			if (other.environmentName != null)
				return false;
		} else if (!environmentName.equals(other.environmentName))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.toString().equals(other.properties.toString()))
			return false;
		if (rows == null) {
			if (other.rows != null)
				return false;
		} else if (!rows.equals(other.rows))
			return false;
		return true;
	}

	
}

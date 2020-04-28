package monitor.dao;

import java.util.HashMap;

import monitor.model.Application;

public class ApplicationCache {

	private ApplicationDAO applicationDAO = new ApplicationDAO();
	private HashMap<ApplicationKey, Application > cache = new HashMap<ApplicationKey, Application>();
	private static ApplicationCache theInstance = new ApplicationCache();
	
	private ApplicationCache() {
	}

	public static ApplicationCache getInstance() {
		return theInstance;
	}
	
	public Application loadApplicationByFileName(String fileName, String nameInEnvironmentView) {
		return loadAndCache(fileName, nameInEnvironmentView);
	}
	
	private synchronized Application loadAndCache(String fileName, String nameInEnvironmentView) {
		ApplicationKey key = new ApplicationKey(fileName, nameInEnvironmentView);
		Application application = cache.get(key);
		if (application == null) {
			application = applicationDAO.loadApplicationByFileName(fileName, nameInEnvironmentView);
			cache.put(key, application);
		}
		return application.getSafeCopy();
	}

	public synchronized void resetCache() {
		cache = new HashMap<ApplicationKey, Application>();
	}
	
	private class ApplicationKey {
		private String fileName; 
		private String nameInEnvironmentView;
		public ApplicationKey(String fileName, String nameInEnvironmentView) {
			super();
			this.fileName = fileName;
			this.nameInEnvironmentView = nameInEnvironmentView;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
			result = prime * result + ((nameInEnvironmentView == null) ? 0 : nameInEnvironmentView.hashCode());
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
			ApplicationKey other = (ApplicationKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (fileName == null) {
				if (other.fileName != null)
					return false;
			} else if (!fileName.equals(other.fileName))
				return false;
			if (nameInEnvironmentView == null) {
				if (other.nameInEnvironmentView != null)
					return false;
			} else if (!nameInEnvironmentView.equals(other.nameInEnvironmentView))
				return false;
			return true;
		}
		private ApplicationCache getOuterType() {
			return ApplicationCache.this;
		}
	}
	
	
}

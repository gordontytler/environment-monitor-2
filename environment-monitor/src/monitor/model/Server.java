package monitor.model;

import monitor.implementation.MonitorRuntimeException;

public class Server {
	
	private final String host;
	
	/**
	 * @param host - machine friendly name e.g. domain name or IP address
	 */
	public Server(String host) {
		super();
		if (host == null || host.length() < 2) {
			throw new MonitorRuntimeException("Invalid host when creating Server instance: " + host);
		}
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	@Override
	public String toString() {
		return host;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
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
		Server other = (Server) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		return true;
	}
	
}

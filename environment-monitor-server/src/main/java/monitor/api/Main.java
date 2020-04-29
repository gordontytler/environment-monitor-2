package monitor.api;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import monitor.dao.EnvironmentNamesDAO;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.SessionManager;
import monitor.model.Configuration;

public class Main {
    
	static Logger logger = Logger.getLogger(Main.class.getName());

    public Main() {
    }
    
    /**
     * This occurs on some machines...
     * 
     * Caused by: java.net.BindException: Cannot assign requested address
	 *	at sun.nio.ch.Net.bind(Native Method)
	 * 	at sun.nio.ch.ServerSocketChannelImpl.bind(ServerSocketChannelImpl.java:119)
     * 
     * This does not fix it 
     * 
     * -Djava.net.preferIPv4Stack=true
     * 
     * And neither does 
     * 
     * add the following lines to /etc/sysctl.conf:
     * 
     * #disable ipv6
	 * net.ipv6.conf.all.disable_ipv6 = 1
	 * net.ipv6.conf.default.disable_ipv6 = 1
	 * net.ipv6.conf.lo.disable_ipv6 = 1
     * 
     * reboot
     * 
     * cat /proc/sys/net/ipv6/conf/all/disable_ipv6 
     * 
     * 0 means it's enabled and 1 - disabled.
     * 
     * @param args - not used 
     */
    public static void main(String[] args) {
    	try {
    		System.getProperties().put("java.net.preferIPv4Stack", "true");
			String monitorHostAndPort = Configuration.getInstance().getMonitorHostAndPort();
			String monitorURL = "http://" + monitorHostAndPort + "/Monitor";
			logger.info("Starting SOAP endpoint with wsdl " + monitorURL + "/?wsdl");
			MonitorServiceImpl monitorServiceImpl = new MonitorServiceImpl();
			Endpoint.publish(monitorURL, monitorServiceImpl);
			
			String scriptAddress = monitorURL + "Script";
			logger.info("Starting http endpoint for scripts at " + scriptAddress);
			Endpoint.publish(scriptAddress, new MonitorHttpProvider(monitorServiceImpl));

			Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
			    public void run() { AllSessionPools.getInstance().messyLogoutAllSessionsOnAllServers("new Thread(\"ShutdownHook\")"); }
			});
			EnvironmentNamesDAO.getInstance().getEnvironmentNames();
			
			new Thread(new SessionManager(), "SessionManager").start();
			
			//new Thread(new LoadTestThread(), "LoadTestThread").start();
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Problem starting up.", e);
		}
    }

}
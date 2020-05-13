
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import monitorservice.EnvironmentView;
import monitorservice.EnvironmentViewRow;
import monitorservice.LogonResult;
import monitorservice.MonitorService;
import monitorservice.MonitorService_Service;


public class WebServiceTest {
	//@WebServiceRef(wsdlLocation = "http://localhost:8080/Monitor/?wsdl")
	// not sure what the annotation does
	
	static MonitorService_Service service;

	public static void main(String[] args) {
		try {
			WebServiceTest client = new WebServiceTest();
			client.doTest(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void doTest(String[] args) throws MalformedURLException {
		// The SOAP server has to be started manually.
		// todo why does localhost get refused?
		service = new MonitorService_Service(new URL("http://gordon-hp-notebook:8084/Monitor/?wsdl"), new QName("http://MonitorService", "MonitorService"));
		System.out.println("Retrieving the port from the following service: " + service);
		MonitorService port = service.getMonitorServicePort();
		LogonResult logonResult = port.logon("localhost", "", "");
		System.out.println(String.format("commandStatus: %s, error: %s, sessionId: %s", logonResult.getCommandStatus(), logonResult.getErrorMessage(), logonResult.getSessionId()));
		System.out.println("Invoking the executeCommand operation on the port.");
		String response = port.executeCommand("ls -al", logonResult.getSessionId()).getOutput();
		System.out.println(response);
		System.out.println("Environment view.");
		EnvironmentView environmentView = port.getEnvironmentView("Test environment", Long.MAX_VALUE);
		for (EnvironmentViewRow row : environmentView.getRows()) {
			System.out.println(String.format("%s - %s - %s",row.getServerName(), row.getApplicationName(), row.getOutputName(), row.getCommandStatus()));	
		}
		
		System.exit(0);
	}
}

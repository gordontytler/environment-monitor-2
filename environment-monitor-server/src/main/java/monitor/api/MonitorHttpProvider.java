package monitor.api;



import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;

import monitor.dao.EnvironmentViewDAO;
import monitor.implementation.action.AlreadyRunningChecker;
import monitor.implementation.application.ApplicationViewBuilder;
import monitor.implementation.environment.RunningApplicationChecker;
import monitor.implementation.session.AllSessionPools;
import monitor.implementation.session.ServerSessionPool;
import monitor.implementation.session.Session;
import monitor.implementation.session.SessionEvent;
import monitor.model.CommandResult;
import monitor.model.CommandStatus;
import monitor.model.Configuration;
import monitor.model.LogonResult;
import monitor.model.Server;
import monitor.model.UpDownState;

/**
 * http://localhost:8080/MonitorScript/executeCommand?sessionId=278e83&command=ls%20-ltr%20&%20background
 */
@WebServiceProvider
@BindingType(HTTPBinding.HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
public class MonitorHttpProvider implements Provider<DataSource> {

	static Logger logger = Logger.getLogger(MonitorHttpProvider.class.getName());
	private static final boolean logFine = Configuration.getInstance().isLogFine();	
	private static final String fullPathToInstallDirectory = Configuration.getInstance().getFullPathToInstallDirectory();
	private static final String hostAndPort = Configuration.getInstance().getMonitorHostAndPort();
	private MonitorServiceImpl monitorServiceImpl;
	private AllSessionPools allSessionPools = AllSessionPools.getInstance();
	private EnvironmentViewDAO environmentViewDAO = EnvironmentViewDAO.getInstance();
	private RunningApplicationChecker runningApplicationChecker = new RunningApplicationChecker();
	private ApplicationViewBuilder applicationViewBuilder = new ApplicationViewBuilder();
	
	// Used to test data/scripts/discover-apps-001.py
	private String mockPSOutput =
		"echo devops    10620     1  0 Sep16 ?        00:06:31 /usr/java/jdk1.6.0_20/jre/bin/java -Xms1179m -Xmx1179m -XX:PermSize=32m -XX:MaxPermSize=64m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -XX:+UsePerfData -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -server -Dlog4j.defaultInitOverride=true -Dtomcat.keystore.type=JKS -Dsun.net.inetaddr.negative.ttl=0 -Dlog.lus.level=INFO -Dlog.default.level=INFO -Dlog.cxf.level=INFO -Dcom.sun.management.jmxremote.authenticate=false -Dlog.dir=/var/log/carrero-predictor -DuseLiveBalanceDao=true -Dlog.carrero.level=INFO -Dtomcat.truststore.type=JKS -Dtomcat.truststore.password=howmanybets -Dcom.sun.management.jmxremote.ssl=false -Djava.io.tmpdir=/var/app/carrero-predictortomcat/tmp -Dcatalina.base=/var/app/carrero-predictortomcat -Dlog.carrero.services.level=INFO -Dtomcat.http.port=8680 -Dtomcat.client.auth=true -Dlog.spring.level=INFO -Dconf.dir=/var/app/carrero-predictor/chef.env/conf -Dtangosol.coherence.log.level=2 -Dtomcat.truststore.file=/etc/carrero-truststores/carrero-truststore.jks -Dsun.net.inetaddr.ttl=600 -Dtangosol.coherence.cacheconfig=/var/app/carrero-predictor/common.env/conf/coherence-cache-config.xml -Dtomcat.keystore.password=howmanybets -Dlog.root.level=INFO -Dhostname=node365.test.carrero.es -Dcatalina.home=/var/app/carrero-predictortomcat -Dlog.date.pattern=.yyyy-MM-dd-HH -Dtomcat.https.port=8643 -Dlog.quartz.level=INFO -Dcom.sun.management.jmxremote.port=8426 -Dtomcat.shutdown.port=8405 -Dtangosol.coherence.log=log4j -Dtomcat.keystore.file=/etc/carrero-predictor/certs/classifier-service-server.jks -Dlog.apache.level=INFO -Dlog.httpclient.level=INFO -Dsun.net.client.defaultConnectTimeout=5000 -Dtomcat.maxThreads=150 -classpath /var/app/carrero-predictor/bootstrap/classes:/var/app/carrero-predictor/chef.env/conf:/var/app/carrero-predictor/common.env/conf:/var/app/carrero-predictorlib/bootstrap.jar:/var/app/carrero-predictorlib/tomcat-juli.jar org.apache.catalina.startup.Bootstrap start\n" +
		"echo devops    10917     1  0 Sep16 ?        00:05:57 /usr/java/jdk1.6.0_20/jre/bin/java -Xms393m -Xmx393m -XX:PermSize=32m -XX:MaxPermSize=64m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -XX:+UsePerfData -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -server -Dlog4j.defaultInitOverride=true -Dtomcat.keystore.type=JKS -Dlog.lus.level=INFO -Dsun.net.inetaddr.negative.ttl=0 -Dlog.default.level=INFO -Dlog.cxf.level=INFO -Dcom.sun.management.jmxremote.authenticate=false -Dlog.dir=/var/log/carrero-munger-service -Dtomcat.truststore.type=JKS -Dtomcat.truststore.password=howmanybets -Dlog.carrero.level=INFO -Dcom.sun.management.jmxremote.ssl=false -Djava.io.tmpdir=/var/app/carrero-munger-service/tmp -Dcatalina.base=/var/app/carrero-munger-service -Dlog.carrero.services.level=INFO -Dtomcat.http.port=8580 -Dtomcat.client.auth=true -Dlog.spring.level=INFO -Dconf.dir=/var/app/carrero-munger-service/chef.env/conf -Dtomcat.truststore.file=/etc/carrero-truststores/carrero-truststore.jks -Dsun.net.inetaddr.ttl=600 -Dtomcat.keystore.password=howmanybets -Dlog.root.level=INFO -Dhostname=node365.test.carrero.es -Dcatalina.home=/var/app/carrero-munger-service -Dlog.date.pattern=.yyyy-MM-dd-HH -Dtomcat.https.port=8543 -Dlog.quartz.level=INFO -Dcom.sun.management.jmxremote.port=8226 -Dtomcat.shutdown.port=8205 -Dtomcat.keystore.file=/etc/carrero-munger-service/certs/munger-service-server.jks -Dlog.apache.level=INFO -Dlog.httpclient.level=INFO -Dsun.net.client.defaultConnectTimeout=5000 -Dtomcat.maxThreads=150 -classpath /var/app/carrero-munger-service/bootstrap/classes:/var/app/carrero-munger-service/chef.env/conf:/var/app/carrero-munger-service/common.env/conf:/var/app/carrero-munger-servicelib/bootstrap.jar:/var/app/carrero-munger-servicelib/tomcat-juli.jar org.apache.catalina.startup.Bootstrap start\n" +
		"echo devops    16585     1  0 Sep16 ?        00:03:44 /usr/java/jdk1.6.0_20/jre/bin/java -Xms393m -Xmx393m -XX:PermSize=32m -XX:MaxPermSize=256m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -XX:+UsePerfData -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -Xloggc:/var/log/carrero-wax-service/node365.test.carrero.es-LDP-gc.log.20100916-1458.57 -server -Dcms.port=80 -Dlog4j.defaultInitOverride=false -DdomainsLocalhostXml.configFile=/var/app/carrero-wax-service/chef.env/conf/domains.localhost.xml -Dsun.net.inetaddr.negative.ttl=0 -Dlog.lus.level=INFO -Dtomcat.security.file=file:/var/app/carrero-wax-service/chef.env/conf/tomcat-security.properties -Dlog.default.level=INFO -Dmalicious-request.log.file=/var/log/carrero-wax-service/node365.test.carrero.es-malicious-request.log -Dlog.cxf.level=INFO -Dlog.dir=/var/log/carrero-wax-service -Dauth-failure.log.file=/var/log/carrero-wax-service/node365.test.carrero.es-auth-failure.log -Dcms.host=content -Dtomcat.truststore.password=howmanybets -Dtomcat.truststore.type=JKS -Dlog.carrero.level=INFO -Dcms.cdnHost=content-cache -Djava.io.tmpdir=/var/app/carrero-wax-service/tmp -Dcatalina.base=/var/app/carrero-wax-service -Dlog.carrero.services.level=INFO -Dtomcat.http.port=8080 -Dtomcat.client.auth=false -Dlog.spring.level=INFO -Dconf.dir=/var/app/carrero-wax-service/chef.env/conf -Dnetscaler.xip.header=X-IP -Dtomcat.truststore.file=/etc/carrero-truststores/carrero-truststore.jks -Dsun.net.inetaddr.ttl=600 -Dtomcat.keystore.password=howmanybets -Dlocal.conf.dir=/var/app/carrero-wax-service/chef.env/conf -Dservice.log.file=/var/log/carrero-wax-service/node365.test.carrero.es-service.log -Dlog.root.level=INFO -Dnetscaler.ssl.use=false -Dhostname=node365.test.carrero.es -Dcatalina.home=/var/app/carrero-wax-service -Dlog.date.pattern=.yyyy-MM-dd-HH -Dlog.quartz.level=INFO -Dtomcat.https.port=8443 -Dapp.name=LDP -Dnetscaler.ssl.header=Front-End-Https -Dlog4j.configuration=file:/var/app/carrero-wax-service/chef.env/conf/ldp-service-log4j.xml -Dtomcat.shutdown.port=8005 -Dnetscaler.xip.use=true -Dtomcat.keystore.file=/etc/carrero-wax-service/certs/ldp-service-server.jks -Dlog.apache.level=INFO -Dlog.httpclient.level=INFO -Dsun.net.client.defaultConnectTimeout=5000 -Dtomcat.maxThreads=150 -classpath /var/app/carrero-wax-service/bootstrap/classes:/var/app/carrero-wax-service/chef.env/conf:/var/app/carrero-wax-service/common.env/conf:/var/app/carrero-wax-servicelib/bootstrap.jar:/var/app/carrero-wax-servicelib/bootstrapped-tomcat-lib-6.0.20.1.1.1.jar:/var/app/carrero-wax-servicelib/tomcat-juli.jar bootstrapped.tomcat.StartTomcat start\n" +
		"echo devops    24208     1  0 Oct06 ?        00:02:23 /usr/java/jdk1.6.0_20/jre/bin/java -Xms256m -Xmx256m -XX:PermSize=32m -XX:MaxPermSize=128m -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+UseConcMarkSweepGC -Dcomponent.log.dir=/var/log/decision -Dcougar.log.level=WARNING -Dcom.carrero.homedir=/etc/decision -classpath /var/app/carrero-jce/bootstrap/lib/*:/etc/decision/conf:/var/app/decision/lib/* com.carrero.cougar.core.impl.Main\n" +
		"echo devops    26718     1  0 Sep30 ?        00:01:04 /usr/java/latest/bin/java -Xmx512M -Dorg.apache.activemq.UseDedicatedTaskRunner=true -Dcom.sun.management.jmxremote -Djava.security.auth.login.config=/var/app/carrero-fuse-activemq-1.0.0-1/fuse/conf/login.config -Dactivemq.classpath=/var/app/carrero-fuse-activemq-1.0.0-1/fuse/conf -Dactivemq.home=/var/app/carrero-fuse-activemq-1.0.0-1/fuse -Dactivemq.base=/var/app/carrero-fuse-activemq-1.0.0-1/fuse -jar /var/app/carrero-fuse-activemq-1.0.0-1/fuse/bin/run.jar start\n" +
		"echo devops    30918     1 41 15:37 ?        00:00:17 /usr/java/jdk1.6.0_20/jre/bin/java -Xms256m -Xmx256m -XX:PermSize=32m -XX:MaxPermSize=128m -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+UseConcMarkSweepGC -Dcomponent.log.dir=/var/log/carrero-uxb -Dcom.carrero.homedir=/etc/carrero-uxb -classpath /var/app/carrero-jce/bootstrap/lib/*:/etc/carrero-uxb/conf:/var/app/carrero-uxb/lib/* com.carrero.cougar.core.impl.Main\n" +
		"echo devops    26268     1  0 Oct06 ?        00:17:06 /usr/java/jdk1.6.0_18/bin/java -server -Dsun.net.client.defaultConnectTimeout=15000 -Dsun.net.client.defaultReadTimeout=15000 -Dtangosol.coherence.smt.enabled=true -XX:+UsePerfData -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1355 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.password.file=/var/carrero/karma/etc/karma-jmx/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/var/carrero/karma/etc/karma-jmx/jmxremote.access -Xmn128m -Xms900m -Xmx900m -XX:PermSize=256m -XX:MaxPermSize=512m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:MaxTenuringThreshold=0 -XX:+DisableExplicitGC -Dsun.rmi.dgc.server.gcInterval=600000 -Dlog.server.console.threshold=DEBUG -Dlog.server.file.threshold=DEBUG -Dlog.karma.system.threshold=DEBUG -Dlog.karma.application.threshold=DEBUG -Dlog.devops.level=INFO -Dlog.apache.level=INFO -Dlog.karma.level=INFO -Dlog.date.pattern=.yyyy-MM-dd-HH -Dorg.apache.cxf.Logger=org.apache.cxf.common.logging.Log4jLogger -Dtangosol.coherence.cacheconfig=/var/carrero/karma/classes/caching-factory-config.xml -Dtangosol.coherence.distributed.localstorage=true -Dtangosol.coherence.log.level=5 -Dtangosol.coherence.localhost=10.160.116.149 -Dtangosol.coherence.cacheconfig=/var/carrero/karma/classes/caching-factory-config.xml -Dtangosol.coherence.clusteraddress=224.1.23.244 -Dtangosol.coherence.clusterport=4143 -Dtangosol.coherence.ttl=1 -Djava.awt.headless=true -Dcom.carrero.homedir=/var/carrero -DjiveHome=/var/carrero/karma/etc/jiveHome -Dcom.carrero.platform.startservices=false -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false -Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE=true -Ddevops.server.home.dir=/var/carrero/karma/devops-4.0.4.GA/server/karma -Ddevops.server.log.dir=/var/log/decision-maker -Djava.io.tmpdir=/var/carrero/karma/devops-4.0.4.GA/server/karma/tmp -Dkarma.keystore.location=/etc/carrero-decisionweb/certs/decisionweb-server.jks -Dkarma.truststore.location=/etc/carrero-truststores/carrero-truststore.jks -Dkarma.keystore.password=howmanybets -Dkarma.truststore.password=howmanybets -Dkarma_hostname=node351.test.carrero.es -Dtomcat.port=8080 -Dtomcat.secure.port=8443 -Dcarrero.hostname=node351.test.carrero.es -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1355 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.password.file=/var/carrero/karma/etc/karma-jmx/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/var/carrero/karma/etc/karma-jmx/jmxremote.access -Dcipher.suite=SSL_RSA_WITH_RC4_128_MD5,SSL_RSA_WITH_RC4_128_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,SSL_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA -classpath /var/carrero/karma/classes:/var/carrero/karma/content:/var/carrero:/var/carrero/karma:/var/carrero/karma/devops-4.0.4.GA/bin/run.jar:/usr/java/jdk1.6.0_18/lib/tools.jar org.devops.Main -c karma\n" +
		"echo devops    15025     1  0 Sep17 ?        00:16:14 /usr/java/jdk1.5.0_08/bin/java -server -Dsun.net.client.defaultConnectTimeout=15000 -Dsun.net.client.defaultReadTimeout=15000 -Dtangosol.coherence.smt.enabled=true -XX:+UsePerfData -Xmn256m -Xms800m -Xmx800m -XX:PermSize=64m -XX:MaxPermSize=128m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:MaxTenuringThreshold=0 -XX:+DisableExplicitGC -Dsun.rmi.dgc.server.gcInterval=600000 -Dlog.server.console.threshold=OFF -Dlog.server.file.threshold=INFO -Dlog.karma.system.threshold=INFO -Dlog.karma.application.threshold=WARN -Dlog.devops.level=INFO -Dlog.apache.level=INFO -Dlog.karma.level=INFO -Dlog.watchdog.level=INFO -Dlog.bbm.system.level=INFO -Dlog.bbm.deltacache.system.level=INFO -Dlog.reporting.adapter.level=INFO -Dlog.startingprice.level=INFO -Dlog.startingprice.reconciliation.audit.level=INFO -Dlog.clientserver.level=INFO -Dlog.activemq.level=INFO -Dlog.date.pattern=.yyyy-MM-dd-HH -Dtangosol.coherence.clusteraddress=224.1.24.61 -Dtangosol.coherence.ttl=1 -Dtangosol.coherence.log.level=5 -Dtangosol.coherence.localhost=node38.test.carrero.es -Dtangosol.coherence.clusterport=4214 -Djava.awt.headless=true -Dcom.carrero.homedir=/var/carrero -DjiveHome=/var/carrero/karma/etc/jiveHome -Dcom.carrero.oracle.BetfairOracleDriver.statementCacheSize=255 -Dcom.carrero.platform.startservices=false -Ddevops.server.home.dir=/var/carrero/karma/jboss-4.0.5.GA/server/karma -Ddevops.server.log.dir=/var/carrero/karma/jboss-4.0.5.GA/server/karma/log -Djava.io.tmpdir=/var/carrero/karma/jboss-4.0.5.GA/server/karma/tmp -Dtomcat.port=8080 -Dtomcat.secure.port=8443 -Dcarrero.hostname=node38.test.carrero.es -classpath /var/carrero/karma/classes:/var/carrero/karma/jboss-4.0.5.GA/bin/run.jar:/usr/java/jdk1.5.0_08/lib/tools.jar org.devops.Main -c karma";

	
    @Resource
    protected WebServiceContext wsContext;

	
	private class OutputDataSource implements DataSource {
		private String output;
		private String contentType;
		public OutputDataSource(String output, String contentType) {
			this.output = output;
			this.contentType = contentType;
		}
		public InputStream getInputStream() {
			return new ByteArrayInputStream(output.getBytes());
		}
		public OutputStream getOutputStream() {
			return null;
		}
		public String getContentType() {
			return contentType;  // "text/plain";
		}
		public String getName() {
			return "";
		}
	};

    public MonitorHttpProvider(MonitorServiceImpl monitorServiceImpl) {
		this.monitorServiceImpl = monitorServiceImpl;
	}

	public OutputDataSource invoke(DataSource ds) {
        MessageContext mc = wsContext.getMessageContext();
        String method = (String)mc.get(MessageContext.HTTP_REQUEST_METHOD);
        if (method.equals("GET")) {
            return get(ds, mc);
		}
        HTTPException ex = new HTTPException(404);
        throw ex;
    }
  
    private OutputDataSource get(DataSource source, MessageContext mc) {
        String path="", queryString="", output="";
        String contentType = "text/plain";
        try {
            path = (String)mc.get(MessageContext.PATH_INFO);
            String encodedQueryString = (String)mc.get(MessageContext.QUERY_STRING);
            if (encodedQueryString != null) {
            	queryString = URLDecoder.decode((String)mc.get(MessageContext.QUERY_STRING), "UTF-8");	
            }

            if (logFine) logger.info(String.format("Request: %s?%s", path, queryString));
            if ("/logon".equalsIgnoreCase(path)) {
            	output = logon(queryString);
            } else if ("/executeCommand".equalsIgnoreCase(path)) {
            	output = executeCommand(queryString);
            } else if ("/addApplication".equalsIgnoreCase(path)) {
            	output = addApplication(queryString);
            } else if ("/applicationIsUp".equalsIgnoreCase(path)) {
            	output = applicationIsUp(queryString);
            } else if ("/finishedHeartbeat".equalsIgnoreCase(path)) {
            	output = finishedHeartbeat(queryString);
            } else if ("/getAllApplications".equalsIgnoreCase(path)) {
            	output = applicationViewBuilder.getAllApplications();
            } else if ("/getEnvironmentApplications".equalsIgnoreCase(path)) {
            	output = getEnvironmentApplications(queryString);
            } else if ("/getAlreadyRunningCache".equalsIgnoreCase(path)) {
            	output = getAlreadyRunningCache();
            } else if ("/close".equalsIgnoreCase(path)) {
            	output = close(queryString);
            } else if ("/sessionHistory".equalsIgnoreCase(path)) {
            	contentType = "text/html";
            	output = sessionHistory(queryString);            	
            } else if ("/get".equalsIgnoreCase(path)) {
            	output = get(queryString);
            	if (path.endsWith(".html") || output.startsWith("<html>") || output.startsWith("<HTML>")) {
            		contentType = "text/html";
            	}
            } else if ("/dumpAllSessionsOnAllServers".equalsIgnoreCase(path)) {
            	contentType = "text/html";
            	output = dumpAllSessionsOnAllServers();
            } else if ("/writeToServerLog".equalsIgnoreCase(path)) {
            	output = writeToServerLog(queryString);
            } else if ("/getHostName".equalsIgnoreCase(path)) {
            	output = getHostName(queryString);
            } else {
            	String indexPage = get("index.html");
            	String withLinks = indexPage.replaceAll("host-port", hostAndPort);
            	if (path.startsWith("/index") || path.startsWith("/help") || path.startsWith("/documentation")) {
            		output = withLinks;
            	} else {
                	String error = String.format("<FONT SIZE=+3 COLOR=\"red\">404 Not found: %s?%s</FONT>", path, queryString);
                	String withError = withLinks.replaceFirst("<!--ERROR-->", error);
                	output = withError;
            	}
            	contentType = "text/html";
            }
		} catch (Exception e) {
			output = String.format("Exception processing request: '%s?%s'\nException: %s ", path, queryString, StackTraceFormatter.asString(e));
		}
        //    HTTPException ex = new HTTPException(404);
		return new OutputDataSource(output, contentType);
	}


	private String sessionHistory(String queryString) {
		StringBuilder page = new StringBuilder(get("sessionHistory.html"));
		Session session = allSessionPools.getSessionUsingSessionId(queryString);
		if (session == null) {
			String replacement = allSessionPools.findReplacementSessionId(queryString);
			session = allSessionPools.getSessionUsingSessionId(replacement);
			if (session == null) {
				return String.format("sessionId %s not found.\n", queryString);
			}
		}
		for (SessionEvent sessionEvent : session.getSessionHistory().getAll()) {
			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss:SSS");
		    page.append("<tr>\n");
		    page.append("<td>" + format.format(new Date(sessionEvent.getTime())) + "</td>\n");
		    page.append("<td>" + sessionEvent.getSessionID() + "</td>\n");
		    page.append("<td>" + sessionEvent.getShortDesc() + "</td>\n");
		    page.append("<td>" + sessionEvent.getLongDesc() + "</td>\n");
		    page.append("<td>" + String.format("%-5b %-5b %-5b %-8s", sessionEvent.isLoggedOn(), sessionEvent.isOpen(), sessionEvent.isControlSession(), sessionEvent.getSessionType() ) + "</td>\n");
		    page.append("<td>" + format.format(new Date(sessionEvent.getLastUsed())) + "</td>\n");
		    page.append("</tr>\n");				
		}
		page.append("</table></body></html>");
		return page.toString();
	}

	private String dumpAllSessionsOnAllServers() {
		StringBuilder dumpPage = new StringBuilder(get("dumpSessions.html"));
		ArrayList<String> servers = new ArrayList<String>();
		for (String server :allSessionPools.getAllServerSessionPools().keySet()) {
			servers.add(server);
		}
		Collections.sort(servers);
		for (String server : servers) {
			ServerSessionPool serverSessionPool = allSessionPools.getServerSessionPool(new Server(server));
			for (Session session : serverSessionPool.getSessions()  ) {
			    dumpPage.append("<tr>\n");
			    dumpPage.append("<td><a href=\"http://" + hostAndPort + "/MonitorScript/sessionHistory?" + session.getSessionId() + "\">" + session.getSessionId() + "</a></td>\n");
			    dumpPage.append("<td>" + session.getLoggedOnUserName() + "</td>\n");
			    dumpPage.append("<td>" + session.getServer().getHost() + "</td>\n");
			    dumpPage.append("<td>" + String.format("%-5b %-5b %-5b %-8s %-8s", session.isLoggedOn(), session.isOpen(), session.isControlSession(), session.getLastCommandStatus(), session.getSessionType() ) + "</td>\n");
			    dumpPage.append("<td>" + session.getRunningCommand() + "</td>\n");
			    dumpPage.append("<td>" + session.getCalledBy() + "</td>\n");
			    dumpPage.append("</tr>\n");				
			}
		}
		dumpPage.append("</table></body></html>");
		return dumpPage.toString();
	}

	private String get(String queryString) {
		File file = new File(fullPathToInstallDirectory + "/" + queryString);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			return e.getMessage();
		}
		int len = 0;
		char[] cbuf = new char[8192];
		try {
			len = reader.read(cbuf);
		} catch (IOException e) {
			return e.getMessage();
		}
		return new String(cbuf, 0, len);
	}

	
	private String logon(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"host"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameters " + missingParametersError + " in query string: " + decodedQueryString;
		}
		String host = parser.getParameterValue("host");
		String environmentName = parser.getParameterValue("environmentName");
		LogonResult logonResult = monitorServiceImpl.logon(host, host, environmentName);
		if (logonResult.getCommandStatus() == CommandStatus.ERROR) {
			return logonResult.getErrorMessage();
		} else {
			return logonResult.getSessionId();
		}
	}	
	
	
	private String executeCommand(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"sessionId", "command"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameters " + missingParametersError + " in query string: " + decodedQueryString;
		}
		String sessionId = parser.getParameterValue("sessionId");
		String command = parser.getParameterValue("command");
		
		if ("mock-ps-command".equals(command)) {
			command = "echo " + mockPSOutput;
		}
		
		Session session = allSessionPools.getSessionUsingSessionId(sessionId);
		if (session == null) {
			return String.format("sessionId %s not found.\n", sessionId);
		}
		CommandResult commandResult = monitorServiceImpl.executeCommand(command, sessionId);
		String output = commandResult.getOutput();
		if (logFine) logger.info(output.length() > 120 ? output.substring(0, 120) + "..." : output);
		return output;
	}
	
	private String addApplication(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"sessionId", "nameInEnvironmentView", "fileName"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameters " + missingParametersError + " in query string: " + decodedQueryString;
		}
		
		String sessionId = parser.getParameterValue("sessionId");
		String nameInEnvironmentView = parser.getParameterValue("nameInEnvironmentView");
		String fileName = parser.getParameterValue("fileName");
		
		CommandResult commandResult = monitorServiceImpl.addApplication(sessionId, nameInEnvironmentView, fileName, null);
		logger.info(commandResult.getOutput());		
		return commandResult.getOutput();
	}

	private String applicationIsUp(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"sessionId", "nameInEnvironmentView"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameters " + missingParametersError + " in query string: " + decodedQueryString;
		}
		String sessionId = parser.getParameterValue("sessionId");
		String nameInEnvironmentView = parser.getParameterValue("nameInEnvironmentView");
		Session session = allSessionPools.getSessionUsingSessionId(sessionId);
		if (session == null) {
			return String.format("sessionId %s not found.\n", sessionId);
		}
		environmentViewDAO.changeApplicationUpDownState(session.getEnvironmentName(), session.getServer().getHost(), nameInEnvironmentView, UpDownState.UP);
		return "OK " + nameInEnvironmentView + " is up.";
	}

	private String finishedHeartbeat(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"sessionId"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameters " + missingParametersError + " in query string: " + decodedQueryString;
		}
		String sessionId = parser.getParameterValue("sessionId");
		Session session = allSessionPools.getSessionUsingSessionId(sessionId);
		String result = null;
		if (session != null) {
			result = runningApplicationChecker.finishedHeartbeat(session.getEnvironmentName(), session.getServer().getHost());
			logger.info(String.format("%s - on '%s' server '%s'.", result, session.getEnvironmentName(), session.getServer().getHost()));
			session.close("MonitorHttpProvider.finishedHeartbeat");
		} else {
			result = String.format("sessionId %s not found.\n", sessionId);
		}
		return result;
	}
	
	private String getEnvironmentApplications(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"sessions"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameter " + missingParametersError + " in query string: " + decodedQueryString;
		}
		String sessions = parser.getParameterValue("sessions");
		return applicationViewBuilder.getEnvironmentApplications(sessions);
	}

	
	
	private String close(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"sessionId"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameters " + missingParametersError + " in query string: " + decodedQueryString;
		}
		String sessionId = parser.getParameterValue("sessionId");
		Session session = allSessionPools.getSessionUsingSessionId(sessionId);
		logger.info(String.format("closing  %s", session));		
		if (session != null) {
			session.close("MonitorHttpProvider.close");
		} else {
			return String.format("sessionId %s not found.\n", sessionId);
		}
		
		return "session " + sessionId + " closed";
	}

	private String writeToServerLog(String decodedQueryString) {
		// the get method already writes the request to the log when logFine
        if (!logFine) logger.info(decodedQueryString);
		return "OK";
	}

	private String getAlreadyRunningCache() {
		return AlreadyRunningChecker.getInstance().getAlreadyRunningCache();
	}	
	
	private String getHostName(String decodedQueryString) {
		HttpQueryStringParser parser = new HttpQueryStringParser(decodedQueryString);
		String missingParametersError = parser.missingRequiredParameters(new String[] {"sessionId"});
		if (missingParametersError.length() > 0) {
			return "Did not find parameters " + missingParametersError + " in query string: " + decodedQueryString;
		}
		String sessionId = parser.getParameterValue("sessionId");
		Session session = allSessionPools.getSessionUsingSessionId(sessionId);
		String result = null;
		if (session != null) {
			result = session.getServer().getHost();
		} else {
			result = String.format("sessionId %s not found.\n", sessionId);
		}
		return result;
	}
	
}
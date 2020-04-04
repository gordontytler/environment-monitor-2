To run the client 
=================

cd environment-monitor/environment-monitor-client
java -jar ./bin/MonitorClient.jar http://127.0.0.1:8085/Monitor

To create the jar file
======================

cd environment-monitor-client
cd bin
jar cmfv META-INF/MANIFEST.MF  MonitorClient.jar .

To zip the client
=================

zip -r environment-monitor-client.zip environment-monitor-client

To generate client service proxy
================================

See http://java.sun.com/webservices/docs/2.0/tutorial/doc/index.html  here is the web service import command

environment-monitor-client$ 

wsimport -keep -d ./bin/ -s ./src-generated/ ../environment-monitor/src-generated/MonitorService.wsdl 
parsing WSDL...


generating code...


compiling code...


To start the server
===================
java -Djava.util.logging.config.file=./logging.properties -classpath ./bin monitor.api.Main


To generate code for the SOAP service from annotations in MonitorServiceImpl
run this in environment-monitor directory 

wsgen -cp ./bin/ -keep -d ./bin/ -s ./src-generated/ -r ./src-generated/ -wsdl monitor.api.MonitorServiceImpl



To generate code for the SOAP service from annotations in MonitorServiceImpl
============================================================================

run this in environment-monitor directory 

wsgen -cp ./bin/ -keep -d ./bin/ -s ./src-generated/ -r ./src-generated/ -wsdl monitor.api.MonitorServiceImpl

To generate the client proxy classes to use the web service 
===========================================================

see ../environment-monitor-client/src/readme.txt


To run the server from command line
===================================

Ubuntu:

java -Djava.util.logging.config.file=./logging.properties -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n -classpath ./bin:./lib/ganymed-ssh2-build251beta1.jar monitor.api.Main &>> ./log/stdout.log  &

Centos:

java -Djava.util.logging.config.file=./logging.properties -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n -classpath ./bin:./lib/ganymed-ssh2-build251beta1.jar monitor.api.Main >> ./log/stdout.log  &

if you need to specify which java to use because the $PATH is wrong

/usr/java/jdk1.6.0_24/bin/java -Djava.util.logging.config.file=./logging.properties -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8797,server=y,suspend=n -classpath ./bin:./lib/ganymed-ssh2-build251beta1.jar monitor.api.Main >> ./log/stdout.log  &


To write random test entries to a log file
==========================================

java -Dlog.dir=/var/log/decision -classpath ./bin monitor.implementation.shell.LogFileWriterApplication &


To deploy on a server
=====================
on the remote server

cd /var/app
rm environment-monitor.zip
rm -r environment-monitor

on the local PC, cd to the directory containing the top level environment-monitor directory 

rm -r environment-monitor.zip
zip -r environment-monitor.zip environment-monitor
scp /home/tytlerg/environment-monitor.zip devops@node425.test.carrero.es:/var/app/environment-monitor.zip

back on the remote

unzip environment-monitor.zip
vi environment-monitor/environment-monitor/config.properties
FullPathToInstallDirectory=/var/app/environment-monitor/environment-monitor

cd /var/app/environment-monitor/environment-monitor
# the command to run the application is above in: To run the server from command line

test with this 

http://node425.test.carrero.es:8084/MonitorScript/get?config.properties


To fix permissions
==================


cd /var/app
chmod -R g+rwx environment-monitor


To run tests
============
You might need to install openssh-server and generate your public/private rsa key pair.
You need to enable ssh login and allow login using public key.
see the comment in ApplicationViewBuilderTest.testGetEnvironmentApplications

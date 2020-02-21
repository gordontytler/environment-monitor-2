# environment-monitor
ssh based zero client server monitor with discovery visualisation and automation

It does what a developer might do. It logs on to a server, checks the running processes and looks for errors in the log files. It has a rest web service and browser console and a Java swing graphical representation of the log files. You see error, warning and info as scrolling patterns of red orange and green. One monitor screen might show an environment, such as user acceptance test. The will be a normal pattern of scrolling graphics but when anything is wrong, the human will instantly see. A network error or database problem produce entirely different patterns.

When it logs on, if it doesn't have one, it will create a new account so as to not fill up command history. Then it will either use the existing config (on a central server) or will discover applications e.g. a jboss or spring boot and start monitoring their log files. 

It is written in java and python. There is no client. The ssl is written in java for performance. It has a rest and soap web service with browser console that can run a commands on servers e.g. a healthcheck script. 


Here is the config file 

#Created from defaults. To recreate delete this file and run monitor.model.ConfigurationTest
#Sun Dec 09 12:34:20 GMT 2019
ApplicationHeartbeatMillis=32000
ChunkedOutputArraySize=200
ClosedSessionTestFrequencyMillis=125000
DefaultCommandTimeoutMillis=1000
FullPathToInstallDirectory=/home/gordon/environment-monitor/environment-monitor
LogFine=false
LogFine.InputFromSSHReader.filter=false
MaximumServerSessions=20
MaximumTerminalsPerSSHConnection=10
MinutesToWaitAfterFailedLogin=10
MonitorPort=8084
SessionLoginTimeoutSeconds=1
TestCommandTimeoutMillis=200
UnusedMillisBeforeCloseOfFinishedActionSession=67000
UnusedMinutesBeforeClosingSession=4
UnusedMinutesBeforeLogoutSession=10
action.already.running.kill=true
action.already.running.run.another=true
user.auto=auto
user.auto.create=true
user.auto.password=iY7UkS05R8UHtwzbqxscBw\=\=
user.auto.tryFirst=true
user.default=jboss
user.default.password=iY7UkS05R8UHtwzbqxscBw\=\=
user.default.password.2=ihtJmup0aRZ8VJkqQBT7KwHr1ewQ7U2e
user.gordon-netbook=gordon
user.gordon-netbook-2=gordon
user.gordon-netbook.password=iY7UkS05R8UHtwzbqxscBw\=\=
user.tytlergubuntu01=tytlerg
user.tytlergubuntu01-2=tytlerg
user.tytlergubuntu01.password=6paptXZlNI8VQU6Dc3/wSA\=\=


Here is the readme.txt


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

java -Dlog.dir=/var/log/bf-cas -classpath ./bin monitor.implementation.shell.LogFileWriterApplication &


To deploy on a server
=====================
This is the monitor server that has all the config and scripts and provides the web service. It logs on to other servers to view their processes and log files.

cd /var/app
rm environment-monitor.zip
rm -r environment-monitor

on the local PC, cd to the directory containing the top level environment-monitor directory 

rm -r environment-monitor.zip
zip -r environment-monitor.zip environment-monitor
scp /home/gordon/environment-monitor.zip user@monitorserver:/var/app/environment-monitor.zip

back on the remote

unzip environment-monitor.zip
vi environment-monitor/environment-monitor/config.properties
FullPathToInstallDirectory=/var/app/environment-monitor/environment-monitor

cd /var/app/environment-monitor/environment-monitor
# the command to run the application is above in: To run the server from command line

test with this 

http://monitorserver:8084/MonitorScript/get?config.properties


To fix permissions
==================


cd /var/app
chmod -R g+rwx environment-monitor


Here is the Python to discover what application are running.

#! /usr/bin/env python

import httplib, urllib, sys, monitorrestclient, application

# Discovers which applications are running on a server.
#
# Calls back to the environment monitor to get the output from the 'command': "ps -ef | grep j[a]va"
# The returned list of processes is examined and when an application is found it notifies environment
# monitor telling it the application name to add and the file which defines its monitoring outputs.
#
# When parameter three is 'heartbeat' it tells the monitor that the application is running.


# useMockPsOutput makes the environment monitor returns fake processes
useMockPsOutput = False
testingHeartbeat = False
printtrace = False

def is_digits_and_commas(s):
    return len([x for x in s if x.isdigit() or x==',']) == len(s) and len(s) > 0

def check_usage():
    if len(sys.argv) < 3 or not is_digits_and_commas(sys.argv[2]) :
        print """
Usage: python discover001.py monitor-url sessionId [option]
 monitor-url  URL of the environment monitor REST web service
 sessions     comma separated list of ssh sessionIds used in calls to the web service to indicate a remote server
 option       when set to "heartbeat" environment monotor is notified which applications are still running
e.g. 

python discoverApps001.py 127.0.0.1:8084 14,15,16
 
"""
        exit()

def discover_var_log_apps(rest, sessionId, option, procs):
    genericAppFileName = "applications/generic-application-001.txt"
    print 'looking for processes with log.dir=/var/log/\n'
    i = 0
    while i < len(procs):
        logDirStart = procs[i].find("log.dir=/var/log/")
        if logDirStart > 0:
            logDirEnd = logDirStart + len("log.dir=/var/log/")
            nextSpace = procs[i].find(" ", logDirEnd)
            nameInEnvironmentView = procs[i][logDirEnd:nextSpace]
            found_application(nameInEnvironmentView, genericAppFileName, option, rest, sessionId)
            del procs[i]
        else:
            i = i + 1

def found_application(nameInEnvironmentView, fileName, option, rest, sessionId):
    print 'found ' + nameInEnvironmentView
    if option == 'heartbeat' or testingHeartbeat:
        reply = rest.application_is_up(sessionId, nameInEnvironmentView)
    else:
        reply = rest.add_application(sessionId, nameInEnvironmentView, fileName)
    print reply


def second_column(procs):
    return str([p.split()[1] for p in procs if len(p.split()) > 1 and  p.split()[1].isdigit()])


def discover_apps(rest, monitorURL, sessionId, apps, option):
    if useMockPsOutput:
        command = 'mock-ps-command'
    else:
        command = 'ps -ef | grep j[a]va'
    lines = rest.execute_command(sessionId, command)
    print '\nGot these processes using sessionId ' + sessionId + '\n' + lines
    if printtrace: rest.write_to_server_log('got this process list\n' + lines)
    procs = lines.strip().split('\n')
    i = 0
    while i < len(procs):
        print '\nchecking element', i, 'remaining len(procs) is', len(procs)
        for a in apps:
            if i < len(procs):
                found = False
                print 'checking if', a.nameInEnvironmentView, 'is process', second_column([procs[i]])
                if a.sessionId == sessionId or a.sessionId == '0':
                    if a.matches(rest, procs[i], sessionId):
                        found_application(a.nameInEnvironmentView, a.fileName, option, rest, sessionId)
                        del procs[i]
                        found = True
                        break
        if not found:
            i = i + 1
    discover_var_log_apps(rest, sessionId, option, procs)
    hostName = rest.get_host_name(sessionId)
    message = hostName + ' ' + str(len(procs)) + ' processes were not identified: ' + second_column(procs) + '\n'

    # only close the session for heartbeat because the output from discover apps session is displayed in a window
    if option == 'heartbeat' and not testingHeartbeat:
        rest.finished_heartbeat(sessionId)
    return message

def discover_apps_all_servers(monitorURL, sessionsArg, option):
    #option = 'heartbeat'
    if option == 'heartbeat':
        taskName = 'heartbeat'
    else:
        taskName = 'Discover applications'
    rest = monitorrestclient.MonitorRestClient(monitorURL)
    print 'Looking for these applications: '
    apps = application.get_applications(rest, sessionsArg, option)
    summary = '\n' + taskName + ' result\n\n'
    if len(apps) > 0:
        sessions = sessionsArg.split(',')
        for i in range(len(sessions)):
            result = discover_apps(rest, monitorURL, sessions[i], apps, option)
            summary = summary + result
            rest.write_to_server_log(taskName + ' for session: ' + sessions[i] + ' ' + result + '_' * 80)
    print summary
    rest.close()

if __name__ == '__main__':
    check_usage()
    option = ""
    print 'in __main__'
    if len(sys.argv) > 3 :
        option = sys.argv[3]
    discover_apps_all_servers(sys.argv[1], sys.argv[2], option)
    exit()

#! /usr/bin/env python

import httplib, urllib, sys, time, monitorrestclient, application

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

def discover_var_app_apps(rest, sessionId, option, procs):
    genericAppFileName = "applications/generic-application-var-app-001.txt"
    print 'looking for processes with log.dir=/var/app/${applicationName}tomcat/logs/\n'
    i = 0
    while i < len(procs):
        logDirStart = procs[i].find("log.dir=/var/app/")
        if logDirStart > 0:
            logDirEnd = logDirStart + len("log.dir=/var/app/")
            nextSpace = procs[i].find("tomcat/logs ", logDirEnd)
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
        command = 'ps -ef | grep j[a]va | grep -v grep'
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
                        found_application(a.nameIfMatched, a.fileName, option, rest, sessionId)
                        del procs[i]
                        found = True
                        break
        if not found:
            i = i + 1
    discover_var_log_apps(rest, sessionId, option, procs)
    discover_var_app_apps(rest, sessionId, option, procs)
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
    print ''
    time.sleep(1) # some bug looses the initial output
    print '\nLooking for these applications: '
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
    print '\nFinished.'

if __name__ == '__main__':
    check_usage()
    option = ""
    print 'in __main__'
    if len(sys.argv) > 3 :
        option = sys.argv[3]
    discover_apps_all_servers(sys.argv[1], sys.argv[2], option)
    exit()

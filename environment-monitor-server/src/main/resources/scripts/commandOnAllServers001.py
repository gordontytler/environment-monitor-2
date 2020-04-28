#! /usr/bin/env python

import time, httplib, urllib, sys, monitorrestclient, application

# Runs a command on all servers.
#

def is_digits_and_commas(s):
    return len([x for x in s if x.isdigit() or x==',']) == len(s) and len(s) > 0

def check_usage():
    if len(sys.argv) < 4 or not is_digits_and_commas(sys.argv[2]) :
        print """
Usage: python commandOnAllServers001.py monitor-url sessions "command"
 monitor-url  URL of the environment monitor REST web service
 sessions     comma separated list of ssh sessionIds used in calls to the web service to indicate a remote server
 command      the command delimited by quotes and bash substituted characters escaped e.g. "echo \~/ \$HOSTNAME \$\$"
e.g. 

python commandOnAllServers001.py 127.0.0.1:8084 14,15,16 "sudo sed 's/umask 077/umask 022/g' /etc/profile"

"""
        exit()

def second_column(procs):
    return str([p.split()[1] for p in procs if len(p.split()) > 1 and  p.split()[1].isdigit()])

def command_all_servers(monitorURL, sessionsArg, command):
    print ''
    time.sleep(1) # some bug looses the first output
    print '\narguments: ', sys.argv[1:], '\n'
    rest = monitorrestclient.MonitorRestClient(monitorURL)
    sessions = sessionsArg.split(',')
    for i in range(len(sessions)):
        host = rest.get_host_name(sessions[i])
        result = rest.execute_command(sessions[i], command)
        print host + '$', result.strip()
    print '\nFinished.'
    rest.close()

if __name__ == '__main__':
    check_usage()
    print 'in __main__'
    command_all_servers(sys.argv[1], sys.argv[2], sys.argv[3])
    exit()

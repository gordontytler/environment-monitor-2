#! /usr/bin/env python

import monitorrestclient, application

rc = monitorrestclient.MonitorRestClient()

print '\ntest monitorrestclient.write_to_server_log'
print rc.write_to_server_log('this message is from testrest.py')

print '\ntest monitorrestclient.execute_command'
print rc.execute_command(-123, 'whoami')

print '\ntest monitorrestclient.get_all_applications'
print rc.get_all_applications()

"""To test this select Server->Open Terminal to get the sessionIds """
sessionsArg = '420,423'

print '\ntest monitorrestclient.get_environment_applications'
print rc.get_environment_applications(sessionsArg)

print '\ntest applicaton.get_applications'
apps = application.get_applications(rc, sessionsArg, 'discover')
for a in apps: print a.toList()

print '\ntest applicaton.matches'
apps = application.get_applications(rc, sessionsArg, 'heartbeat')
procs = ['eclipse','monitor.gui.MainFrame']
for s in sessionsArg.split(','):
	for p in procs:
		for a in [app for app in apps if app.sessionId == s or app.sessionId == 0]:
			print s, a.nameInEnvironmentView, a.matches(rc, p, s)




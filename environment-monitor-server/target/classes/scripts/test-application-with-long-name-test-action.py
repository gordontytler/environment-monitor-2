#! /usr/bin/env python

import httplib, urllib, sys

# This test is an example of Python using http to ask
# environment-monitor to run a command on the remote machine.
#
# sys.argv[1]  URL of the environment monitor
# sys.argv[2]  sessionId the  ssh 
#
params = urllib.urlencode({'sessionId': sys.argv[2], 'command': "sh logger.sh"})
conn = httplib.HTTPConnection(sys.argv[1])
conn.request("GET", "/MonitorScript/executeCommand?" + params)
# The commands output will already be in the session's output. There is no need to duplicate it here.
print conn.getresponse().read()
print "python printed the previous output from http://" + sys.argv[1] + "/MonitorScript/executeCommand?" + params
print "\nFinished."
conn.close()
exit()

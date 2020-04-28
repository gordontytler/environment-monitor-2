#! /usr/bin/env python

import httplib, urllib, sys

# sys.argv[1]  URL of the environment monitor
# sys.argv[2]  sessionId the  ssh 
#
#
# TODO display usage message if sessionId parameter missing
params = urllib.urlencode({'sessionId': sys.argv[2], 'command': "sh logger.sh"})
#conn = httplib.HTTPConnection("localhost:8085")
conn = httplib.HTTPConnection(sys.argv[1])
conn.request("GET", "/MonitorScript/executeCommand?" + params)
# The commands output will already be in the session's output. There is no need to duplicate it here.
print conn.getresponse().read()
print "output printed by the python script is merged with the remote session output"
conn.close()
exit()

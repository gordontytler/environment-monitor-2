#! /usr/bin/env python

import httplib, urllib

params = urllib.urlencode({'spam': 1, 'eggs': 2, 'bacon': 0, 'command':"ps -ef | grep java ; cd /usr/java/jdk <~>"})
conn = httplib.HTTPConnection("localhost:8080")
conn.request("GET", "/MonitorPython/" + params)
print conn.getresponse().read()
conn.close()
exit()

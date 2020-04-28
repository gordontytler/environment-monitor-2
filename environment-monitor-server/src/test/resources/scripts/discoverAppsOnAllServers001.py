#! /usr/bin/env python

import discoverApps001, sys

# This is necessary because scripts with "OnAllServers" in their file name will have 
# a comma separated list of sessionId passed to the second parameter.
#
# Can't just rename discoverApps001.py because environmentMonitor doesnt pass the last parameter.
# A side effect is the bytecode for the module will load quicker.


discoverApps001.discover_apps_all_servers(sys.argv[1], sys.argv[2], "discover")
#discoverApps001.discover_apps_all_servers(sys.argv[1], sys.argv[2], "heartbeat")



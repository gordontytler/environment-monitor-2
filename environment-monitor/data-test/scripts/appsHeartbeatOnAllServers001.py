#! /usr/bin/env python

import discoverApps001, sys

# Calls discoverApps with an extra parameter causing it to reply to caller with applicationIsUp messages.
# The second parameter is treated as a comma separated list of sessionId

discoverApps001.discover_apps_all_servers(sys.argv[1], sys.argv[2], "heartbeat")



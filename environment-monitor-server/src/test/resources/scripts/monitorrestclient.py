#! /usr/bin/env python

import httplib, urllib

class MonitorRestClient:

	"http interface to the environment monitor."

	def __init__(self, monitorURL='localhost:8084', context='/MonitorScript/'):
		self.monitorURL = monitorURL
		print 'monitorURL: ', monitorURL
		self.conn = httplib.HTTPConnection(monitorURL)
		self.context = context

	def execute_command(self, sessionId, command):
		params = urllib.urlencode({'sessionId': sessionId, 'command': command})
		self.conn.request("GET", self.context + "executeCommand?" + params)
		return self.conn.getresponse().read()

	def get_all_applications(self):
		self.conn.request("GET", self.context + "getAllApplications?")
		return self.conn.getresponse().read()

	def get_environment_applications(self, sessionsArg):
		"""takes a comma separated list of sessionIds and returns the applications for each """
		def is_digits_and_commas(s):
			return len([x for x in s if x.isdigit() or x==',']) == len(s) and len(s) > 0
		if not is_digits_and_commas(sessionsArg): raise Exception('sessionsArg should be comma separated string of numbers but was ' + "'" + sessionsArg + "'")
		self.conn.request("GET", self.context + "getEnvironmentApplications?sessions=" + sessionsArg)
		return self.conn.getresponse().read()

	def add_application(self, sessionId, nameInEnvironmentView, fileName):
		params = urllib.urlencode({'sessionId': sessionId, 'nameInEnvironmentView': nameInEnvironmentView,'fileName':fileName})
		self.conn.request("GET", self.context + "addApplication?" + params)
		return self.conn.getresponse().read()

	def application_is_up(self, sessionId, nameInEnvironmentView):
		params = urllib.urlencode({'sessionId': sessionId, 'nameInEnvironmentView': nameInEnvironmentView})
		self.conn.request("GET", self.context + "applicationIsUp?" + params)
		return self.conn.getresponse().read()

	def finished_heartbeat(self, sessionId):
		self.conn.request("GET", self.context + "finishedHeartbeat?sessionId=" + sessionId)
		return self.conn.getresponse().read()

	def write_to_server_log(self, message):
		print message
		params = urllib.urlencode({'message': message})
		self.conn.request("GET", self.context + "writeToServerLog?" + params)
		return self.conn.getresponse().read()

	def get_host_name(self, sessionId):
		self.conn.request("GET", self.context + "getHostName?sessionId=" + sessionId)
		return self.conn.getresponse().read()

	def close(self):
		self.conn.close()

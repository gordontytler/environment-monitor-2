# This initial Dockerfile creates an image based on openkjdk and just copies in the maven output.
#
#  to build and start the container
#  ================================
#
#  mvn install -Dmaven.test.skip=true
#  docker build -t environment-monitor-server .
#  docker run -dp 8084:8084 environment-monitor-server bash -c "java -Djava.util.logging.config.file=./classes/logging.properties -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n -classpath ./classes:./ganymed-ssh2-262.jar monitor.api.Main"
#
#  to test (host is in config.properties)
#  ======================================
#  try these URL on your local host  e.g.  InetAddress.getLocalHost().getHostName()
#
#  http://gordon-hp-notebook:8084/MonitorScript/get?config.properties
#  http://gordon-hp-notebook:8084/MonitorScript/getAllApplications
#
#  you will see an error
#  todo get it working in container
#
#  to stop container and remove image
#  ==================================
#
#  docker ps
#  docker stop <the-container-id>
#  docker rm <the-container-id>

FROM openjdk:8
COPY ./target /tmp
COPY ./lib/ganymed-ssh2-262.jar /tmp
WORKDIR /tmp

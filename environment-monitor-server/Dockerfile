# This initial Dockerfile creates an image based on openkjdk and just copies in the maven output.
#
#  to build and start the container
#  ================================
#
#  mvn install -Dmaven.test.skip=true
#  docker build -t environment-monitor-server .
#  docker run -dp 8084:8084 environment-monitor-server bash -c "java -Djava.util.logging.config.file=./logging.properties -DDataDirectory=./ -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n -classpath ./:./ganymed-ssh2-262.jar monitor.api.Main"
#
#  to test
#  =======
#  Try these URL on your local host. You need the host name, not localhost.  e.g.  InetAddress.getLocalHost().getHostName()
#
#  http://gordon-hp-notebook:8084/MonitorScript/get?config.properties
#  ps
#
#  you will see an error
#  todo get it working in container
#
#  to open a shell in the container
#  ================================
#  docker ps
#  docker exec -it <the-container-id> /bin/bash
#
#  to stop container and remove image
#  ==================================
#  docker ps
#  docker stop <the-container-id>
#  docker rm <the-container-id>
#
# to find out why container doesn't start
# =======================================
# docker logs <startup-hex-id-from-docker-run>
# Listening for transport dt_socket at address: 8787
# Error: Could not find or load main class monitor.api.Main
#

FROM openjdk:8
COPY ./target/classes /tmp
COPY ./lib/ganymed-ssh2-262.jar /tmp
RUN mkdir /tmp/log
WORKDIR /tmp

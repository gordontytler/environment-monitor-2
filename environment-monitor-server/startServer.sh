#!/bin/bash

#JAVA_OPTIONS="-Djava.util.logging.config.file=./logging.properties -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"

JAVA_OPTIONS=-Djava.util.logging.config.file=./logging.properties
MAVEN_HOME=~/.m2
CLASSPATH=./target/environment-monitor-server-1.0-SNAPSHOT.jar:$MAVEN_HOME/repository/ch/ethz/ganymed/ganymed-ssh2/262/ganymed-ssh2-262.jar

#java $JAVA_OPTIONS -classpath $CLASSPATH monitor.api.Main
#  >> ./log/stdout.log &

java   -classpath $CLASSPATH monitor.api.Main

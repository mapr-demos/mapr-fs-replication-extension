#!/usr/bin/env bash

#Start Monitor
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5555 -cp "`mapr classpath`:/home/mapr/mapr-fs-replication-extension/monitor/target/monitor-1.0-SNAPSHOT-jar-with-dependencies.jar" com.mapr.fs.application.Monitor

#Start Consumer
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -cp "`mapr classpath`:/home/mapr/mapr-fs-replication-extension/consumer/target/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar" com.mapr.fs.application.Consumer

#Start Consumer UI
java -jar mapr-fs-replication-extension/mapr-consumer-fs-ui/target/mapr-consumer-fs-ui-1.0-SNAPSHOT.jar

#Start Monitor UI
java -jar mapr-fs-replication-extension/mapr-monitor-fs-ui/target/mapr-monitor-fs-ui-1.0-SNAPSHOT.jar
#!/usr/bin/env bash

#Start Monitor
sudo -u mapr java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5555 -cp "`mapr classpath`:/home/vagrant/jars/monitor-1.0-SNAPSHOT-jar-with-dependencies.jar" com.mapr.fs.application.Monitor -p ~/jars/config.conf

#Start Consumer
sudo -u mapr java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -cp "`mapr classpath`:/home/vagrant/jars/consumer-1.0-SNAPSHOT-jar-with-dependencies.jar" com.mapr.fs.application.Consumer -p ~/jars/config.conf

#Start Consumer UI
sudo -u mapr java -jar ~/jars/mapr-consumer-fs-ui-1.0-SNAPSHOT.jar -p ~/jars/config.conf

#Start Monitor UI
sudo -u mapr java -jar ~/jars/mapr-monitor-fs-ui-1.0-SNAPSHOT.jar -p ~/jars/config.conf

#Start S3 UI
sudo -u mapr java -jar ~/jars/mapr-s3-replication-ui-1.0-SNAPSHOT.jar -p ~/jars/config.conf

#Start s3Replicator
sudo -u mapr java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=6006 -cp "`mapr classpath`:/home/vagrant/jars/maprfs-s3-replication-1.0-SNAPSHOT-jar-with-dependencies.jar" com.mapr.fs.s3.S3Replicator -p ~/jars/config.conf

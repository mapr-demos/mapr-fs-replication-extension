#!/usr/bin/env bash
#Start Monitor
sudo -u mapr java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5555 -cp "`mapr classpath`:/home/vagrant/mapr-fs-replication-extension/fs-monitor/target/fs-monitor-1.0-SNAPSHOT.jar" com.mapr.fs.application.Monitor vol1:/mapr/cyber.mapr.cluster/volumes/vol1 vol2:/mapr/cyber.mapr.cluster/volumes/vol2

#Start Consumer
sudo -u mapr java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -cp "`mapr classpath`:/home/vagrant/mapr-fs-replication-extension/fs-monitor/target/fs-monitor-1.0-SNAPSHOT.jar" com.mapr.fs.application.Consumer vol1:/mapr/cyber.mapr.cluster/volumes_replica/vol1 vol2:/mapr/cyber.mapr.cluster/volumes_replica/vol2

#Start ApplicationStarter
sudo -u mapr java -jar mapr-fs-replication-extension/fs-monitor/target/fs-monitor-1.0-SNAPSHOT.jar



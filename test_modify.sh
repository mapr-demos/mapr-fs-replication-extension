#!/usr/bin/env bash

# For this script must be installed sshpass tool
# Example of run command:
# bash test_modify.sh 3 ~/test1.img /mapr/my.cluster.com/volumes_replica/vol1/test1.img mapr mapr 127.0.0.1 2222

# Size of created File in Mb
size=$1

# Path where file must be created
path_to_monitored_file=$2

# Path to replicated file
path_to_replicated_file=$3

# User name for ssh connection to node with replication
user_name_replication_cluster=$4

# User password for ssh connection to node with replication
user_password_on_remote_node=$5

# Host for ssh connection to node with replication
host_replication_cluster=$6

# Port for ssh connection to node with replication
port=$7

printf "\n\nMonitored file -> $2"
printf "\nReplicated file -> $3\n\n"

echo "----------------MODIFY TEST----------------"
dd if=/dev/zero of=${path_to_monitored_file} bs=1024 count=0 seek=$[1024*size] status=none

printf "\nCreated file named $path_to_monitored_file with size close to ${size} Mb \n\n"

md5=`md5sum ${path_to_monitored_file} | awk '{ print $1 }'`
echo "MONITORED FILE HASH"
echo ${md5}

sleep_time=$[2*size]
sleep ${sleep_time}

REMOTE_HASH=$(
sshpass -p "${user_password_on_remote_node}" ssh -o StrictHostKeyChecking=no ${user_name_replication_cluster}@${host_replication_cluster} -T -p ${port} << EOSSH
md5sum ${path_to_replicated_file} | awk '{print \$1}'
EOSSH
)
printf "\nREPLICATED FILE HASH\n"
echo ${REMOTE_HASH}

if [ "$md5" == "$REMOTE_HASH" ]
	then printf "\nModify Test: SUCCESS \n\n"
else
	printf "\nModify Test: FAULT\n\n"
fi

echo "----------------DELETE TEST----------------"

printf "\nDelete $path_to_monitored_file \n"
rm ${path_to_monitored_file}

sleep 4

sshpass -p ${user_password_on_remote_node} ssh -o StrictHostKeyChecking=no ${user_name_replication_cluster}@${host_replication_cluster} -T -p ${port} << EOSSH
    if [ ! -f ${path_to_replicated_file} ]; then
    	printf "\n${path_to_replicated_file} also deleted\n"
    	printf "\nDelete Test: SUCCESS \n\n"
    else
        printf "\nDelete Test: FAULT\n"
    fi
EOSSH



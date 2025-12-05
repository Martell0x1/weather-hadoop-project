#!/bin/bash
set -e

service ssh start

su - hadoop -c "
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export HADOOP_HOME=/opt/hadoop
export HIVE_HOME=/opt/hive
export PATH=\$JAVA_HOME/bin:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin:\$HIVE_HOME/bin:\$PATH

# Format NameNode only once
if [ ! -d /home/hadoop/hdfs/namenode/current ]; then
    echo 'Formatting NameNode...'
    hdfs namenode -format -force
fi

# Start HDFS + YARN
\$HADOOP_HOME/sbin/start-dfs.sh
\$HADOOP_HOME/sbin/start-yarn.sh

# Create Hive warehouse dir
hdfs dfs -mkdir -p /user/hive/warehouse
hdfs dfs -chmod g+w /user/hive/warehouse

# Initialize Hive schema (VERY IMPORTANT)
schematool -dbType derby -initSchema
hiveserver2

# Keep container alive
exec bash
"


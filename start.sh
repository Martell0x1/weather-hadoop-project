#!/bin/bash
set -e


service ssh start

su - hadoop -c "bash -lc
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export HADOOP_HOME=/opt/hadoop
export PATH=\$PATH:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin

if [ ! -d /home/hadoop/hdfs/namenode/current ]; then
    echo \" formating hdfs namenode we keda ya3ne :/ \"
    hdfs namenode -format -force
fi

$HADOOP_HOME/sbin/start-dfs.sh
$HADOOP_HOME/sbin/start-yarn.sh

hdfs dfs -mkdir -p /weather/data
hdfs dfs -put $HADOOP_HOME/weatherHistory.csv /weather/input

touch $HIVE_HOME/conf/hive-env.sh
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' > $HIVE_HOME/conf/hive-env.sh
echo 'export HADOOP_HOME=/opt/hadoop' >> $HIVE_HOME/conf/hive-env.sh
echo 'export PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH' >> $HIVE_HOME/conf/hive-env.sh


# keep container alive
exec bash
"


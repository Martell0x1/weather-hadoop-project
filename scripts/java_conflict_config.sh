#!/bin/bash

export HADOOP_HOME=/opt/hadoop
echo 'export HADOOP_OPTS="$HADOOP_OPTS \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED"
' >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh

echo ' export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ' >> /opt/hive/conf/hive-env.sh
echo ' export HADOOP_HOME=/opt/hadoop' >> /opt/hive/hive-env.sh
echo ' export PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$HIVE_HOME/bin:$PATH ' >> /opt/hive/hive-env.sh

echo 'export HADOOP_OPTS="$HADOOP_OPTS \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED"
' >> /opt/hive/hive-env.sh

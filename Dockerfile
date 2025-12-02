FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV HADOOP_VERSION=3.3.4
ENV HIVE_VERSION=4.1.0
ENV HADOOP_HOME=/opt/hadoop
ENV HIVE_HOME=/opt/hive
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$HIVE_HOME/bin

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless ssh rsync wget curl tar vim net-tools lsof procps \
    python3 python3-pip && \
    rm -fr /var/lib/apt/lists/*


RUN useradd -m -s /bin/bash hadoop && \
    mkdir -p /opt && chown -R hadoop:hadoop /opt

USER hadoop
WORKDIR /opt


RUN wget -q https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz && \
    tar -xzf hadoop-${HADOOP_VERSION}.tar.gz && \
    mv hadoop-${HADOOP_VERSION} hadoop && \
    rm hadoop-${HADOOP_VERSION}.tar.gz


RUN wget -q https://archive.apache.org/dist/hive/hive-${HIVE_VERSION}/apache-hive-${HIVE_VERSION}-bin.tar.gz && \
    tar -xzf apache-hive-${HIVE_VERSION}-bin.tar.gz && \
    mv apache-hive-${HIVE_VERSION}-bin hive && \
    rm apache-hive-${HIVE_VERSION}-bin.tar.gz

USER root


RUN apt-get update && apt-get install -y openssh-server && \
    mkdir -p /var/run/sshd

USER hadoop
RUN mkdir -p /home/hadoop/.ssh && chmod 700 /home/hadoop/.ssh && \
    ssh-keygen -t rsa -P "" -f /home/hadoop/.ssh/id_rsa && \
    cat /home/hadoop/.ssh/id_rsa.pub >> /home/hadoop/.ssh/authorized_keys && \
    chmod 600 /home/hadoop/.ssh/authorized_keys


USER root
COPY config/* $HADOOP_HOME/etc/hadoop/
COPY hive/hive-site.xml $HIVE_HOME/conf/


RUN echo "export JAVA_HOME=${JAVA_HOME}" >> /home/hadoop/.bashrc && \
    echo "export HADOOP_HOME=${HADOOP_HOME}" >> /home/hadoop/.bashrc && \
    echo "export HIVE_HOME=${HIVE_HOME}" >> /home/hadoop/.bashrc && \
    echo "export HADOOP_CONF_DIR=\$HADOOP_HOME/etc/hadoop" >> /home/hadoop/.bashrc && \
    echo "export PATH=\$PATH:\$HADOOP_HOME/bin:\$HADOOP_HOME/sbin:\$HIVE_HOME/bin" >> /home/hadoop/.bashrc

RUN echo "export JAVA_HOME=${JAVA_HOME}" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh

COPY data/* /home/hadoop/

# -----------------------
# Ports
# -----------------------
EXPOSE 9870 8088 9000 9864 8042

# -----------------------
# Startup script
# -----------------------
USER root
COPY start.sh /start.sh
RUN chmod +x /start.sh

ENTRYPOINT ["/start.sh"]


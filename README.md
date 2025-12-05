# Hadoop Weather Analysis on Docker

A complete end-to-end project that processes a real-world weather dataset using **Hadoop**, **MapReduce**, **Hive**, and full **Docker containerization**. This README documents the full workflow, technical challenges, implementation details, and deployment to Docker Hub.
<p align="center">

<img alt="Java" src="https://img.shields.io/badge/Language-Java-orange?style=flat-square&logo=openjdk&logoColor=white">

<img alt="Hadoop" src="https://img.shields.io/badge/Framework-Hadoop-yellow?style=flat-square&logo=apachehadoop&logoColor=black">

<img alt="Hive" src="https://img.shields.io/badge/Tool-Hive-gold?style=flat-square&logo=apachehive&logoColor=black">

<img alt="Docker" src="https://img.shields.io/badge/Container-Docker-2496ED?style=flat-square&logo=docker&logoColor=white">

<img alt="DockerHub" src="https://img.shields.io/badge/Registry-DockerHub-0db7ed?style=flat-square&logo=docker&logoColor=white">

<img alt="MapReduce" src="https://img.shields.io/badge/Engine-MapReduce-green?style=flat-square">

</p>

---

## Project Overview

This project builds a reproducible Hadoop environment inside Docker containers to analyze historical **NOAA weather data**. The goal is to:

- Extract, clean, and load weather data into HDFS
    
- Run MapReduce jobs to compute analytics
    
- Use Hive for querying and metadata management
    
- Containerize the entire ecosystem for easy sharing and deployment
    

---

## 🌦️ Dataset Description

The dataset includes observations across the year **1916**, containing:

- **STATION** — Weather station ID
    
- **DATE** — Timestamp in format `YYYY-MM-DD HH:MM:SS`
    
- **TMP** — Recorded temperature in tenths of degrees Celsius
    
- Other unused attributes
    

### 👉 Features Extracted

From each record, we parsed:

- `year`
    
- `month`
    
- `day`
    
- `temperature`
    

After cleaning, the dataset was stored into Hive and used for MapReduce computation.

---

## 🧹 Data Cleaning Issues & Solutions

We faced several problems during data preparation:

### 1. **SLF4J Multiple Bindings Warning**

Hive struggled due to conflicting log4j bindings.

- **Fix:** Removed duplicate JARs and ensured correct Hive logging dependencies.
    

### 2. **Hive Metastore Failing to Start**

Error caused by Java module access restrictions.

- **Fix:** Added the required JVM flag:
    
    ```bash
    export HADOOP_OPTS="$HADOOP_OPTS --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED"
    ```
    

### 3. **Timestamp Parsing Issues**

Some rows had irregular timestamps.

- **Fix:** Applied regex + substring extraction before inserting into Hive.
    

### 4. **Large File Loading**

HDFS refused large files due to block size mismatch.

- **Fix:** Uploaded using:
    
    ```bash
    hdfs dfs -put -f weather.csv /input
    ```
    

---

## 🗺️ MapReduce Job — Max Temperature Per Month

We wrote a custom **Java MapReduce job** to extract:

> **The highest temperature for each month (1–12) in the year 1916.**

### 🔹 Mapper

Extracts:

- `month` from timestamp
    
- `temperature`
    

Emits:

```
key: month
value: temperature
```

### 🔹 Reducer

For each month:

- Iterates over temperatures
    
- Computes the **maximum**
    

Example Output:

```
01   31.2
02   33.0
03   37.1
...
12   29.4
```

---

## 🐳 Docker & Containerization

We built a **custom Hadoop-Hive Docker image** with:

- Hadoop 3.x
    
- Hive 4.x
    
- JDK 11
    
- Pre-configured environment variables
    
- HDFS, YARN, Hive Metastore
    

### Challenges Faced

#### 1. **Container Not Running in Interactive Mode**

Needed to start with:

```bash
docker run -it weather-hadoop bash
```

#### 2. **Ports & Networking**

HiveServer2 required exposing:

```
10000  (HiveServer2)
9083   (Metastore)
```

#### 3. **Persisting Data**

We mounted:

```
./data:/opt/data
```

so HDFS data stays across restarts.

---

## 📦 Publishing to Docker Hub

### Steps Used

1. Tag image:
    

```bash
docker tag weather-hadoop marawan/weather-hadoop:latest
```

2. Login:
    

```bash
docker login
```

3. Push:
    

```bash
docker push martell0x1/weather-hadoop:latest
```

Now anyone can pull the environment using:

```bash
docker pull martell0x1/weather-hadoop:latest
```

---

## 🧪 How to Run the Project

### Start the container

```bash
docker run -it martell0x1/weather-hadoop:latest bash
```

### Start Hadoop

```bash
start-dfs.sh
start-yarn.sh
```

### Start Hive

```bash
hive --service metastore &
hiveserver2 &
```

### Run MapReduce job

```bash
hadoop jar WeatherMaxTemp.jar MaxTempDriver /input /output
```

### Query using Hive

```sql
SELECT * FROM weather_data_cleaned LIMIT 10;
```

---

## 📊 Final Output

The result is a clean summary of **maximum temperatures for each month** in 1916, computed using distributed processing and stored/queried through Hive.

---

## 🏁 Conclusion

This project demonstrates:

- Distributed data processing using Hadoop
    
- Running custom Java MapReduce jobs
    
- Integrating Hive with Hadoop inside Docker
    
- Overcoming real compatibility and configuration issues
    
- Publishing a production-grade container image to Docker Hub
    

It serves as a full reference implementation for anyone learning Hadoop, Hive, or large-scale data processing.

---

## ⭐ Future Enhancements

- Add Spark layer
    
- Run monthly averages, variance, and anomaly detection
    
- Deploy on Kubernetes
    
- Use Sqoop to integrate with external databases
    

---

## 📬 Meet The Team
- **My Self**
- @loubid
- @huda-ali
- @habiba3072005
- @HabibaHamdy2004
- @rhmShark

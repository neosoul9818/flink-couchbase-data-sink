
# Flink-couchbase-data-sink
### A Flink job that reads a Json file (either one-time or continous poll) as its source and dumps the list of json documents to a couchbase sink using the asynchronous Couchbase SDK.  
---

- A file containing the list of json documents with id is present that needs to be inserted into couchbase 
- A Flink job takes the file as its source and dumps it to a couchbase sink 
- Couchbase sink puts the incoming documents to the cluster specified in the config files
- Flink job has the ability to poll the file for changes at a duration specified in the config 

![flink](https://user-images.githubusercontent.com/12872673/49009888-ac6d4c80-f197-11e8-887c-72688aff0ded.png)![image](https://user-images.githubusercontent.com/12872673/80646620-b715ca80-8a8a-11ea-8f8c-b2f74283742b.png)


### Prerequisites 
* Java 1.8
* Zookeeper 3.6.0
* Apache Flink 1.10.0 [Download](https://flink.apache.org/downloads.html)
* Couchbase Server 6.5.1

### Config properties 
Edit the following properties to match your target instance

Property | Value
--- | --- 
couchbase.node | Location of couchbase cluster. By default, localhost
couchbase.username | Username of couchbase dashboard
couchbase.password | Password of couchbase dashboard
startup.documents.path | Path of the json document file. By default, it is present in src/main/resources
startup.documents.poll.continuous | Flag to enable polling or not. By default set to false
startup.documents.poll.duration | Duration in ms after which file will be polled for changes if enabled


### Setting up the project 
```
  # Start Zookeeper (required for Flink)
  $ ./zkServer start

  # Start Couchbase server instance 
  $ sudo /etc/init.d/couchbase-server start

  # Create a default bucket. Change port accordingly
  $ View couchbase dashboard at http://127.0.0.1:8091. Enter your credentials and create a bucket called "data"
  
  # Start Flink cluster in the FLINK_BIN directory
  $ ./start-cluster.sh
  
  # Submit the job by packaging the jar and supplying its path. The config lies in src/main/resources
  $ flink.sh run -c com.aman.flink.job.FlinkDatabaseStartupJob <jar-location> --config <config-file-location>
  $ ./flink run -c <main-class> <jar> <config-properties>

  e.g ./flink run -c com.github.isopropylcyanide.flinkcouchbasesink.FlinkDatabaseStartupJob \
                      flink-couchbase-data-starter/target/flink-couchbase-sink-1.0.jar \
                      flink-couchbase-data-starter/src/main/resources/config.properties
  
  # Verify the documents were inserted properly
  $ View the dashboard at http://127.0.0.1:8091 and verify the documents in the bucket "data"

  # Stop the cluster once job is done
  $ ./stop-cluster.sh
  
```
Note: Replace .sh files with .bat files when working in a Windows environment.

### Output   
- Flink jobs submitted with `startup.documents.poll.continuous`: true will run continuously. 
- Flink jobs submitted with `startup.documents.poll.continuous`: false will finish once run

![image](https://user-images.githubusercontent.com/12872673/80646236-fe4f8b80-8a89-11ea-8632-f7d590007f22.png)
 
- Verify data in Couchbase
![image](https://user-images.githubusercontent.com/12872673/80646304-19ba9680-8a8a-11ea-9f21-99f7a2b87481.png)
 
---
### Flink 
* Open-source platform for distributed stream and batch data processing.
* Provides data distribution, communication, and fault tolerance for distributed computations over data streams. 
* Builds batch processing on top of the streaming engine, overlaying native iteration support, managed memory, and program optimization.

---
### Couchbase
* Open source, distributed, NoSQL document-oriented engagement database. 
* Exposes a fast key-value store with managed cache for sub-millisecond data operations
* Specialized to provide low-latency data management for large-scale interactive web, mobile, and IoT applications


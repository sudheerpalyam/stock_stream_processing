### 
Proposed Data Engineering Framework for ThroughWorks, as part for a technical assessment.

Currently focusing on performing Moving Averages on Streaming Market Stock data, but this framework is generally applicable for any Data Engineering, Machine Learning Engineering tasks.

@author: Sudheer Palyam



### Analysis of Structured Streaming Sliding Window based aggregates:

![Alt text](static/OutputAnalysis.png?raw=true "Streaming output")


As we see the output above, Kafka is fed with one message per second.
Spark streaming is set to 3 seconds windows, sliding every second.
So there will be three messages in each window.
Since we are grouping by StockName, in this case AGL. There were two AGL stocks in one sliding window and its aggregates like max, min and avg are computed.
So we can observe how Spark Structured Streaming retains messages from previous windows.
Watermarking is used to limit the state maintenance, as more state to maintain mean more resources utilised.



### Architectural Patterns:


#### File Streaming Mode - Spark Window Aggregations
![Alt text](static/SparkBatchPipeline.jpeg?raw=true "Stock Aggregations by loading files in Batch mode")


#### Feature bookmarks:

- Spark Structured Streaming
  - [File Stream](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/HelloStructredStreaming.scala#L23)
    - [schema](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L46)
  - [Kafka Source](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L58-L64)
  - [Kafka Sink](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L96-L109)
  - [EventTime](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L79)
  - [ProcessingTime](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L80)
  - [Watermarks](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L77)
  - [Checkpointing](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L104)
  - [Sliding Window](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L78)
  - [Tumbling Window](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L79)
  - Aggregations/Operations
     - [avg](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L81)
     - [count](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/SocketSourceStreaming.scala#L37)
     - [max](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L45)
     - [flatMap](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/HelloStructredStreaming.scala#L28)
     - [alias](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L81)
     - [filter](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L82)
     - [groupby](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L79)
     - [where](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L56)
     - [withColumnRenamed](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L55)
     - [repartition](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L65)
     - [partitionBy](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L70)
  - SQL
    - [selectExpr](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L67)
    - [CAST](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L67)
    - [where](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L64)
  - Output Modes
    - [complete](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L106)
    - [append](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L107)
    - [update](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L108)
  - [Multiple Stream Queries](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L111)
  - [Kafka Producer](src/main/scala/au/com/thoughtworks/assessment/spark/util/RandomStocksKafkaProducer.scala)





##### Quick steps to setup kafka and run locally:
  Download from https://kafka.apache.org/downloads

  start zookeeper:
  $<kafka-dir>/bin/zookeeper-server-start.sh config/zookeeper.properties

  start kafka broker(s):
  $<kafka-dir>/bin/kafka-server-start.sh config/server.properties

  create kafka topics:
  $<kafka-dir>/bin/kafka-topics.sh --create --topic "stocks" --replication-factor 1 --partitions 1 --zookeeper localhost:2181
  $<kafka-dir>/bin/kafka-topics.sh --create --topic "stocks_averages" --replication-factor 1 --partitions 1 --zookeeper localhost:2181

  List Topics:
  $<kafka-dir>/bin/kafka-topics.sh  --list --zookeeper localhost:2181
  Delete Topic
  $<kafka-dir>/bin/kafka-topics.sh  --delete --topic stocks --zookeeper localhost:2181

  $<kafka-dir>/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic stocks
  $<kafka-dir>/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic stocks

  describe:
  $<kafka-dir>/bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic stocks
  $<kafka-dir>/bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic stocks_averages


  #### Next Steps:
  1) Integrate a visualization layer based on Kibana & InfluxDB to continuously stream raw vs moving averages
  2) Run Kafka & Spark in Yarn/Mesos/DCOS Clustered Mode
  3) Implement the same pipeline using AWS native Serverless components replacing:
        Kafka -> AWS Kinesis Streams
        Spark -> AWS Kinesis Analytics (As there is no serverless equivalent of Spark yet in AWS)
        Spark Console/Kafka Writer -> AWS Kinesis FireHose
        Kibana -> AWS QuickSight
        Scala Kafka Producer -> Kinesis Data Generator
  4) Dockerize all the workflow components and run it in Container managers like Kubernetes or AWS Elastic Kubernetes Service
  5) Enhance Unit Tests and perform Code Coverage and eventually DevOps
  6) SonarQube/Fortify code vulnerability assessment




  #### References:
  https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23/MEETUP_NOTES.md - My implementation is inspired by this
  https://github.com/pablo-tech/SparkService--Statistician
  https://aws.amazon.com/big-data/datalakes-and-analytics/
  https://docs.aws.amazon.com/streams/latest/dev/learning-kinesis-module-one.html
  https://vishnuviswanath.com/spark_structured_streaming.html -- Good blog on Structured Streaming
  https://databricks.com/blog/2017/05/08/event-time-aggregation-watermarking-apache-sparks-structured-streaming.html - Structured Streaming Window aggregations
  https://github.com/snowplow/spark-streaming-example-project/blob/master/src/main/scala/com.snowplowanalytics.spark/streaming/StreamingCounts.scala - Spark Streaming write to DynamoDB
  https://github.com/snowplow/spark-streaming-example-project/blob/master/src/main/scala/com.snowplowanalytics.spark/streaming/kinesis/KinesisUtils.scala - Kinesis Utils
  https://github.com/snowplow/spark-streaming-example-project/blob/master/src/main/scala/com.snowplowanalytics.spark/streaming/storage/DynamoUtils.scala - Dynamo Utils

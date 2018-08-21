### 
Proposed Data Engineering Framework for ThroughWorks, as part for a technical assessment.

Currently focusing on performing Moving Averages on Streaming Market Stock data, but this framework is generally applicable for any Data Engineering, Machine Learning Engineering tasks.

@author: Sudheer Palyam


  * To setup kafka:
  * Download from https://kafka.apache.org/downloads
  *
  * start zookeeper:
  * bin/zookeeper-server-start.sh conig/zookeeper.properties
  *
  * start kafka broker(s):
  * bin/kafka-server-start.sh config/server.properties
  *
  * create kafka topics:
  * bin/kafka-topics.sh --create --topic "stocks" --replication-factor 1 --partitions 1 --zookeeper localhost:2181
  * bin/kafka-topics.sh --create --topic "stocks_averages" --replication-factor 1 --partitions 1 --zookeeper localhost:2181
  *
  * bin/kafka-console-producer.sh --topic stocks_averages --broker-list localhost:9092
  * bin/kafka-console-console.sh --topic stocks_averages --bootstrap-server localhost:2181
  *
  * describe:
  * bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic stocks 
  * bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic stocks_averages


Architectural Patterns:

Batch Mode - Spark Window Aggregations
![alt text](https://github.com/sudheerpalyam/stock_stream_processing/static/SparkBatchPipeline.jpeg)



Spark 2.3/2.4.0-SNAPSHOT repository

- Spark Structured Streaming
  - [File Stream](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/HelloStructredStreaming.scala#L23)
    - [schema](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L30)
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
     - [withColumnRenamed](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L48)
     - [repartition](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L57)
     - [partitionBy](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L62)
  - SQL
    - [selectExpr](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L67)
    - [CAST](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L67)
    - [where](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L56)
  - Output Modes
    - [complete](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L106)
    - [append](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L107)
    - [update](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L108)
  - [Multiple Stream Queries](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaSourceStreaming.scala#L111)
  - [Kafka Producer](src/main/scala/au/com/thoughtworks/assessment/spark/util/RandomStocksKafkaProducer.scala)

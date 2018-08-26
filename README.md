### Technical assessment

Currently focusing on performing Moving Averages on Streaming Market Stock data, but this framework is generally extendable for any Data Engineering, Machine Learning Engineering tasks.

@author: Sudheer Palyam


### Problem Statement
    Design and implement a Scalable, Distributed, Complete solution accepting input data feed and perform ***moving averages*** which helps identifying trends in Stock Market.

#### Technologies chosen:  Apache Spark 2.3, Scala 2.11, SBT 1.0, Kafka 2.0.0, Zookeeper

### Analysis of Structured Streaming Sliding Window based aggregates:

![Alt text](static/OutputAnalysis.png?raw=true "Streaming output")


As we see the output above, Kafka is fed with one message per second.
Spark streaming is set to **3 seconds windows**, **sliding every second**.
So there will be three messages in each window.
Since we are grouping by StockName, in this case AGL. There were two AGL stocks in one sliding window and its aggregates like max, min and avg are computed.
So we can observe how Spark Structured Streaming retains messages from previous windows.
Watermarking is used to limit the state maintenance, as more state to maintain mean more resources utilised.

```
Moving averages formula provided:
     y(n-k+1) = (x(n-k+1) + x(n-k+2 ---- + x*k) * (1 / K)
        Where n : Window number
              k : number of items in each window
              x : item values

From our sample output:
     y = ( 436.84 + 698.17) * (1 / 2)
     i.e., y = 567.505   <== Moving Window Average value
```

### Data:
    [Synthetic Stock Events generated: ](data/sample_input.txt)
    [Final Moving Averages Output: ](data/sample_output.txt)


### Scalability considerations:
    KAFKA:
        1. Partitions with in Topics drives the parallelism of consumers. So increase the number of partitions so that a Spark Consumer Group can process these messages in parallel.
        2. Based on throughput requirements one can pick a rough number of partitions.
            Lets call the throughput from producer to a single partition is P
            Throughput from a single partition to a consumer is C
            Target throughput is T
            Required partitions = Max (T/P, T/C)
     SPARK:
        1. Run in Yarn-Client or Yarn-Cluster mode with kafka consumer group set. Set the Number of Kafka Partitions according to number of Spark Executor tasks feasible on the cluster.
        2. Appropriate Spark Streaming Watermark configuration.
     AWS:
        1. Porting this pipeline to AWS Kinesis Firehose & Analytics will take care of scaling shards automatically and these are also managed serverless services.




### Architectural Patterns:
    Given problem can be implemented in the following architectures, Considering Cost optimization, Reliability, Operational Efficiency, Performance & Security (CROPS):



#### 1. Classic Approach - Kafka + Spark Window Aggregations (current implementation)
![Alt text](static/SparkKafkaStreaming.jpeg?raw=true "Stock Aggregations using kafka message and spark")


#### 2. Serverless Approach - Cloud Native - Infinite Scalability and less management (proposed)
![Alt text](static/KinesisDataStream.jpg?raw=true "Stock Aggregations using Kinesis stream and analytics")


#### 3. File Streaming Mode - Spark Window Aggregations (Alternate approach)
![Alt text](static/SparkBatchPipeline.jpeg?raw=true "Stock Aggregations by loading files in Batch mode")



### Machine Learning :

The problem is a typical time series problem. In this you can see if there's seasonality in the time series.
In reality a time series problem one needs to decompose the time series to see whether its additive or multiplicative in nature.
Below is the scenario:

Below is one such example :
https://onlinecourses.science.psu.edu/stat510/node/70/

Usually in a time series we handle 3 scenarios:
Seasonality (S)
Trend (T)
Remainder component (R)
For example in a time series for a data point Y, if the above 3 components are additive in nature then its expressed as
Y = S + T+ R
if this is multiplicative in nature then it would be:
Y = S * T * R


We can use following example:
https://stackoverflow.com/questions/23402303/apache-spark-moving-average
http://xinhstechblog.blogspot.com/2016/04/spark-window-functions-for-dataframes.html
https://github.com/apache/spark/blob/v1.4.1/mllib/src/main/scala/org/apache/spark/mllib/rdd/SlidingRDD.scala


But for now, you can do this in the format:

 - Aggregate the data if its from Daily to weekly / monthly levels.
 - Perform What if analysis - What if the stock price is going up / down by "X" dollars (maybe 2-3 dollars)
 - Check for Stationarity - See if there is any way we can do Dickey fuller test / KS Test in Spark Scala. I am not sure if this is available.
 Usually this is the way we do in a time series data: http://rstudio-pubs-static.s3.amazonaws.com/22255_f08b6a7cfff9451abaace84773bb41e0.html
 - You can try applying ARIMA Time series models
https://mapr.com/blog/using-apache-spark-sql-explore-sp-500-and-oil-stock-prices/
https://stackoverflow.com/questions/28248916/how-to-do-time-series-simple-forecast


### Feature bookmarks:

- Spark Structured Streaming
  - [File Stream](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/HelloStructredStreaming.scala#L23)
    - [schema](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L46)
  - [Kafka Source](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L42-L50)
  - [Kafka Sink](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L83-L93)
  - [EventTime](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L63)
  - [ProcessingTime](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L65)
  - [Watermarks](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L62)
  - [Checkpointing](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L91)
  - [Sliding Window](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L63)
  - [Tumbling Window](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L64)
  - Aggregations/Operations
     - [avg](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L66)
     - [count](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/SocketSourceStreaming.scala#L37)
     - [max](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/StreamingAggregations.scala#L45)
     - [flatMap](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/HelloStructredStreaming.scala#L28)
     - [alias](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L81)
     - [filter](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L82)
     - [groupby](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L79)
     - [withColumnRenamed](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L55)
     - [repartition](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L65)
     - [partitionBy](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L70)
  - SQL
    - [selectExpr](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L54)
    - [CAST](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L54)
    - [where](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/FileStreamingAggregations.scala#L64)
  - Output Modes
    - [complete](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L79)
    - [append](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L80)
    - [update](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala#L81)
  - [Multiple Stream Queries](src/main/scala/au/com/thoughtworks/assessment/spark/streaming/KafkaStructuredStreaming.scala)
  - [Kafka Producer](src/main/scala/au/com/thoughtworks/assessment/spark/util/RandomStocksKafkaProducer.scala)



### Quick steps to setup kafka and run locally:
  Download from https://kafka.apache.org/downloads
  ```
  start zookeeper:
  $<kafka-dir>/bin/zookeeper-server-start.sh config/zookeeper.properties

  start kafka broker(s):
  $<kafka-dir>/bin/kafka-server-start.sh config/server.properties

  create kafka topics:
  $<kafka-dir>/bin/kafka-topics.sh --create --topic "stocks" --replication-factor 1 --partitions 4 --zookeeper localhost:2181
  $<kafka-dir>/bin/kafka-topics.sh --create --topic "stocks_averages" --replication-factor 1 --partitions 4 --zookeeper localhost:2181

  List Topics:
  $<kafka-dir>/bin/kafka-topics.sh  --list --zookeeper localhost:2181
  Delete Topic
  $<kafka-dir>/bin/kafka-topics.sh  --delete --topic stocks --zookeeper localhost:2181

  $<kafka-dir>/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic stocks
  $<kafka-dir>/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic stocks

  describe:
  $<kafka-dir>/bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic stocks
  $<kafka-dir>/bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic stocks_averages

  Spark Submit Script:
  src/main/resources/submitSpark.sh

  nohup spark-submit \
   --deploy-mode client --master yarn \
   --executor-cores 4 --executor-memory 6g --conf spark.yarn.executor.memoryOverhead=1G --conf spark.yarn.driver.memoryOverhead=1G  \
   --jars $(echo /home/sudheerpalyam/jars/*.jar | tr ' ' ',') \
   --class au.com.thoughtworks.assessment.spark.streaming.KafkaStructuredStreaming \
   /home/sudheerpalyam/jars/stock_stream_processing_2.11-0.1.jar \
   --isGlue false \
   --mode  yarn >> ../logs/stock-spark-$runTime.log &

```

### Next Steps:
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
  7) Associate a Machine Learning use case which can be facilitated by moving averages.



  ### References:
  https://github.com/soniclavier/bigdata-notebook/blob/master/spark_23
  https://github.com/pablo-tech/SparkService--Statistician
  https://aws.amazon.com/big-data/datalakes-and-analytics/
  https://docs.aws.amazon.com/streams/latest/dev/learning-kinesis-module-one.html
  https://vishnuviswanath.com/spark_structured_streaming.html -- Good blog on Structured Streaming
  https://databricks.com/blog/2017/05/08/event-time-aggregation-watermarking-apache-sparks-structured-streaming.html - Structured Streaming Window aggregations
  https://github.com/snowplow/spark-streaming-example-project/blob/master/src/main/scala/com.snowplowanalytics.spark/streaming/StreamingCounts.scala - Spark Streaming write to DynamoDB
  https://github.com/snowplow/spark-streaming-example-project/blob/master/src/main/scala/com.snowplowanalytics.spark/streaming/kinesis/KinesisUtils.scala - Kinesis Utils
  https://github.com/snowplow/spark-streaming-example-project/blob/master/src/main/scala/com.snowplowanalytics.spark/streaming/storage/DynamoUtils.scala - Dynamo Utils
  https://www.slideshare.net/SparkSummit/time-series-analytics-with-spark-spark-summit-east-talk-by-simon-Ouellette


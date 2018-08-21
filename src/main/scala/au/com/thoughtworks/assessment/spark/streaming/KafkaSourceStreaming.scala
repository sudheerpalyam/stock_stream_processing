package au.com.thoughtworks.assessment.spark.streaming

import java.sql.Timestamp

import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

/**
  * @author sudheerpalyam
  * @version 0.1
  *
  * Spark streaming processor from Kafka source.
  * Ingests messages from Kafka topics, unions messages from different topics, Cast messages to Stock objects, Performs Window based aggregations and write to Console/another Kafka topic.
  *
  */
object KafkaSourceStreaming {


  //convert aggregates into typed data
  case class StockEvent(stockName: String, tradeType: String, price: Option[Double], quantity: Option[Int], timestamp: Timestamp, eventTimeReadable: String)
  object StockEvent {
    def apply(rawStr: String): StockEvent = {
      val parts = rawStr.split(",")
      StockEvent(parts(0), parts(1), Some(java.lang.Double.parseDouble(parts(2))), Some(Integer.parseInt(parts(3))),  new Timestamp(parts(4).toLong), parts(5))
    }
  }

  def main(args: Array[String]): Unit = {

    //create a spark session, and run it on local mode
    val spark = SparkSession.builder()
      .appName("KafkaSourceStreaming")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._

    //read the source
    val df: DataFrame = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "stocks")
      //.schema(schema)  : we cannot set a schema for kafka source. Kafka source has a fixed schema of (key, value)
      .load()

    val stocks: Dataset[StockEvent] = df
      .selectExpr("CAST(value AS STRING)")
      .map(r ⇒ StockEvent(r.getString(0)))

    //aggregation without window
    /*val aggregates = stocks
      .groupBy("stockName")
      .avg("price")*/

    //windowing
    val aggregates = stocks
      .withWatermark("timestamp", "5 seconds") // Ignore data if they are late by more than 5 seconds
      .groupBy(window($"timestamp","3 seconds","1 seconds"), $"stockName")  //sliding window of size 4 seconds, that slides every 1 second
//      .groupBy(window($"timestamp","6 seconds","2 seconds"))  //sliding window of size 4 seconds, that slides every 1 second
//      .groupBy(window($"timestamp","6 seconds"), $"stockName") //tumbling window of size 4 seconds (event time)
      //.groupBy(window(current_timestamp(),"4 seconds"), $"stockName") //Use processing time.
      .agg(avg("price").alias("price"), min("price").alias("minPrice"), max("price").alias("maxPrice"), count("price").alias("count"))
      .select("window.start", "window.end", "stockName", "price", "minPrice", "maxPrice", "count")

   aggregates.printSchema()



    val writeToConsole = aggregates
      .writeStream
      .format("console")
      .option("truncate", "false") //prevent trimming output fields
      .queryName("kafka spark streaming console")
      .outputMode("append") // only supported when we set watermark. output only new
      .start()

    val writeToKafka = aggregates
      .selectExpr("CAST(stockName AS STRING) AS key", "to_json(struct(*)) AS value")
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers","localhost:9092")
      .option("topic", "stocks_averages")
      //.option("startingOffsets", "earliest") //earliest, latest or offset location. default latest for streaming
      //.option("endingOffsets", "latest") // used only for batch queries
      .option("checkpointLocation", "/tmp/sparkcheckpoint/") //must when not memory or console output
      .queryName("kafka spark streaming kafka")
      //.outputMode("complete") // output everything
      //.outputMode("append")  // only supported when we set watermark. output only new
      .outputMode("append") //ouput only new records
      .start()

    spark.streams.awaitAnyTermination() //running multiple streams at a time
  }
}

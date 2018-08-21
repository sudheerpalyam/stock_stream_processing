package au.com.thoughtworks.assessment.spark.streaming

import java.sql.Timestamp

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger


//convert aggregates into typed data
case class StockEvent(stockName: String, tradeType: String, price: Option[Double], quantity: Option[Int], timestamp: Timestamp, eventTimeReadable: String)
object StockEvent {
  def apply(rawStr: String): StockEvent = {
    val parts = rawStr.split(",")
    StockEvent(parts(0), parts(1), Some(java.lang.Double.parseDouble(parts(2))), Some(Integer.parseInt(parts(3))),  new Timestamp(parts(4).toLong), parts(5))
  }
}
/**
  * @author sudheerpalyam
  * @version 0.1
  *
  * Spark Structured Continuous Stream processor. Unlike other spark streaming mechansims, this does not micro-batch and hences processes messages on arrival.
  */
object ContinuousKafkaStreaming {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ContinuousStreaming Kafka example")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val raw = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "stocks")
      .load()

    //supported operations in Continuous Processing includes - Map, Filter, Project
    val aggstocks = raw
      .selectExpr("CAST(value as STRING)") //project
      .map(r ⇒ StockEvent(r.getString(0)))
      .filter("price > 70") //filter
      //.filter(c ⇒ c.price.getOrElse(0) > 70) //TypedFilter not supported in continuous processing,


   val consoleQuery = aggstocks
      .writeStream
      .format("console")
      .outputMode("append")
      //.outputMode("update")
      //.outputMode("complete") not supported since it requires an agg, and Continuous processing does not support aggregations.
      .trigger(Trigger.Continuous("1 second"))
      .start()


    val kafkaSinkQuery = aggstocks
      .selectExpr("CAST(stockName as STRING) as value") //kafka needs a value field
      .writeStream
      .format("kafka")
      .outputMode("update")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", "stocks_averages")
      .option("checkpointLocation", "/tmp/spark/continuousCheckpoint")
      .outputMode("update")
      .trigger(Trigger.Continuous("10 seconds")) //how often to checkpoint the offsets,
      .start()

    spark.streams.awaitAnyTermination()

  }

}

package com.thoughtworks.assessment.spark.streaming

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.scalatest.FunSpec

class StockStructuredStreamingAggregatesAppTest extends FunSpec with SparkSessionTestWrapper {

  spark.sparkContext.setLogLevel("WARN")
  import spark.implicits._

  //define the schema
  val schema = StructType(
    StructField("stockName", StringType) ::
      StructField("tradeType", StringType) ::
      StructField("price", DoubleType) ::
      StructField("quantity", IntegerType) ::
      StructField("timestamp", LongType) ::
      StructField("eventTimeReadable", StringType) :: Nil)

  //read the files from source
  val stocksTestDF: DataFrame = spark
    .readStream
    .schema(schema)
    .csv("src/test/resources/testdata/")
    .withColumn("timestamp", ($"timestamp" / 1000).cast(TimestampType)) // cast unix epoch to Timestamp

  stocksTestDF.printSchema()

  val aggregates = au.com.thoughtworks.assessment.spark.streaming.KafkaStructuredStreaming.performSlidingWindowFrame(stocksTestDF)


  val query = aggregates.writeStream
    .format("memory")
    .queryName("StockAggregateOutput")
    .outputMode(OutputMode.Complete())
    .start()
    .processAllAvailable()

  // Tests related to loading data in structured streaming fashion
  describe("Stock Structured Streaming Windows") {
    it("number of sliding windows count should be") {
      val q = "select * from StockAggregateOutput order by start"
      spark.sql(q).collect().foreach(println)

      // Since the test data has roughly 10 seconds of data and window spec is set to roll every 5 seconds, we should expect 7 aggregation
      val results: java.util.List[Row] = spark.sql(q).collectAsList()
      println("results size : " + results.size())
      assert(results.size() === 7)
    }

    it("number of sliding windows count for a given Stock should be") {
      val q = "select * from StockAggregateOutput where stockName = 'AGL'"
      spark.sql(q).collect().foreach(println)

      // Since the test data has roughly 10 seconds of data and window spec is set to roll every 5 seconds, we should expect 7 aggregation
      val results: java.util.List[Row] = spark.sql(q).collectAsList()
      println("results size : " + results.size())
      assert(results.size() === 3)
    }

    it("aggregated value should match") {
      val q = "select * from StockAggregateOutput order by start limit 1"
      spark.sql(q).collect().foreach(println)

      // Since the test data has roughly 10 seconds of data and window spec is set to roll every 5 seconds, we should expect 7 aggregation
      val results: java.util.List[Row] = spark.sql(q).collectAsList()
      val avgPrice = results.get(0).getAs[Double]("avgPrice")
      println("Average price for  AMC in first window : " + avgPrice)
      assert(avgPrice === 457.15000000000003)

      val minPrice = results.get(0).getAs[Double]("minPrice")
      println("Minimum price for  AMC in first window : " + minPrice)
      assert(minPrice === 158.84)

      val maxPrice = results.get(0).getAs[Double]("maxPrice")
      println("Maximum price for  AMC in first window : " + maxPrice)
      assert(maxPrice === 755.46)

      val count = results.get(0).getAs[Long]("count")
      println("Number of AMC stocks in first window : " + count)
      assert(count === 2)
    }
  }
}
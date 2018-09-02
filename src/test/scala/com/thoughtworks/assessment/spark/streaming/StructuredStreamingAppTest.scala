package com.thoughtworks.assessment.spark.streaming

import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.{BeforeAndAfterEach, FunSpec}
import org.apache.spark.sql.types._

import org.apache.spark.sql.streaming.OutputMode

class StructuredStreamingAppTest extends FunSpec with SparkSessionTestWrapper {

  import spark.implicits._

  spark.sparkContext.setLogLevel("WARN")

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

  //windowing
  //      val aggregates = stocksTestDF
  //        .withWatermark("timestamp", "5 seconds") // Ignore data if they are late by more than 5 seconds
  //        .groupBy(window(col("timestamp"),"3 seconds","1 seconds"), col("stockName"))  //sliding window of size 4 seconds, that slides every 1 second
  //        .agg(avg("price").alias("price"), min("price").alias("minPrice"), max("price").alias("maxPrice"), count("price").alias("count"))
  //        .select("window.start", "window.end", "stockName", "price", "minPrice", "maxPrice", "count")

  val query = stocksTestDF.writeStream
    .format("memory")
    .queryName("Output")
    .outputMode(OutputMode.Append())
    .start()
    .processAllAvailable()

  // Tests related to loading data in structured streaming fashion
  describe("Structured Streaming") {
    it("readstream should read all the data from file") {
      spark.sql("select * from Output").collect().foreach(println)

      val results: java.util.List[Row] = spark.sql("select * from Output").collectAsList()
      println("results size : " + results.size())
      assert(results.size() === 9)
    }

    it("readstream should associate actual number of columns") {
      val numColumns = spark.sql("desc formatted Output").collectAsList()
      println("Number of columns : " + numColumns.size())
      assert(numColumns.size() === 6)
    }
  }
}
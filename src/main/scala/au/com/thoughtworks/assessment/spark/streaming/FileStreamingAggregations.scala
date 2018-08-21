package au.com.thoughtworks.assessment.spark.streaming

import java.sql.Timestamp

import org.apache.spark.SparkConf
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * @author sudheerpalyam
  * @version 0.1
  *
  * Explore Spark streaming aggregations.
  */
object FileStreamingAggregations {

  //convert aggregates into typed data
  case class StockEvent(stockName: String, tradeType: String, price: Option[Double], quantity: Option[Int], timestamp: Timestamp, eventTimeReadable: String)
  case class AggretatedStockEvent(stockName: String, tradeType: String, maxPrice: Option[Double], avgQuantity: Option[Double])


  def main(args: Array[String]): Unit = {

    //create a spark session, and run it on local mode
    val spark = SparkSession.builder()
      .appName("StreaminAggregations")
      .master("local[*]")
      .getOrCreate()

    //spark.sparkContext.setLogLevel("WARN")

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
    val stocks: DataFrame = spark
      .readStream
      .schema(schema)
      .csv("/tmp/spark_file_stream/stocks/")

    //do aggregates
    val aggregates = stocks
      .groupBy("stockName", "tradeType")
      .agg(
        "price" → "max",
        "quantity" → "avg")
      .withColumnRenamed("max(price)", "maxPrice")
      .withColumnRenamed("avg(quantity)", "avgQuantity")

    aggregates.printSchema()
    aggregates.explain()

    val typedAggregates = aggregates.as[AggretatedStockEvent]
    val filtered  = typedAggregates
      .filter(_.maxPrice.exists(_ > 70))
      .where("avgQuantity > 10")
      .repartition(10)

    val query = filtered
      .writeStream
      .queryName("stocks_averages")
      .partitionBy("stockName")
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()
  }
}
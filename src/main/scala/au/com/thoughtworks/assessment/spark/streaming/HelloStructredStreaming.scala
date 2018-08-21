package au.com.thoughtworks.assessment.spark.streaming

import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * @author sudheerpalyam
  * @version 0.1
  *
  * Hello world Word count program to get started with Spark Structured Streaming
  */
object HelloStructredStreaming {

  def main(args: Array[String]): Unit = {

    //create a spark session, and run it on local mode
    val spark = SparkSession.builder()
      .appName("HelloStructuredStreaming")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    //read from a directory as text stream
    val readme: Dataset[String] = spark
      .readStream
      .textFile("/tmp/spark_file_stream/")

    //do word count
    val words = readme.flatMap(_.split(" "))
    val wordCounts = words.groupBy("value").count()

    //run the wordCount query and write to console
    val query = wordCounts
        .writeStream
        .queryName("WordCount")
        .outputMode("complete")
        .format("console")
        .start()

    //wait till query.stop() is called
    query.awaitTermination()
  }
}

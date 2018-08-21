package au.com.thoughtworks.assessment.spark.streaming

import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * @author sudheerpalyam
  * @version 0.1
  *
  * Word counter from socket streams.
  *
  * To get Socker Console: nc -lk 9999
  */
object SocketSourceStreaming {

  def main(args: Array[String]): Unit = {

    //create a spark session, and run it on local mode
    val spark = SparkSession.builder()
      .appName("NetcatSourceStreaming")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._

    //read from a directory as text stream
    val socketData = spark
      .readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load()

    //do word count
    val words = socketData.as[String].flatMap(_.split(" "))
    val wordCounts = words.groupBy("value").count()

    //run the wordCount query and write to console
    val query = wordCounts
        .writeStream
        .queryName("WordCount")
        .outputMode("update") //output only the counts that changed
        //.outputMode("complete") //output all the counts seen till now
        .format("console")
        //.trigger(Trigger.ProcessingTime(5000))  //triggers the query every "interval" if any new element was received.
        .start()

    //wait till query.stop() is called
    query.awaitTermination()
  }
}

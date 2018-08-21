package au.com.thoughtworks.assessment.spark.streaming.sources

import java.sql.Timestamp
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.annotation.tailrec
import scala.util.{Random => r}

/**
  * @author sudheerpalyam
  * @version 0.1
  *
  * Produces synthetic Stock Messages to be published to Kafka
  */
object RandomStocksKafkaProducer {

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val interval = 100
    val topic = "stocks"
    val numRecsToProduce: Option[Int] = None //None = infinite
    var theBoolean = true



    @tailrec
    def produceRecord(numRecToProduce: Option[Int]): Unit = {
      def generateStockRecord(topic: String): ProducerRecord[String, String] = {
        val stockName = s"stock${r.nextInt(10)}"
        val quantity = r.nextInt(150)
        val price = r.nextFloat * 100
        theBoolean = !theBoolean
        val tradeType = if (theBoolean) "SELL" else "BUY"

        val eventTime = System.currentTimeMillis()
        val eventTimeReadable = new Timestamp(eventTime.toLong)

        val value = s"$stockName,$tradeType,$price,$quantity,$eventTime,$eventTimeReadable"
        print(s"Writing $value\n")
        val d = r.nextFloat() * 100
        if (d < 2) {
          //induce random delay
          println("Argh! some network dealy")
          Thread.sleep((d*100).toLong)
        }
        new ProducerRecord[String, String](topic,"key", value)
      }

      numRecToProduce match {
        case Some(x) if x > 0 ⇒
          producer.send(generateStockRecord(topic))
          Thread.sleep(interval)
          produceRecord(Some(x - 1))

        case None ⇒
          producer.send(generateStockRecord(topic))
          Thread.sleep(interval)
          produceRecord(None)

        case _ ⇒
      }
    }

    produceRecord(numRecsToProduce)
  }
}

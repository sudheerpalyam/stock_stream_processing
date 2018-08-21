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
    val stockNames = Map(0 -> "AGL", 1 -> "AMC", 2 -> "ANZ", 3 -> "BHP", 4 -> "BXB", 5 -> "CBA", 6 -> "CSL", 7 -> "IAG", 8 -> "MQG", 9 -> "NAB", 10 -> "ORG", 11 -> "RIO", 12 -> "SCG", 13 -> "S32", 14 -> "SUN", 15 -> "TLS", 16 -> "TCL", 17 -> "WES", 18 -> "WBC", 19 -> "WPL", 20 -> "WOW")



    @tailrec
    def produceRecord(numRecToProduce: Option[Int]): Unit = {
      def generateStockRecord(topic: String): ProducerRecord[String, String] = {
        val stockName = stockNames(r.nextInt(3))
        val quantity = r.nextInt(1000)
        val price = r.nextFloat() * 1000
        val priceF = "%.2f".format(price)
        theBoolean = !theBoolean
        val tradeType = if (theBoolean) "SELL" else "BUY"

        val eventTime = System.currentTimeMillis()
        val eventTimeReadable = new Timestamp(eventTime.toLong)

        val value = s"$stockName,$tradeType,$priceF,$quantity,$eventTime,$eventTimeReadable"
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

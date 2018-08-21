package au.com.thoughtworks.assessment.spark.streaming.sources

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Calendar, Properties}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.util.{Random => r}

/**
  *
  * @author sudheerpalyam
  * @version 0.1
  *
  * A late data kafka producer to demonstrate how Spark Structrued Streaming handles late arriving data.
  */
object SimulateLateDateProducer {

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val topic = "stocks"
    var mCount = 1
    var theBoolean = true

    def generateStockRecord(stockName: String, price: Int = r.nextInt(150), topic: String = topic, lateby: Long = 0): ProducerRecord[String, String] = {
      val stockName = s"stock${r.nextInt(10)}"
      val quantity = r.nextInt(150)
      theBoolean = !theBoolean
      val tradeType = if (theBoolean) "SELL" else "BUY"

      val eventTime = System.currentTimeMillis()
      val eventTimeReadable = new Timestamp(eventTime.toLong)

      val nowTs = System.currentTimeMillis()
      val ts = nowTs - lateby
      val value = s"$stockName,$tradeType,$price,$quantity,$eventTime,$eventTimeReadable"

      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

      val cal = Calendar.getInstance()
      cal.setTimeInMillis(ts)

      val now = Calendar.getInstance()
      now.setTimeInMillis(nowTs)
      print(s"[$mCount] Writing $value at ${format.format(now.getTime)} with Event time = ${format.format(cal.getTime)}\n")
      mCount += 1
      new ProducerRecord[String, String](topic,"key", value)
    }

    producer.send(generateStockRecord("stock1", price = 75))
    Thread.sleep(1000)
    producer.send(generateStockRecord("stock2", price = 20))
    Thread.sleep(1000)
    producer.send(generateStockRecord("stock2", price = 20))
    Thread.sleep(8000)
    producer.send(generateStockRecord("stock2", price = 20))  //this message has a hidden importance, it increments the event time
    Thread.sleep(3000)
    producer.send(generateStockRecord("stock1", price = 50, lateby = 12000))
  }
}

package producer

import java.util.{Properties, UUID}
import kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import kafka.message.DefaultCompressionCodec

class AsyncProducer(brokerList: String) {
  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  private val props = new Properties()

  props.put("compression.codec", DefaultCompressionCodec.codec.toString)
  props.put("producer.type", "async")
  props.put("batch.num.messages", "200")
  props.put("metadata.broker.list", brokerList)
  props.put("message.send.max.retries", "5")
  props.put("request.required.acks", "-1")
  props.put("serializer.class", "kafka.serializer.StringEncoder")
  props.put("client.id", UUID.randomUUID().toString())

  private val producer = new Producer[String, String](new ProducerConfig(props))

  def send(topic: String, message: String): Unit = send(topic, List(message))

  def send(topic: String, messages: Seq[String]): Unit =
    try {
      logger.info("sending batch messages  to kafka queue: " + messages)
      val queueMessages = messages.map { message => new KeyedMessage[String, String](topic, message) }
      producer.send(queueMessages: _*)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()

    }

}

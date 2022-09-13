package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.Done
import cats.implicits.catsSyntaxEitherId
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KafkaProduceService() {
  private val config          = ConfigFactory.load()
  private val broker          = config.getString("localhost:9092")
  private val topic           = config.getString("merchant-producer-risk-score-data")

  def configureKafkaProducer():Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", broker)
    props.put("client.id", "producer")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props
  }

  def sendMessage(merchantId: String, listType: String): Future[Either[String, Done.type]] = {
    println("Inside producer.......")
    val props= configureKafkaProducer()
    val producer = new KafkaProducer[String, String](props)
    val t = System.currentTimeMillis()
    val data = new ProducerRecord[String, String](topic, merchantId, listType)
    producer.send(data)
    Future(Done.asRight[String])
  }
}

package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.actor.TypedActor.context
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import cats.implicits.catsSyntaxEitherId
import com.squareoneinsights.merchantportallagomapp.api.request.{MerchantRiskScoreProducer, RiskScoreReq}
import com.squareoneinsights.merchantportallagomapp.impl.common.JsonSerializer
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.Play.materializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class KafkaProduceService
                         {

                           import akka.actor.ActorSystem
                           import akka.stream.ActorMaterializer
                           implicit val sys = ActorSystem("MyTest")
                           implicit val mat = ActorMaterializer()
  private val config          = ConfigFactory.load()
  private val broker          = "localhost:9092"
  private val topicName       = "merchant-producer-risk-score-data"
  private val producerConfig  = config.getConfig("akka.kafka.producer")

  val configureKafkaProducer = {
    ProducerSettings(producerConfig, new StringSerializer, new JsonSerializer[MerchantRiskScoreProducer])
      .withBootstrapServers(broker)
  }



  def sendMessage(merchantId: String, listType: String): Future[Either[String, Done]] = {
    val producerSet = configureKafkaProducer.createKafkaProducer()
    val riskScoreReq = MerchantRiskScoreProducer.apply(merchantId, listType)
    val source = List(new ProducerRecord[String, MerchantRiskScoreProducer](topicName, riskScoreReq))
    val q: Future[Done] = Source(source)
      .runWith(Producer.plainSink(configureKafkaProducer, producerSet))
    println("Inside producer.......")
    q.map(_.asRight[String])
  }
}

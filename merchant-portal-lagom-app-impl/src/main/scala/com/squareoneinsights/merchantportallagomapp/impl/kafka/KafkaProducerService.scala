package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.actor.TypedActor.context
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import cats.implicits.catsSyntaxEitherId
import com.squareoneinsights.merchantportallagomapp.api.request.{MerchantRiskScoreProducer, RiskScoreReq, RiskType}
import com.squareoneinsights.merchantportallagomapp.impl.common.{JsonSerializer, MerchantPortalError, RiskSettingProducerErr}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.Play.materializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantRiskScore

class KafkaProduceService {
  implicit val sys = ActorSystem("MyTest")
  implicit val mat = ActorMaterializer()
  private val config          = ConfigFactory.load()
  private val broker          = config.getString("merchant.portal.risk.score.kafka.producer.url")
  private val topicName       = config.getString("merchant.portal.risk.score.kafka.producer.topic")
  private val producerConfig  = config.getConfig("akka.kafka.producer")

  val configureKafkaProducer = {
    ProducerSettings(producerConfig, new StringSerializer, new JsonSerializer[MerchantRiskScoreProducer])
      .withBootstrapServers(broker)
  }

  def sendMessage(merchantId: String, oldRiskListType: RiskType.Value, updatedListType: RiskType.Value, partnerId: Int): Future[Either[MerchantPortalError, Done]] = {
    println("Inside sendMessage.......")
    val producerSet = configureKafkaProducer.createKafkaProducer()
    val riskScoreReq = MerchantRiskScoreProducer.apply(partnerId, merchantId, oldRiskListType, updatedListType)
    val source = List(new ProducerRecord[String, MerchantRiskScoreProducer](topicName, riskScoreReq))
    val q: Future[Done] = Source(source)
      .runWith(Producer.plainSink(configureKafkaProducer, producerSet))
    println("Inside producer.......")
    q.map(_.asRight[RiskSettingProducerErr])
  }

  def senderMessagesQueue(mRisk: Seq[MerchantRiskScore]): Seq[Future[Either[MerchantPortalError, Done]]] = {
    mRisk.map { message =>
      sendMessage(message.merchantId, RiskType.withName(message.oldSliderPosition), RiskType.withName(message.updatedSliderPosition), message.partnerId)
    }
  }
}

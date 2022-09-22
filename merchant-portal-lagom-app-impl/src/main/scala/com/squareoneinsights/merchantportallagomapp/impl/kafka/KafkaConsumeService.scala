package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.routing.Broadcast
import akka.stream.scaladsl.{Sink, Source}
import com.squareoneinsights.merchantportallagomapp.api.request.RiskScoreReq
import com.squareoneinsights.merchantportallagomapp.impl.model.ConsumeRiskScore
import com.squareoneinsights.merchantportallagomapp.impl.repository.MerchantRiskScoreDetailRepo
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import java.util.{Collections, Properties, UUID}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}


class KafkaConsumeService(merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo,
                          implicit val system: ActorSystem) {

  private final val stringDeserializer  = new StringDeserializer
  private final val conf                = ConfigFactory.load()
  private val groupId                   = UUID.randomUUID().toString
  private val topic                     = conf.getString("merchant-portal-risk-score-kafka-consume-topic")
  private val kafkaBootstrapServers     = conf.getString("merchant-portal-risk-score-kafka-consume-url")

  val createConsumerConfig = {
    ConsumerSettings(system, stringDeserializer, stringDeserializer)
      .withBootstrapServers(kafkaBootstrapServers)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
      .withStopTimeout(0.seconds)
  }
    println(s"Inside run Received message")
   val x: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] = Consumer.committableSource(createConsumerConfig, Subscriptions.topics(topic))
  println("value of x ---->"+x)
  x.map(consumerMsg => {
      println("consume message ->"+consumerMsg)
      val message = consumerMsg.record.value()
      Try(Json.parse(stringDeserializer.deserialize("merchant-risk-score-data", message.getBytes())).as[ConsumeRiskScore]) match {
        case Success(riskScoreObj) => {
          println("success -->"+riskScoreObj)
          val r = RiskScoreReq.apply(riskScoreObj.merchantId, riskScoreObj.isApproved)
          merchantRiskScoreDetailRepo.updatedIsApprovedFlag(r).map {
            case Left(err) => throw new RuntimeException("Unable to save ifrm flag value \n Error:"+err)
            case Right(value) => value
          }
        }
        case Failure(exception) => println("Invalid message body " + message, exception)
      }
    }).runWith(Sink.ignore)
}

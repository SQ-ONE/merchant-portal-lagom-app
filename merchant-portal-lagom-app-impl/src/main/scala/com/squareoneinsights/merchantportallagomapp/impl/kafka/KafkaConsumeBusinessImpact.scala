package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Sink, Source}
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.squareoneinsights.merchantportallagomapp.api.request.BusinessImpactDetail
import com.squareoneinsights.merchantportallagomapp.impl.repository.{BusinessImpactRepo, MerchantRiskScoreDetailRepo}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class KafkaConsumeBusinessImpact(businessImpactRepo: BusinessImpactRepo,
                                 implicit val system: ActorSystem) {

  private final val stringDeserializer = new StringDeserializer
  private final val conf = ConfigFactory.load()
  private val groupId = UUID.randomUUID().toString
  private val topic = conf.getString("merchant.portal.business.kafka.consume.topic")
  private val kafkaBootstrapServers = conf.getString("merchant.portal.business.kafka.consumer-url")
  println(s"Inside KafkaConsumeBusinessImpact.................")
  val createConsumerConfig = {
    ConsumerSettings(system, stringDeserializer, stringDeserializer)
      .withBootstrapServers(kafkaBootstrapServers)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
      .withStopTimeout(0.seconds)
  }
  val x: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] = Consumer.committableSource(createConsumerConfig, Subscriptions.topics(topic))

  x.map(consumerMsg => {
    val message = consumerMsg.record.value()
    Try(Json.parse(stringDeserializer.deserialize(topic, message.getBytes())).as[BusinessImpactDetail]) match {
      case Success(merchantBusinessData) => {
        println(s"Inside KafkaConsumeBusinessImpact Success.................")
        import merchantBusinessData._
        val r = BusinessImpactDetail.apply(partnerId, merchantId, lowPaymentAllowed, lowPaymentReview, lowPaymentBlocked, medPaymentAllowed, medPaymentReview, medPaymentBlocked, highPaymentAllowed, highPaymentReview, highPaymentBlocked, updatedTimeStamp)
        businessImpactRepo.save(r).map {
          case Left(err) => {
            println(s"Inside KafkaConsumeBusinessImpact Left.................")
            throw BadRequest(s"Failed to consumed and insert record to database \n Error: ${err}")
          }
          case Right(value) => {
            println(s"Inside KafkaConsumeBusinessImpact Right................."+value)
            value
          }
        }
      }
      case Failure(exception) => println("Invalid message body " + message, exception)
    }
  }).runWith(Sink.ignore)
}

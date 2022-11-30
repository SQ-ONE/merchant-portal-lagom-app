package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.squareoneinsights.merchantportallagomapp.impl.model.{LogCreated, MerchantCaseCreated, MerchantCaseUpdated, MerchantTransaction, MerchantTransactionEvent}
import com.squareoneinsights.merchantportallagomapp.impl.repository.MerchantTransactionRepo
import com.squareoneinsights.merchantportallagomapp.impl.util.MerchantUtil
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.Timestamp
import scala.concurrent.duration.DurationInt
import java.util.UUID
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class KafkaTransactionConsumer(repo: MerchantTransactionRepo, implicit val system: ActorSystem) {

  private final val stringDeserializer = new StringDeserializer
  private final val conf               = ConfigFactory.load()
  private val groupId                  = UUID.randomUUID().toString
  private val topic                    = conf.getString("merchant.portal.transaction.kafka.consume.topic")
  private val kafkaBootstrapServers = conf.getString("merchant.portal.transaction.kafka.consumer-url")
  println(s"Inside KafkaConsumerTransactionAndLog.................")
  val createConsumerConfig = {
    ConsumerSettings(system, stringDeserializer, stringDeserializer)
      .withBootstrapServers(kafkaBootstrapServers)
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
      .withStopTimeout(0.seconds)
  }
  val x: Source[ConsumerMessage.CommittableMessage[String, String], Consumer.Control] =
    Consumer.committableSource(createConsumerConfig, Subscriptions.topics(topic))

  x.map(consumerMsg => {
    val message = consumerMsg.record.value()
    Try(Json.parse(stringDeserializer.deserialize(topic, message.getBytes())).as[MerchantTransactionEvent]) match {
      case Success(transactions) =>
        transactions match {
          case x:MerchantCaseUpdated =>
            repo.updateByCaseRefNo(x)
              .map {
                case Left(err) => {
                  println(s"Inside KafkaTransactionConsumer Left.................err: $err")
                }
                case Right(value) => {
                  println(s"Inside KafkaTransactionConsumer Right................."+value)
                  value
                }
              }
          case MerchantCaseCreated(
                partnerId,
                merchantId,
                txnId,
                caseRefNo,
                txnTimestamp,
                txnAmount,
                ifrmVerdict,
                caseStatus,
                channel,
                alertType,
                responseCode,
                customerId, // entityId
                txnType: String,    // instrument
                lat: Double,
                long: Double,
                txnResult: String,
                violationDetails,
                investigatorComment: String,
                caseId
              ) =>
            repo.saveTransaction(
              MerchantTransaction(
                partnerId: Int,
                merchantId: String,
                txnId: String,
                caseRefNo: String,
                Timestamp.valueOf(txnTimestamp): Timestamp,
                txnAmount: Int,
                ifrmVerdict: String,
                caseStatus,
                channel,
                alertType,
                responseCode,
                customerId,
                txnType,
                MerchantUtil.findLocation(lat, long),
                txnResult,
                violationDetails,
                investigatorComment,
                caseId
              )
            ) .map {
              case Left(err) => {
                println(s"Inside KafkaTransactionConsumer Left.................err: $err")
              }
              case Right(value) => {
                println(s"Inside KafkaTransactionConsumer Right................."+value)
                value
              }
            }
          case x:LogCreated =>
            println("data  ----"+x)
            repo.insertCaseLogs(x)
              .map {
                case Left(err) => {
                  println(s"Inside KafkaTransactionConsumer Left.................err: $err")
                }
                case Right(value) => {
                  println(s"Inside KafkaTransactionConsumer Right................."+value)
                  value
                }
              }

          case _ =>
            println("Invalid message body " + message)
        }
      case Failure(exception) => println("Invalid message body " + message, exception)
    }
  }).runWith(Sink.ignore)
}

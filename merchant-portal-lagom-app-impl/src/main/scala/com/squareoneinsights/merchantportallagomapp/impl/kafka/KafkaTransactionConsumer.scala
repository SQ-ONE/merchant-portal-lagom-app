package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantTransaction
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantTransactionKafka
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantTransactionLog
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantTransactionLogKafka
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantTransactions
import com.squareoneinsights.merchantportallagomapp.impl.repository.MerchantTransactionRepo
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json

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
  private val topic                    = conf.getString("merchant.portal.business.kafka.consume.topic")
  println(s"Inside KafkaConsumeBusinessImpact.................")
  val createConsumerConfig = {
    ConsumerSettings(system, stringDeserializer, stringDeserializer)
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
    Try(Json.parse(stringDeserializer.deserialize(topic, message.getBytes())).as[MerchantTransactions]) match {
      case Success(transactions) =>
        transactions match {
          case MerchantTransactionKafka(
                partnerId,
                merchantId,
                txnId,
                caseRefNo,
                txnTimestamp,
                txnAmount,
                ifrmVerdict,
                investigationStatus,
                channel,
                txnType,
                responseCode,
                customerId,
                instrument,
                location,
                txnResult,
                violationDetails,
                investigatorComment,
                caseId
              ) =>
            repo.saveTransaction(
              MerchantTransaction(
                partnerId,
                merchantId,
                txnId,
                caseRefNo,
                Timestamp.valueOf(txnTimestamp),
                txnAmount,
                ifrmVerdict,
                investigationStatus,
                channel,
                txnType,
                responseCode,
                customerId,
                instrument,
                location,
                txnResult,
                violationDetails,
                investigatorComment,
                caseId
              )
            )
          case MerchantTransactionLogKafka(txnId, logName, logValue) =>
            repo.saveTransactionLog(MerchantTransactionLog(None, txnId, logName, logValue))
        }
      case Failure(exception) => println("Invalid message body " + message, exception)
    }
  }).runWith(Sink.ignore)
}

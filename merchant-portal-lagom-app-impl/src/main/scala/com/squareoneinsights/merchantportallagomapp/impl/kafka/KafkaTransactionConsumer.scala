package com.squareoneinsights.merchantportallagomapp.impl.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantCaseCloser
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantCaseData
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantCaseNotation
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
          case MerchantCaseData(
                partnerId: Int,
                merchantId: String,
                txnId: String,
                caseRefNo: String,
                txnTimestamp: String,
                txnAmount: Int,
                ifrmVerdict: String,
                investigationStatus: String,
                channel: String,
                alertTypeId: String,
                responseCode: String,
                customerId: String, // entityId
                txnType: String,    // instrument
                lat: Double,
                long: Double,
                txnResult: String,
                violationDetails: String,
                investigatorComment: String,
                caseId: String
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
                investigationStatus: String,
                channel: String,
                txnType: String,
                responseCode: String,
                customerId: String,
                txnType: String,
                s"$lat, $long": String,
                txnResult: String,
                violationDetails: String,
                investigatorComment: String,
                caseId: String
              )
            )

          case MerchantCaseCloser(caseRefNo, investigationStatus) =>
            repo.updateByCaseRefNo(MerchantCaseCloser(caseRefNo, investigationStatus))
          case MerchantCaseNotation(caseId, comment) => repo.updateByCaseId(MerchantCaseNotation(caseId, comment))

          case MerchantTransactionLogKafka(txnId, logName, logValue) =>
            repo.saveTransactionLog(MerchantTransactionLog(None, txnId, logName, logValue))
        }
      case Failure(exception) => println("Invalid message body " + message, exception)
    }
  }).runWith(Sink.ignore)
}

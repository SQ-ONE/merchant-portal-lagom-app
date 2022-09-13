package com.squareoneinsights.merchantportallagomapp.impl.kafka

import com.squareoneinsights.merchantportallagomapp.api.request.RiskScoreReq
import com.squareoneinsights.merchantportallagomapp.impl.model.ConsumeRiskScore
import com.squareoneinsights.merchantportallagomapp.impl.repository.MerchantRiskScoreDetailRepo
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.{Collections, Properties}
import java.util.{Collections, Properties}


class KafkaConsumeService(merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo) {

  private val config = ConfigFactory.load()
 println("Inside KafkaConsumeService...")
  val brokers = "localhost:9092"
  val groupId = "group-1"
  val topic = "merchant-risk-score-data"
  val props = createConsumerConfig(brokers, groupId)
  val consumer = new KafkaConsumer[String, String](props)

  def createConsumerConfig(brokers: String, groupId: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    //props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    //props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props
  }

  val run = {
    println(s"Inside run Received message ")
    consumer.subscribe(Collections.singletonList(topic))
    while (true) {
      val records = consumer.poll(1000)
      for (record <- records) {
        val r = ConsumeRiskScore.decoder(record.value())
        println("consumed message----------->"+r)
        val q = RiskScoreReq.apply(r.merchantId, r.isApproved)
        merchantRiskScoreDetailRepo.updatedIsApprovedFlag(q).map {
          case Left(err) => throw new RuntimeException("Unable to save ifrm flag value \n Error:"+err)
          case Right(value) => value
        }
        println(s"Received user Detail message:   ${q}")
      }
    }
  }
}

package com.squareoneinsights.merchantportallagomapp.impl.util

import akka.actor.{Actor, ActorLogging}
import cats.data.EitherT
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.squareoneinsights.merchantportallagomapp.impl.kafka.KafkaProduceService
import com.squareoneinsights.merchantportallagomapp.impl.repository.MerchantRiskScoreDetailRepo
import cats.implicits._
import cats.Functor

import scala.concurrent.ExecutionContext


class UpdateRiskToIfrmActor(kafkaProduceService: KafkaProduceService,
                            merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo)
                           (implicit ec: ExecutionContext)
  extends Actor with ActorLogging {

  override def receive: Receive = {
    case "Update" => {
      log.info("Call to getInitialRiskScore")
      getInitialRiskScore
    }
  }

  def getInitialRiskScore() = {
    merchantRiskScoreDetailRepo.checkInitialRiskSet.flatMap { x =>
      x match {
        case Left(value) => throw BadRequest("Failed to get Initial Risk Type")
        case Right(value) => kafkaProduceService.senderMessagesQueue(value).head
      }
    }
  }
}

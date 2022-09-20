package com.squareoneinsights.merchantportallagomapp.impl

import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import akka.Done
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.util.Timeout
import cats.data.EitherT
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.squareoneinsights.merchantportallagomapp.api.request.{
  MerchantDetailsReq,
  MerchantRiskScoreReq
}
import com.squareoneinsights.merchantportallagomapp.api.response.{
  MerchantDetailsResp,
  MerchantImpactDataResp,
  MerchantRiskScoreResp
}
import com.squareoneinsights.merchantportallagomapp.impl.kafka.KafkaProduceService
import com.squareoneinsights.merchantportallagomapp.impl.repository.{
  BusinessImpactRepo,
  MerchantRiskScoreDetailRepo,
  LogOutRepo
}

class MerchantportallagomappServiceImpl(
    merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo,
    kafkaProduceService: KafkaProduceService,
    businessImpactRepo: BusinessImpactRepo,
    logOutRepo: LogOutRepo
)(implicit ec: ExecutionContext)
    extends MerchantportallagomappService {

  implicit val timeout = Timeout(5.seconds)

  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall {
    _ =>
      // Look up the sharded entity (aka the aggregate instance) for the given ID.
      println("Fine.................")
      // Ask the aggregate instance the Hello command.
      import scala.concurrent._
      Future.successful("Ok....")
  }

  override def getRiskScore(
      merchantId: String
  ): ServiceCall[NotUsed, MerchantRiskScoreResp] =
    ServerServiceCall { _ =>
      merchantRiskScoreDetailRepo.fetchRiskScore(merchantId).map {
        case Left(err)   => throw BadRequest(s"Error: ${err}")
        case Right(data) => data
      }
    }

  override def addRiskType
      : ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp] =
    ServerServiceCall { riskJson =>
      val resp = for {
        toRedis <- EitherT(
          merchantRiskScoreDetailRepo.insertRiskScore(riskJson)
        )
        //toRdbms <- EitherT(addRiskToRedis.publishMerchantRiskType(riskJson.merchantId, riskJson.riskType))
        toKafka <- EitherT(
          kafkaProduceService.sendMessage(
            riskJson.merchantId,
            riskJson.oldRisk,
            riskJson.updatedRisk
          )
        )
      } yield (toKafka)
      resp.value.map {
        case Left(err) => throw new MatchError(err)
        case Right(_) => {
          val merchantRiskResp = MerchantRiskScoreResp.apply(
            riskJson.merchantId,
            riskJson.oldRisk,
            riskJson.updatedRisk,
            "approved"
          )
          if (riskJson.updatedRisk == "High")
            merchantRiskResp.copy(approvalFlag = "pending")
          else merchantRiskResp
        }
      }
    }

  override def logOutMerchant
      : ServiceCall[MerchantDetailsReq, MerchantDetailsResp] =
    ServerServiceCall { logOutJson =>
      val resp = for {
        toRedis <- EitherT(
          logOutRepo.updatedToLogout(logOutJson)
        )
        //toRdbms <- EitherT(addRiskToRedis.publishMerchantRiskType(riskJson.merchantId, riskJson.riskType))
        toKafka <- EitherT(
          kafkaProduceService.sendMessage(
            logOutJson.merchantId.toString,
            logOutJson.mccCode.toString,
            logOutJson.isLoginActive
          )
        )
      } yield (toKafka)
      resp.value.map {
        case Left(err) => throw new MatchError(err)
        case Right(_) => {
          val merchantRiskResp = MerchantDetailsResp.apply(
            logOutJson.merchantId,
            logOutJson.userName,
            logOutJson.merchantName,
            logOutJson.mccCode,
            logOutJson.isLoginActive,
            "200 OK"
          )
          merchantRiskResp
        }
      }

    }

  override def getMerchantImpactData(
      merchantId: String
  ): ServiceCall[NotUsed, MerchantImpactDataResp] =
    ServerServiceCall { _ =>
      businessImpactRepo.fetchBusinessDetail(merchantId).map {
        case Left(err)   => throw BadRequest(s"Error: ${err}")
        case Right(data) => MerchantImpactDataResp.setMerchantBusinessData(data)
      }
    }
}

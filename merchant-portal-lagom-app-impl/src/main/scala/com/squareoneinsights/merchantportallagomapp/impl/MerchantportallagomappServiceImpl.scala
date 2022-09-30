package com.squareoneinsights.merchantportallagomapp.impl

import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import akka.Done
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import cats.syntax.either._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import cats.data.EitherT
import cats.implicits.{catsStdInstancesForFuture, catsSyntaxEitherId}
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, ResponseHeader}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.squareoneinsights.merchantportallagomapp.api.request.{
  MerchantLoginReq,
  MerchantRiskScoreReq
}
import com.squareoneinsights.merchantportallagomapp.api.response.{
  MerchantImpactDataResp,
  MerchantLoginResp,
  MerchantRiskScoreResp
}

import com.squareoneinsights.merchantportallagomapp.impl.common.{
  JwtTokenGenerator,
  RedisUtility,
  TokenContent
}
import com.squareoneinsights.merchantportallagomapp.impl.kafka.KafkaProduceService
import com.squareoneinsights.merchantportallagomapp.impl.repository.{
  BusinessImpactRepo,
  MerchantLoginRepo,
  MerchantRiskScoreDetailRepo
}
import org.joda.time.DateTime

import scala.util.Try

import com.squareoneinsights.merchantportallagomapp.api.request.{
  MerchantLoginReq,
  MerchantLogoutReq,
  MerchantRiskScoreReq
}

import com.squareoneinsights.merchantportallagomapp.impl.repository.MerchantLogoutRepo

class MerchantportallagomappServiceImpl(
    merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo,
    kafkaProduceService: KafkaProduceService,
    businessImpactRepo: BusinessImpactRepo,
    redisUtility: RedisUtility,
    merchantLoginRepo: MerchantLoginRepo,
    merchantLogoutRepo: MerchantLogoutRepo
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
      println(
        "Inside getRiskScore ****************************************--->"
      )
      merchantRiskScoreDetailRepo.fetchRiskScore(merchantId).map {
        case Left(err) => {
          println("Inside getRiskScore Left--->" + err)
          throw BadRequest(s"Error: ${err}")
        }
        case Right(data) => {
          println("Inside getRiskScore--->" + data)
          data
        }
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

  override def getMerchantImpactData(
      merchantId: String
  ): ServiceCall[NotUsed, MerchantImpactDataResp] =
    ServerServiceCall { _ =>
      businessImpactRepo.fetchBusinessDetail(merchantId).map {
        case Left(err)   => throw BadRequest(s"Error: ${err}")
        case Right(data) => MerchantImpactDataResp.setMerchantBusinessData(data)
      }
    }

  override def login: ServiceCall[MerchantLoginReq, MerchantLoginResp] =
    ServerServiceCall { req =>
      val jwt = JwtTokenGenerator.generate(
        TokenContent("11", "vaibhav"),
        new DateTime().plusMinutes(50).toDate
      )

      println(jwt.authToken)
      val dataef = for {

        merchant <- EitherT(merchantLoginRepo.getUserByName(req.userName))
      } yield merchant
      dataef.value.map {
        case Left(err) => throw BadRequest(s"Error: ${err}")
        case Right(data) =>
          MerchantLoginResp(
            data.merchantId,
            data.merchantName,
            "scala1",
            data.isLoggedInFlag
          )
      }
    }

  override def logOutMerchant: ServiceCall[MerchantLogoutReq, Done] =
    ServerServiceCall { logOutJson =>
      val resp: EitherT[Future, String, Done] = for {
        _ <- EitherT(
          merchantLogoutRepo.logoutMerchant(logOutJson.userName)
        )
        _ <- EitherT(
          merchantLogoutRepo.updateActivity(logOutJson.userName)
        )
        toRedis <- EitherT(delTokenFromRedis(logOutJson.userName))

      } yield (toRedis)
      resp.value.map {
        case Left(err) => throw new MatchError(err)
        case Right(_) =>
          Done
      }

    }

  def addTokenToRedis(
      userName: String,
      authToken: String
  ): Future[Either[String, Done]] = Future {
    Either
      .fromTry(Try(redisUtility.addToken(userName, authToken)))
      .leftMap { case ex =>
        ex.getMessage
      }
      .map(_ => Done)
  }

  def delTokenFromRedis(userName: String): Future[Either[String, Done]] =
    Future {
      Either
        .fromTry(Try(redisUtility.deleteToken(userName)))
        .leftMap { case ex =>
          ex.getMessage
        }
        .map(_ => Done)
    }

}

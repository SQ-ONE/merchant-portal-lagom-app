package com.squareoneinsights.merchantportallagomapp.impl

import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.api.ServiceCall
import cats.syntax.either._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import cats.data.EitherT
import cats.implicits.{catsStdInstancesForFuture, catsSyntaxEitherId}
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, ResponseHeader}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.squareoneinsights.merchantportallagomapp.api.request.{MerchantLoginReq, MerchantRiskScoreReq}
import com.squareoneinsights.merchantportallagomapp.api.response.{MerchantImpactDataResp, MerchantLoginResp, MerchantRiskScoreResp}
import com.squareoneinsights.merchantportallagomapp.impl.authenticator.WindowsADAuthenticator
import com.squareoneinsights.merchantportallagomapp.impl.common.{JwtTokenGenerator, RedisUtility, TokenContent}
import com.squareoneinsights.merchantportallagomapp.impl.kafka.KafkaProduceService
import com.squareoneinsights.merchantportallagomapp.impl.repository.{BusinessImpactRepo, MerchantOnboardRiskScore, MerchantRiskScoreDetailRepo}
import com.squareoneinsights.merchantportallagomapp.impl.repository.{BusinessImpactRepo, MerchantLoginRepo, MerchantRiskScoreDetailRepo}
import org.joda.time.DateTime

import scala.util.Try

class MerchantportallagomappServiceImpl(merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo,
                                        kafkaProduceService: KafkaProduceService,
                                        merchantOnboardRiskScore: MerchantOnboardRiskScore,
                                        businessImpactRepo: BusinessImpactRepo,
                                        merchantLoginRepo:MerchantLoginRepo,
                                        redisUtility: RedisUtility,
                                        system: ActorSystem)
                                       (implicit ec: ExecutionContext)
  extends MerchantportallagomappService {

  implicit val timeout = Timeout(5.seconds)
  val maxAgeInSeconds = 36000
  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall {
    _ =>
      // Look up the sharded entity (aka the aggregate instance) for the given ID.
     println("Fine.................")
      // Ask the aggregate instance the Hello command.
       import scala.concurrent._
      Future.successful("Ok....")
  }

 override def getRiskScore(merchantId: String): ServiceCall[NotUsed, MerchantRiskScoreResp] =
    ServerServiceCall { _ =>
      println("Inside getRiskScore ****************************************--->")

      val getMerchantRisk = for {
        a <- EitherT(merchantRiskScoreDetailRepo.fetchRiskScore(merchantId))
        ifExist <- EitherT(merchantRiskScoreDetailRepo.checkRiskScoreExist(merchantId))
        b <- EitherT(if(ifExist) merchantRiskScoreDetailRepo.fetchRiskScore(merchantId) else getMerchantOnboardRiskData(merchantId))
      } yield (b)
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

  def getMerchantOnboardRiskData(merchantId: String): Future[Either[String, MerchantRiskScoreResp]] = {
    val getAndUpdateQuery = for {
      onboardRiskScore <- EitherT(merchantOnboardRiskScore.getInitialRiskType(merchantId))
      toRedis <- EitherT(merchantRiskScoreDetailRepo.insertRiskScore(MerchantRiskScoreReq.apply(merchantId, onboardRiskScore, onboardRiskScore)))
      toKafka <- EitherT(kafkaProduceService.sendMessage(merchantId, onboardRiskScore, onboardRiskScore))
    } yield(MerchantRiskScoreResp.getMerchantObj(merchantId, onboardRiskScore))
     getAndUpdateQuery.value
  }

   override def addRiskType: ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp] =
    ServerServiceCall { riskJson =>
      val resp = for {
        toRedis <- EitherT(merchantRiskScoreDetailRepo.insertRiskScore(riskJson))
        //toRdbms <- EitherT(addRiskToRedis.publishMerchantRiskType(riskJson.merchantId, riskJson.riskType))
        toKafka <- EitherT(kafkaProduceService.sendMessage(riskJson.merchantId, riskJson.oldRisk, riskJson.updatedRisk))
      } yield(toKafka)
      resp.value.map {
        case Left(err) => throw new MatchError(err)
        case Right(_) => {
          val merchantRiskResp = MerchantRiskScoreResp.apply(riskJson.merchantId, riskJson.oldRisk, riskJson.updatedRisk, "approve")
          if (riskJson.updatedRisk == "High") merchantRiskResp.copy(approvalFlag = "pending") else merchantRiskResp
        }
      }
    }

  override def getMerchantImpactData(merchantId: String): ServiceCall[NotUsed, MerchantImpactDataResp] =
    ServerServiceCall { _ =>
      businessImpactRepo.fetchBusinessDetail(merchantId).map {
        case Left(err) => throw BadRequest(s"Error: ${err}")
        case Right(data) => MerchantImpactDataResp.setMerchantBusinessData(data)
      }
    }

  override def login = ServerServiceCall{(requestHeader, userLoginDetails) =>
    val resp = for {
     merchant <- EitherT(merchantLoginRepo.getUserByName(userLoginDetails.userName))
   //  _ <- EitherT(WindowsADAuthenticator.authenticateUser(userLoginDetails.userName, userLoginDetails.password))
     tokenContent <- EitherT.rightT(TokenContent(merchant.merchantId,merchant.merchantName))
      jwt <- EitherT(JwtTokenGenerator.createToken(tokenContent))
     _ <- EitherT(merchantLoginRepo.updateMerchantLoginInfo(merchant))
      _ <- EitherT(addTokenToRedis(merchant.merchantId, jwt.refreshToken))
    } yield (merchant, jwt)
    val response = resp.value.map {
        case Left(err) => throw BadRequest(s"Error: ${err}")
        case Right((data,auth)) =>
          (MerchantLoginResp(data.merchantId,data.merchantId,data.merchantName,data.merchantMcc,true), auth)
      }

    response map {
      case (res , auth) =>
      val header =  ResponseHeader.Ok.withHeader("Set-Cookie",
          s"authToken=${auth.authToken}; Max-Age=${maxAgeInSeconds}")
        header -> res
    }
  }

  def addTokenToRedis(userName: String, authToken: String):
  Future[Either[String, Done]] = Future {
    Either.fromTry(Try(redisUtility.addToken(userName, authToken))).leftMap {
        case ex => ex.getMessage
      }.map(_ => Done)
    }

}

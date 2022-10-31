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
import com.squareoneinsights.merchantportallagomapp.api.request.{LogOutReq, MerchantLoginReq, MerchantRiskScoreReq, RiskType, TransactionFilterReq}
import com.squareoneinsights.merchantportallagomapp.api.response.{BusinessImpact, MerchantImpactDataResp, MerchantLoginResp, MerchantRiskScoreResp, MerchantTransactionResp}
import com.squareoneinsights.merchantportallagomapp.impl.MerchantportallagomappServiceImpl.{maxAgeInSeconds, tokenValidityInMinutes}
import com.squareoneinsights.merchantportallagomapp.impl.common.{JwtTokenGenerator, Pac4jAuthorizer, RedisUtility, TokenContent}
import com.squareoneinsights.merchantportallagomapp.api.response.{BusinessImpact, MerchantImpactDataResp, MerchantLoginResp, MerchantRiskScoreResp, ResponseMessage}
import com.squareoneinsights.merchantportallagomapp.impl.common.{AddMerchantErr, CreateLogInTokenErr, GetBusinessImpactErr, GetMerchantErr, GetMerchantOnboard, GetUserDetailErr, JwtTokenGenerator, LogoutErr, LogoutRedisErr, MerchantPortalError, MerchantTxnErr, RedisUtility, TokenContent, UpdateLogInRedisErr}
import com.squareoneinsights.merchantportallagomapp.impl.kafka.KafkaProduceService
import com.squareoneinsights.merchantportallagomapp.impl.repository.{BusinessImpactRepo, FilterTXN, MerchantLoginRepo, MerchantOnboardRiskScore, MerchantRiskScoreDetailRepo, MerchantTransactionRepo}
import com.squareoneinsights.merchantportallagomapp.impl.util.MerchantUtil
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}

class MerchantportallagomappServiceImpl(merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo,
                                        kafkaProduceService: KafkaProduceService,
                                        merchantOnboardRiskScore: MerchantOnboardRiskScore,
                                        businessImpactRepo: BusinessImpactRepo,
                                        merchantLoginRepo:MerchantLoginRepo,
                                        merchantTransactionRepo:MerchantTransactionRepo,
                                        redisUtility: RedisUtility,
                                        system: ActorSystem)
                                       (implicit ec: ExecutionContext)
  extends  Pac4jAuthorizer(system)  with MerchantportallagomappService {

  val logger: Logger = LoggerFactory.getLogger(getClass)
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
    authorize((tokenContent, _) =>
    ServerServiceCall { _ =>
      println("Inside getRiskScore ****************************************--->")

      val getMerchantRisk = for {
        //a <- EitherT(merchantRiskScoreDetailRepo.fetchRiskScore(merchantId))
        ifExist <- EitherT(merchantRiskScoreDetailRepo.checkRiskScoreExist(merchantId))
        b <- EitherT(if (ifExist) merchantRiskScoreDetailRepo.fetchRiskScore(merchantId) else getMerchantOnboardRiskData(merchantId))
      } yield (b)
      getMerchantRisk.value.map {
        case Left(err) => {
          err match {
            case er: GetMerchantErr => throw BadRequest(er.err)
            case getM: GetMerchantOnboard => throw BadRequest(getM.err)
          }
        }
        case Right(data) => {
          logger.info("Inside getRiskScore--->" + data)
          data
        }
      }
    })

  def getMerchantOnboardRiskData(merchantId: String): Future[Either[MerchantPortalError, MerchantRiskScoreResp]] = {
    val getAndUpdateQuery = for {
      onboardRiskScore <- EitherT(merchantOnboardRiskScore.getInitialRiskType(merchantId))
      toRedis <- EitherT(merchantRiskScoreDetailRepo.insertRiskScore(MerchantRiskScoreReq.apply(merchantId, RiskType.withName(onboardRiskScore), RiskType.withName(onboardRiskScore))))
      toKafka <- EitherT(kafkaProduceService.sendMessage(merchantId, RiskType.withName(onboardRiskScore), RiskType.withName(onboardRiskScore)))
    } yield (MerchantRiskScoreResp.getMerchantObj(merchantId, onboardRiskScore))
    getAndUpdateQuery.value
  }

  override def addRiskType: ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp] =
    authorize((tokenContent, _) =>
    ServerServiceCall { riskJson =>
      val resp = for {
        toRedis <- EitherT(merchantRiskScoreDetailRepo.updateRiskScore(riskJson))
        //toRdbms <- EitherT(addRiskToRedis.publishMerchantRiskType(riskJson.merchantId, riskJson.riskType))
        toKafka <- EitherT(kafkaProduceService.sendMessage(riskJson.merchantId, riskJson.oldRisk, riskJson.updatedRisk))
      } yield (toKafka)
      resp.value.map {
        case Left(err) => {
          err match {
            case addEr: AddMerchantErr => throw BadRequest(addEr.err)
            case er => throw new MatchError(er)
          }
        }
        case Right(_) => {
          val merchantRiskResp = MerchantRiskScoreResp.apply(riskJson.merchantId, riskJson.oldRisk, riskJson.updatedRisk, "Approve")
          if (riskJson.updatedRisk == "High") merchantRiskResp.copy(approvalFlag = "Pending") else merchantRiskResp
        }
      }
    })

  override def getMerchantImpactData(merchantId: String): ServiceCall[NotUsed, BusinessImpact] =
    authorize((tokenContent, _) =>
    ServerServiceCall { _ =>
      businessImpactRepo.fetchBusinessDetail(merchantId).map {
        case Left(err) => {
          err match {
            case getE: GetBusinessImpactErr => throw BadRequest(getE.err)
          }
        }
        case Right(data) => {
          val x = MerchantImpactDataResp.setMerchantBusinessData(data)
          BusinessImpact.apply(x)
        }
      }
    })

  override def login = ServerServiceCall{(requestHeader, userLoginDetails) =>
    val resp = for {
      merchant <- EitherT(merchantLoginRepo.getUserByName(userLoginDetails.userName))
      //  _ <- EitherT(WindowsADAuthenticator.authenticateUser(userLoginDetails.userName, userLoginDetails.password))
      tokenContent <- EitherT.rightT(TokenContent(merchant.merchantId ,merchant.merchantName))
      jwt <- EitherT(JwtTokenGenerator.createToken(tokenContent, new DateTime().plusMinutes(tokenValidityInMinutes).toDate))
      _ <- EitherT(merchantLoginRepo.updateMerchantLoginInfo(merchant))
      _ <- EitherT(redisUtility.addTokenToRedis(merchant.merchantId, jwt.authToken))
      jwtFreshToken <- EitherT(JwtTokenGenerator.generateRefreshToken(tokenContent, new DateTime().plusMinutes(tokenValidityInMinutes).toDate))
      _ <- EitherT(redisUtility.addTokenToRedis(merchant.merchantId+"_refreshToken", jwtFreshToken.refreshToken.getOrElse("")))
    } yield (merchant, jwt)
    val response = resp.value.map {
      case Left(err) => {
        err match {
          case ex: LogoutRedisErr => throw BadRequest(ex.err)
          case gErr: GetUserDetailErr => throw BadRequest(gErr.err)
          case cErr: CreateLogInTokenErr => throw BadRequest(cErr.err)
          case uErr: UpdateLogInRedisErr => throw BadRequest(uErr.err)
        }
      }
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

  override def logOut: ServiceCall[LogOutReq, ResponseMessage] =
    authorize((tokenContent, _) =>
    ServerServiceCall { req =>
    val query = for {
      merchant <- EitherT(merchantLoginRepo.getUserByName(req.userName))
      updateStatus <- EitherT(merchantLoginRepo.updateMerchantLoginStatus(req.userName))
      del <- EitherT(redisUtility.deleteTokenFromRedis(req.userName))
      _<- EitherT(merchantLoginRepo.logoutActivity(merchant.merchantId))
      delR <- EitherT(redisUtility.deleteTokenFromRedis(req.userName+"_refreshToken"))
    } yield(del)
    query.value.map {
      case Left(err) => {
        logger.info(s"LogOut Failed. \n Error: ${err}")
        err match {
          case lerr: LogoutErr => throw BadRequest(lerr.err)
          case errl: LogoutRedisErr => throw BadRequest(errl.err)
        }
      }
      case Right(resp) => {
        ResponseMessage.apply("Logout Successfully")
      }
    }
  })

  override def getTransactions(txnType: String, merchantId: String): ServiceCall[NotUsed, List[MerchantTransactionResp]] =
    authorize((tokenContent, _) =>
    ServerServiceCall { req =>

    val resp = for {
      merchant <- EitherT(merchantTransactionRepo.getTransactionsByType(merchantId, txnType))
    } yield merchant
    resp.value.map {
      case Left(err) =>
        err match {
          case ex: MerchantTxnErr => throw BadRequest(ex.err)
        }
      case Right((data)) =>
        data.map { m =>
          MerchantTransactionResp(m.txnId,
            m.caseRefNo,
            m.txnTimestamp.toString,
            m.txnAmount,
            m.ifrmVerdict,
            m.investigationStatus,
            m.channel,
            m.txnType,
            m.responseCode)
        }.toList
    }
  })

  override def getTransactionsBySearch(txnType: String, merchantId: String): ServiceCall[TransactionFilterReq, List[MerchantTransactionResp]] =
    authorize((tokenContent, _) =>
    ServerServiceCall { req =>
    val request = req.filterCondition.map(x => FilterTXN(x.key, MerchantUtil.conditionMap(x.condition), x.value))
    val resp = for {
      merchant <- EitherT(merchantTransactionRepo.getTransactionsBySearch(merchantId, txnType, request))
    } yield merchant
    resp.value.map {
      case Left(err) =>
        err match {
          case ex: MerchantTxnErr => throw BadRequest(ex.err)
        }
      case Right((data)) =>
        data.map { m =>
          MerchantTransactionResp(m.txnId,
            m.caseRefNo,
            m.txnTimestamp,
            m.txnAmount,
            m.ifrmVerdict,
            m.investigationStatus,
            m.channel,
            m.txnType,
            m.responseCode)
        }.toList
    }
  })

}



object MerchantportallagomappServiceImpl{
  val config: Config = ConfigFactory.load()
  val maxAgeInSeconds: Int = config.getInt("ifrm.cookie.max-age.seconds")
  val tokenValidityInMinutes:Int= config.getInt("ifrm.token.validity.minutes")
}

package com.squareoneinsights.merchantportallagomapp.impl

import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.api.ServiceCall
import cats.syntax.either._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import cats.implicits.catsSyntaxEitherId
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.squareoneinsights.merchantportallagomapp.api.request.LogOutReq
import com.squareoneinsights.merchantportallagomapp.api.request.MerchantLoginReq
import com.squareoneinsights.merchantportallagomapp.api.request.MerchantRiskScoreReq
import com.squareoneinsights.merchantportallagomapp.api.request.RiskType
import com.squareoneinsights.merchantportallagomapp.api.request.TransactionFilterReq
import com.squareoneinsights.merchantportallagomapp.api.response.{BusinessImpact, MerchantImpactDataResp, MerchantLoginResp, MerchantRiskScoreResp, MerchantTransactionDetails, MerchantTransactionResp, MerchantTxnSearchCriteria, PartnerInfo, ResponseMessage, TxnSearchCriteria}
import com.squareoneinsights.merchantportallagomapp.impl.common.{AddMerchantErr, CreateLogInTokenErr, FailedToGetPartner, GetBusinessImpactErr, GetMerchantErr, GetMerchantOnboard, GetUserDetailErr, JwtTokenGenerator, LogoutErr, LogoutRedisErr, MerchantPortalError, MerchantTxnErr, Pac4jAuthorizer, RedisUtility, RiskSettingProducerErr, TokenContent, UpdateLogInRedisErr, UpdatedRiskErr}
import com.squareoneinsights.merchantportallagomapp.impl.MerchantportallagomappServiceImpl.tokenValidityInMinutes
import com.squareoneinsights.merchantportallagomapp.impl.kafka.KafkaProduceService
import com.squareoneinsights.merchantportallagomapp.impl.repository.{BusinessImpactRepo, FilterTXN, MerchantLoginRepo, MerchantOnboardRiskScore, MerchantRiskScoreDetailRepo, MerchantTransactionRepo, PartnerInfoRepo}
import com.squareoneinsights.merchantportallagomapp.impl.util.MerchantUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MerchantportallagomappServiceImpl(
                                        merchantRiskScoreDetailRepo: MerchantRiskScoreDetailRepo,
                                        kafkaProduceService: KafkaProduceService,
                                        merchantOnboardRiskScore: MerchantOnboardRiskScore,
                                        businessImpactRepo: BusinessImpactRepo,
                                        merchantLoginRepo: MerchantLoginRepo,
                                        merchantTransactionRepo: MerchantTransactionRepo,
                                        redisUtility: RedisUtility,
                                        partnerInfoRepo: PartnerInfoRepo,
                                        system: ActorSystem
                                       )(implicit ec: ExecutionContext)
    extends Pac4jAuthorizer(system)
    with MerchantportallagomappService {

  val logger: Logger   = LoggerFactory.getLogger(getClass)
  implicit val timeout = Timeout(5.seconds)
  val maxAgeInSeconds  = 36000

  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful("Ok....")
  }

 override def getRiskScore(merchantId: String, partnerId: Int): ServiceCall[NotUsed, MerchantRiskScoreResp] =
   authorize((tokenContent, _) =>
    ServerServiceCall { _ =>
      val getMerchantRisk = for {
        getMerchantRiskData    <- EitherT(merchantRiskScoreDetailRepo.fetchRiskScore(merchantId, partnerId))
      } yield (getMerchantRiskData)
      getMerchantRisk.value.map {
        case Left(err) => {
          err match {
            case er: GetMerchantErr => throw BadRequest(er.err)
            case getM: GetMerchantOnboard  => throw BadRequest(getM.err)
          }
        }
        case Right(data) => {
          logger.info("Inside getRiskScore--->" + data)
          data
        }
      }
    })

   override def addRiskType(merchantId: String, partnerId: Int): ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp] =
     authorize((tokenContent, _) =>
    ServerServiceCall { riskJson =>
      val resp = for {
        toRedis       <- EitherT(merchantRiskScoreDetailRepo.updateRiskScore(riskJson, partnerId, merchantId))
        toKafka       <- EitherT(kafkaProduceService.sendMessage(riskJson.merchantId, riskJson.oldRisk, riskJson.updatedRisk, partnerId))
      } yield(toKafka)
      resp.value.map {
        case Left(err) => {
          err match {
            case addEr: UpdatedRiskErr => {
              logger.info("Inside addRiskType1 =>"+addEr.err)
              throw BadRequest(addEr.err)
            }
            case rskErr : RiskSettingProducerErr => {
              logger.info("Inside addRiskType1 =>"+rskErr.err)
              throw BadRequest(rskErr.err)
            }
          }
        }
        case Right(_) => {
          val merchantRiskResp = MerchantRiskScoreResp.apply(riskJson.merchantId, riskJson.oldRisk, riskJson.updatedRisk, "Approve")
          if (riskJson.updatedRisk == "High") merchantRiskResp.copy(approvalFlag = "Pending") else merchantRiskResp
        }
      }
    })

  override def getMerchantImpactData(merchantId: String, partnerId: Int): ServiceCall[NotUsed, BusinessImpact] =
    authorize((tokenContent, _) =>
    ServerServiceCall { _ =>
      businessImpactRepo.fetchBusinessDetail(merchantId, partnerId).map {
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
      merchant      <- EitherT(merchantLoginRepo.getUserByName(userLoginDetails.userName))
      tokenContent  <- EitherT.rightT(TokenContent(merchant.merchantId, merchant.merchantName))
      jwt           <- EitherT(JwtTokenGenerator.createToken(tokenContent, new DateTime().plusMinutes(tokenValidityInMinutes).toDate))
                  _ <- EitherT(merchantLoginRepo.updateMerchantLoginInfo(merchant))
                  _ <- EitherT(redisUtility.addTokenToRedis(merchant.merchantId, jwt.authToken))
      jwtFreshToken <- EitherT(JwtTokenGenerator.generateRefreshToken(tokenContent, new DateTime().plusMinutes(tokenValidityInMinutes).toDate))
      _ <- EitherT(redisUtility.addTokenToRedis(merchant.merchantId + "_refreshToken", jwtFreshToken.refreshToken.getOrElse("")))
    } yield (merchant, jwt)
    val response = resp.value.map {
      case Left(err) => {
        err match {
          case ex: LogoutRedisErr        => throw BadRequest(ex.err)
          case gErr: GetUserDetailErr    => throw BadRequest(gErr.err)
          case cErr: CreateLogInTokenErr => throw BadRequest(cErr.err)
          case uErr: UpdateLogInRedisErr => throw BadRequest(uErr.err)
        }
      }
      case Right((data, auth)) =>
        (MerchantLoginResp(data.merchantId, data.partnerId, data.merchantId, data.merchantName, "mcc", true), auth)
    }
    response.map { case (res, auth) =>
      val header =
        ResponseHeader.Ok.withHeader("Set-Cookie", s"authToken=${auth.authToken}; Max-Age=${maxAgeInSeconds}")
      header -> res
    }
  }

  override def logOut: ServiceCall[LogOutReq, ResponseMessage] =
    authorize((tokenContent, _) =>
    ServerServiceCall { req =>
    val query = for {
      merchant      <- EitherT(merchantLoginRepo.getUserByName(req.userName))
      updateStatus  <- EitherT(merchantLoginRepo.updateMerchantLoginStatus(req.userName))
      del           <- EitherT(redisUtility.deleteTokenFromRedis(req.userName))
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

  override def getPartner: ServiceCall[NotUsed, Seq[PartnerInfo]] =
      ServerServiceCall { _ =>
        partnerInfoRepo.getPartners.map {
          case Left(lerr) => logger.info(s"LogOut Failed. \n Error: ${lerr}")
            lerr match {
              case getErr: FailedToGetPartner => throw BadRequest(getErr.err)
            }
          case Right(value) => value
        }
      }


  override def getTransactions(
      txnType: String,
      merchantId: String,
      partnerId: Int
  ): ServiceCall[NotUsed, List[MerchantTransactionResp]] =
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
              MerchantTransactionResp(
                m.txnId,
                m.caseRefNo,
                m.txnTimestamp.toString,
                m.txnAmount,
                m.ifrmVerdict,
                m.investigationStatus,
                m.channel,
                m.txnType,
                m.responseCode
              )
            }.toList
        }
      }
    )

  override def getTransactionsBySearch(
      txnType: String,
      merchantId: String,
      partnerId: Int
  ): ServiceCall[TransactionFilterReq, List[MerchantTransactionResp]] =
    authorize((tokenContent, _) =>
      ServerServiceCall { req =>
     val resp = for {
          request <- EitherT.rightT( FilterTXN(MerchantUtil.filterColumn(req.filterCondition.key), MerchantUtil.conditionMap(req.filterCondition.condition), req.filterCondition.value))
          merchant <- EitherT(merchantTransactionRepo.getTransactionsBySearch(merchantId, txnType, request, partnerId))
        } yield merchant
        resp.value.map {
          case Left(err) =>
            err match {
              case ex: MerchantTxnErr => throw BadRequest(ex.err)
            }
          case Right((data)) =>
            data.map { m =>
              MerchantTransactionResp(
                m.txnId,
                m.caseRefNo,
                m.txnTimestamp,
                m.txnAmount,
                m.ifrmVerdict,
                m.investigationStatus,
                m.channel,
                m.txnType,
                m.responseCode
              )
            }.toList
        }
      }
    )

  override def getTxnSearchCriteriaList(partnerId: Int): ServiceCall[NotUsed, MerchantTxnSearchCriteria] =
    ServerServiceCall { _ =>
      val x =  List(TxnSearchCriteria("channel", "Channel"),
              TxnSearchCriteria("responseCode","Response Code"),
              TxnSearchCriteria("txnAmount","Transaction Amount"),
              TxnSearchCriteria("txnTimestamp","Transaction Date"),
              TxnSearchCriteria("txnType","Transaction Type")
      )
      val merchantResultList = MerchantTxnSearchCriteria(x)
      Future.successful(merchantResultList)
    }

  override def getTxnDetails(
      txnType: String,
      txnId: String,
      merchantId: String,
      partnerId: Int
  ): ServiceCall[NotUsed, MerchantTransactionDetails] = ServerServiceCall { _ =>
    merchantTransactionRepo
      .getTransactionDetails(txnType, txnId, merchantId, partnerId)
      .map {
        case Left(err)   => throw BadRequest(s"Error: ${err}")
        case Right(data) => data
      }
  }

  override def addRiskTypeNew(merchantId: String, partnerId: Int):
  ServiceCall[MerchantRiskScoreReq, String] =
    ServerServiceCall { _ =>
    logger.info("Inside addRiskTypeNew--"+partnerId)
      logger.info("Inside addRiskTypeNew merchantId--"+merchantId)
    Future.successful("Working")
  }

}

object MerchantportallagomappServiceImpl {
  val config: Config              = ConfigFactory.load()
  val maxAgeInSeconds: Int        = config.getInt("ifrm.cookie.max-age.seconds")
  val tokenValidityInMinutes: Int = config.getInt("ifrm.token.validity.minutes")
}

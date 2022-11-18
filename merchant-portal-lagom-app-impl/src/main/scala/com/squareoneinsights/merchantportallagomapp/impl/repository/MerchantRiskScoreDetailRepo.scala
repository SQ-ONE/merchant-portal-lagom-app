package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import com.squareoneinsights.merchantportallagomapp.api.request.{MerchantRiskScoreReq, RiskScoreReq, RiskType}
import org.slf4j.{Logger, LoggerFactory}
import akka.Done
import cats.implicits.{catsSyntaxEitherId, _}
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantRiskScoreResp
import com.squareoneinsights.merchantportallagomapp.impl.common.{AddMerchantErr, CheckRiskScoreExist, GetMerchantErr, MerchantPortalError, UpdatedRiskErr}
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantRiskScore

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

class MerchantRiskScoreDetailRepo(db: Database)
                                 (implicit ec: ExecutionContext) extends MerchantRiskScoreDetailTrait {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  private val merchantRiskScoreDetailTable = TableQuery[MerchantRiskScoreDetailTable]

  //val db = Database.forConfig("postgreDBProfile")
  def updateRiskScore(riskScoreReq: MerchantRiskScoreReq, partnerId: Int, merchantId: String): Future[Either[MerchantPortalError, Done]] = {
    logger.info("Inside updateRiskScore---->"+riskScoreReq)
    val approvalFlag = if(riskScoreReq.updatedRisk == "High") "Approve" else "Approve"
    val update = merchantRiskScoreDetailTable.filter(col => (col.merchantId === merchantId && col.partnerId === partnerId))
      .map(row => (row.oldSliderPosition, row.updatedSliderPosition, row.approvalFlag, row.updateTimestamp))
      .update(riskScoreReq.oldRisk.toString, riskScoreReq.updatedRisk.toString, approvalFlag, Some(LocalDateTime.now()))
    db.run(update).map { _ =>
      Done.asRight[MerchantPortalError]
    }.recover {
      case ex => {
        logger.info("Inside updateRiskScore error---->"+ex)
        UpdatedRiskErr(ex.getMessage).asLeft[Done]
      }
    }
  }

/*  def insertRiskScore(riskScoreReq: MerchantRiskScoreReq): Future[Either[MerchantPortalError, Done]] = {
    val approvalFlag = if(riskScoreReq.updatedRisk == "High") "Approve" else "Approve"
    val insertMessage = merchantRiskScoreDetailTable +=  MerchantRiskScore(0, riskScoreReq.merchantId, riskScoreReq.oldRisk.toString,  riskScoreReq.updatedRisk.toString, approvalFlag, None ,Some(LocalDateTime.now()))
    db.run(insertMessage).map { _ =>
      Done.asRight[MerchantPortalError]
    }.recover {
      case ex => AddMerchantErr(ex.toString).asLeft[Done]
    }
  }*/

  def updatedIsApprovedFlag(riskScoreReq: RiskScoreReq) = {
    println("updatedIsApprovedFlag.............")
    val updatedFlag = merchantRiskScoreDetailTable.filter(_.merchantId === riskScoreReq.merchantId)
      .map(col => (col.approvalFlag, col.updateTimestamp))
      .update(riskScoreReq.isApproved, Some(LocalDateTime.now()))
    db.run(updatedFlag).map(_ => Done.asRight[MerchantPortalError]).recover {
      case ex => UpdatedRiskErr(ex.toString).asLeft[Done]
    }
  }

  def fetchRiskScore(merchantId: String, partnerId: Int): Future[Either[MerchantPortalError, MerchantRiskScoreResp]] = {
    println("fetchRiskScore.............")
    val fetchMessage = merchantRiskScoreDetailTable
      .filter(col => (col.merchantId ===  merchantId && col.partnerId === partnerId  && col.isActive === 1))
      .map(col => (col.merchantId, col.oldSliderPosition, col.updatedSliderPosition, col.approvalFlag))
    db.run(fetchMessage.result.headOption)
      .map { fromTryMerchant =>
        Either.fromOption(fromTryMerchant.map(seqMerchant => MerchantRiskScoreResp(seqMerchant._1,
          RiskType.withName(seqMerchant._2), RiskType.withName(seqMerchant._3), seqMerchant._4)),
          GetMerchantErr("No merchant found for MerchantId: ${merchantId}"))
      }
  }

  def checkRiskScoreExist(merchantId: String, partnerId: Int): Future[Either[MerchantPortalError, Boolean]] = {
    val containsBay = for {
      m <- merchantRiskScoreDetailTable.filter(row => (row.partnerId === partnerId && row.merchantId === merchantId))
      } yield m
    val bayMentioned = containsBay.exists.result
    db.run(bayMentioned)
      .map(value => value.asRight[MerchantPortalError]).recover {
      case ex => CheckRiskScoreExist(ex.toString).asLeft[Boolean]
    }
  }
}

trait MerchantRiskScoreDetailTrait {

  class MerchantRiskScoreDetailTable(tag: Tag) extends Table[MerchantRiskScore](tag, _schemaName = Option("MERCHANT_PORTAL_RISK") ,"MERCHANT_RISK_SETTING") {

    def * = (requestId, merchantId, oldSliderPosition, updatedSliderPosition, approvalFlag) <> ((MerchantRiskScore.apply _).tupled, MerchantRiskScore.unapply)

    def requestId = column[Int]("REQUEST_ID", O.AutoInc, O.Unique)

    def partnerId = column[Int]("PARTNER_ID")

    def merchantId = column[String]("MERCHANT_ID")

    def oldSliderPosition = column[String]("OLD_RISK")

    def updatedSliderPosition = column[String]("UPDATED_RISK")

    def approvalFlag = column[String]("APPROVAL_FLAG", O.Default("Approve"))

    def isActive = column[Int]("IS_MERCHANT_ACTIVE")

    def updateTimestamp = column[Option[LocalDateTime]]("UPDATED_TIMESTAMP")

    def createTimeStamp = column[Option[LocalDateTime]]("CREATE_TIMESTAMP")

    def listType = column[String]("MERCHANT_RISK_TYPE")
  }
}

package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import com.squareoneinsights.merchantportallagomapp.api.request.{
  MerchantRiskScoreReq,
  RiskScoreReq
}
import org.slf4j.{Logger, LoggerFactory}
import akka.Done
import cats.implicits.{catsSyntaxEitherId, _}
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantRiskScoreResp
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantRiskScore
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

class MerchantRiskScoreDetailRepo(db: Database)(implicit ec: ExecutionContext)
    extends MerchantRiskScoreDetailTrait {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  private val merchantRiskScoreDetailTable =
    TableQuery[MerchantRiskScoreDetailTable]

  //val db = Database.forConfig("postgreDBProfile")
  def insertRiskScore(
      riskScoreReq: MerchantRiskScoreReq
  ): Future[Either[String, Done]] = {
    val approvalFlag =
      if (riskScoreReq.updatedRisk == "High") "Pending" else "Approved"
    val insertMessage = merchantRiskScoreDetailTable += MerchantRiskScore(
      0,
      riskScoreReq.merchantId,
      riskScoreReq.oldRisk,
      riskScoreReq.updatedRisk,
      approvalFlag,
      LocalDateTime.now()
    )
    db.run(insertMessage)
      .map { _ =>
        Done.asRight[String]
      }
      .recover { case ex =>
        ex.getMessage.asLeft[Done]
      }
  }

  def updatedIsApprovedFlag(riskScoreReq: RiskScoreReq) = {
    val updatedFlag = merchantRiskScoreDetailTable
      .filter(_.merchantId === riskScoreReq.merchantId)
      .map(_.approvalFlag)
      .update(riskScoreReq.isApproved)
    db.run(updatedFlag).map(_ => Done.asRight[String]).recover { case ex =>
      ex.getMessage.asLeft[Done]
    }
  }

  def fetchRiskScore(
      merchantId: String
  ): Future[Either[String, MerchantRiskScoreResp]] = {
    val fetchMessage =
      merchantRiskScoreDetailTable.filter(_.merchantId === merchantId)
    db.run(fetchMessage.result.headOption)
      .map { fromTryMerchant =>
        Either.fromOption(
          fromTryMerchant.map(seqMerchant =>
            MerchantRiskScoreResp(
              seqMerchant.merchantId,
              seqMerchant.oldSliderPosition,
              seqMerchant.updatedSliderPosition,
              seqMerchant.approvalFlag
            )
          ),
          s"No merchant found for MerchantId: ${merchantId}"
        )
      }
  }
}

trait MerchantRiskScoreDetailTrait {

  class MerchantRiskScoreDetailTable(tag: Tag)
      extends Table[MerchantRiskScore](tag, "merchant_risk_score") {

    def * = (
      requestId,
      merchantId,
      oldSliderPosition,
      updatedSliderPosition,
      approvalFlag,
      updateTimestamp
    ) <> ((MerchantRiskScore.apply _).tupled, MerchantRiskScore.unapply)

    def requestId = column[Int]("request_id", O.AutoInc, O.Unique)

    def merchantId = column[String]("merchant_id")

    def oldSliderPosition = column[String]("old_risk")

    def updatedSliderPosition = column[String]("updated_risk")

    def approvalFlag = column[String]("approval_flag")

    def updateTimestamp = column[LocalDateTime]("updated_timestamp")

    def listType = column[String]("MERCHANT_RISK_TYPE")
  }
}

package com.squareoneinsights.merchantportallagomapp.impl.repository

import org.slf4j.{Logger, LoggerFactory}
import akka.Done
import cats.implicits.{catsSyntaxEitherId, _}
import com.squareoneinsights.merchantportallagomapp.impl.model.{MerchantOnboardRS}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

class MerchantOnboardRiskScore (db: Database)
                               (implicit ec: ExecutionContext) extends MerchantOnboardRiskScoreTrait {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  private val merchantOnboardRiskScoreDetailTable = TableQuery[MerchantOnboardRiskScoreDetailTable]

  def getInitialRiskType(merchantId: String): Future[Either[String, String]] = {
    val query = merchantOnboardRiskScoreDetailTable.filter(_.merchantId === merchantId).map(_.merchantOnboardScore)
    db.run(query.result.headOption)
      .map { fromTryMerchant =>
        Either.fromOption(fromTryMerchant.map(seqMerchant => seqMerchant), s"No merchant found for MerchantId: ${merchantId}")
      }
  }
}


trait MerchantOnboardRiskScoreTrait {

  class MerchantOnboardRiskScoreDetailTable(tag: Tag) extends Table[MerchantOnboardRS](tag, _schemaName = Option("IFRM_UDS") ,"MERCHANT_ONBOARD_RISK_SCORE") {

    def * = (partnerId, merchantId, merchantOnboardScore) <> ((MerchantOnboardRS.apply _).tupled, MerchantOnboardRS.unapply)

    def partnerId = column[Int]("PARTNERID",O.Unique)

    def merchantId = column[String]("MERCHANT_ID")

    def merchantOnboardScore = column[String]("MERCHANT_ONBOARD_RISK_SCORE")
  }
}
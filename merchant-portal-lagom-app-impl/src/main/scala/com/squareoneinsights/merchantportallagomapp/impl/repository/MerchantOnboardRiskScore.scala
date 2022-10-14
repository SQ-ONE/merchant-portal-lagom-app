package com.squareoneinsights.merchantportallagomapp.impl.repository

import org.slf4j.{Logger, LoggerFactory}
import akka.Done
import cats.implicits.{catsSyntaxEitherId, _}
import com.squareoneinsights.merchantportallagomapp.impl.common.{GetMerchantErr, GetMerchantOnboard, MerchantPortalError}
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantOnboardRS

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

class MerchantOnboardRiskScore (db: Database)
                               (implicit ec: ExecutionContext) extends MerchantOnboardRiskScoreTrait {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  private val merchantOnboardRiskScoreDetailTable = TableQuery[MerchantOnboardRiskScoreDetailTable]

  def getInitialRiskType(mcc: String): Future[Either[MerchantPortalError, String]] = {
    val query = merchantOnboardRiskScoreDetailTable.filter(_.mcc === mcc).map(_.merchantOnboardScore)
    db.run(query.result.headOption)
      .map { fromTryMerchant =>
        Either.fromOption(fromTryMerchant.map(seqMerchant => seqMerchant), GetMerchantOnboard(s"No merchant found for mcc: ${mcc}"))
      }
  }
}


trait MerchantOnboardRiskScoreTrait {

  class MerchantOnboardRiskScoreDetailTable(tag: Tag) extends Table[MerchantOnboardRS](tag, _schemaName = Option("IFRM_UDS") ,"MCC") {

    def * = (partnerId, mcc, merchantOnboardScore) <> ((MerchantOnboardRS.apply _).tupled, MerchantOnboardRS.unapply)

    def partnerId = column[Int]("PARTNERID",O.Unique)

    def mcc = column[String]("MCC")

    def merchantOnboardScore = column[String]("RISK")
  }
}
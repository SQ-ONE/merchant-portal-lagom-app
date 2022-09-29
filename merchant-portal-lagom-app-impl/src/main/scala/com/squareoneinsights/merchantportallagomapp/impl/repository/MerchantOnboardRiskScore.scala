package com.squareoneinsights.merchantportallagomapp.impl.repository

import org.slf4j.{Logger, LoggerFactory}
import akka.Done
import cats.implicits.{catsSyntaxEitherId, _}
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantOnboardRiskScore
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._


class MerchantOnboardRiskScoreTable (db: Database)
                               (implicit ec: ExecutionContext) extends MerchantRiskScoreDetailTrait{
  val logger: Logger = {
    LoggerFactory.getLogger(getClass)
  }
}


trait MerchantOnboardRiskScoreTrait {

  class MerchantRiskScoreDetailTable(tag: Tag) extends Table[MerchantOnboardRiskScore](tag, _schemaName = Option("IFRM_UDS") ,"MERCHANT_ONBOARD_RISK_SCORE") {

    def * = (partnerId, merchantId, merchantOnboardScore) <> ((MerchantOnboardRiskScore.apply _).tupled, MerchantOnboardRiskScore.unapply)

    def partnerId = column[Int]("PARTNERID",O.Unique)

    def merchantId = column[String]("MERCHANT_ID")

    def merchantOnboardScore = column[String]("MERCHANT_ONBOARD_RISK_SCORE")
  }
}

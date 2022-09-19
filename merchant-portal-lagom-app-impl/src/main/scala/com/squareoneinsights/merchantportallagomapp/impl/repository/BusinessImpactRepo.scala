package com.squareoneinsights.merchantportallagomapp.impl.repository

import cats.implicits._
import com.squareoneinsights.merchantportallagomapp.api.request.BusinessImpactDetail
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class BusinessImpactRepo(db: Database)(implicit ec: ExecutionContext) extends BusinessImpactTrait {

  //val db = Database.forConfig("db.default")
  //val logger: Logger = LoggerFactory.getLogger(BusinessImpactRepo)
  val businessImpactTable = TableQuery[BusinessImpactTable]

  def fetchBusinessDetail(merchantId: String): Future[Either[String, BusinessImpactDetail]] = {
    val businessImpact = businessImpactTable.filter(col => (col.merchantId === merchantId))
    db.run(businessImpact.result).map { x =>
      Either.fromOption(x.headOption, s"No Business Impact found for merchantId: ${merchantId}")
    }
  }

  def save(businessImpactDetail: BusinessImpactDetail) = {
    val query = businessImpactTable += businessImpactDetail
    db.run(query).map(x => x.asRight[String])
  }
}

trait BusinessImpactTrait {

  class BusinessImpactTable(tag: Tag) extends Table[BusinessImpactDetail](tag, "merchant_risk_score_data") {

    def * = (partnerId, merchantId, lowPaymentAllowed, lowPaymentReview, lowPaymentBlocked, medPaymentAllowed, medPaymentReview,
      medPaymentBlocked, highPaymentAllowed, highPaymentReview, highPaymentBlocked, updatedTimeStamp) <>
      ((BusinessImpactDetail.apply _).tupled, BusinessImpactDetail.unapply _)

    def partnerId = column[Int]("partner_id")

    def merchantId = column[String]("merchant_id")

    def lowPaymentAllowed = column[Int]("low_payment_allowed")

    def lowPaymentReview = column[Int]("low_payment_review")

    def lowPaymentBlocked = column[Int]("low_payment_blocked")

    def medPaymentAllowed = column[Int]("med_payment_allowed")

    def medPaymentReview = column[Int]("med_payment_review")

    def medPaymentBlocked = column[Int]("med_payment_blocked")

    def highPaymentAllowed = column[Int]("high_payment_allowed")

    def highPaymentReview = column[Int]("high_payment_review")

    def highPaymentBlocked = column[Int]("high_payment_blocked")

    def updatedTimeStamp = column[LocalDateTime]("updated_time_stamp")
  }

}

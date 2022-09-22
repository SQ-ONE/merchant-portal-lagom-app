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
    println("fetchBusinessDetail.............")
    val businessImpact = businessImpactTable.filter(col => (col.merchantId === merchantId))
    db.run(businessImpact.result).map { x =>
      Either.fromOption(x.headOption, s"No Business Impact found for merchantId: ${merchantId}")
    }
  }

  def save(businessImpactDetail: BusinessImpactDetail) = {
    println("save.............")
    val query = businessImpactTable += businessImpactDetail
    db.run(query).map(x => x.asRight[String])
  }
}

trait BusinessImpactTrait {

  class BusinessImpactTable(tag: Tag) extends Table[BusinessImpactDetail](tag, _schemaName = Option("IFRM_LIST_LIMITS") , "MERCHANT_RISK_SCORE") {

    def * = (partnerId, merchantId, lowPaymentAllowed, lowPaymentReview, lowPaymentBlocked, medPaymentAllowed, medPaymentReview,
      medPaymentBlocked, highPaymentAllowed, highPaymentReview, highPaymentBlocked, updatedTimeStamp) <>
      ((BusinessImpactDetail.apply _).tupled, BusinessImpactDetail.unapply _)

    def partnerId = column[Int]("PARTNER_ID", O.PrimaryKey)

    def merchantId = column[String]("MERCHANT_ID" , O.PrimaryKey)

    def lowPaymentAllowed = column[Int]("LOW_PAYMENTS_ALLOWED")

    def lowPaymentReview = column[Int]("LOW_PAYMENTS_REVIEW")

    def lowPaymentBlocked = column[Int]("LOW_PAYMENTS_BLOCKED")

    def medPaymentAllowed = column[Int]("MED_PAYMENTS_ALLOWED")

    def medPaymentReview = column[Int]("MED_PAYMENTS_REVIEW")

    def medPaymentBlocked = column[Int]("MED_PAYMENTS_BLOCKED")

    def highPaymentAllowed = column[Int]("HIGH_PAYMENTS_ALLOWED")

    def highPaymentReview = column[Int]("HIGH_PAYMENTS_REVIEW")

    def highPaymentBlocked = column[Int]("HIGH_PAYMENTS_BLOCKED")

    def updatedTimeStamp = column[LocalDateTime]("UPDATED_AT")
  }
}

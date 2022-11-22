package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import cats.data.EitherT
import cats.implicits._
import com.squareoneinsights.merchantportallagomapp.api.request.BusinessImpactDetail
import com.squareoneinsights.merchantportallagomapp.impl.common.{AddBusinessImpactErr, CheckRiskScoreExist, GetBusinessImpactErr, MerchantPortalError, UpdateBusinessImpactErr}
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class BusinessImpactRepo(db: Database)(implicit ec: ExecutionContext) extends BusinessImpactTrait {

  //val db = Database.forConfig("db.default")
  //val logger: Logger = LoggerFactory.getLogger(BusinessImpactRepo)
  val businessImpactTable = TableQuery[BusinessImpactTable]

  def fetchBusinessDetail(merchantId: String, partnerId: Int): Future[Either[MerchantPortalError, BusinessImpactDetail]] = {
    println("fetchBusinessDetail.............")
    val businessImpact = businessImpactTable.filter(col => (col.merchantId === merchantId && col.partnerId === partnerId))
    db.run(businessImpact.result).map { x =>
      Either.fromOption(x.headOption, GetBusinessImpactErr(s"No Business Impact found for merchantId: ${merchantId}"))
    }
  }

  def save(businessImpactDetail: BusinessImpactDetail): Future[Either[MerchantPortalError, Done]] = {
    println("save.............")
    val insertOrSave = for {
       checkFlag <- EitherT(checkFlag(businessImpactDetail.merchantId, businessImpactDetail.partnerId))
       finalOperation <- EitherT(if(checkFlag) update(businessImpactDetail) else insert(businessImpactDetail))
    } yield(finalOperation)
    insertOrSave.value
  }

  def insert(businessImpactDetail: BusinessImpactDetail): Future[Either[MerchantPortalError, Done]] = {
    println("insert business.............")
    val query = businessImpactTable += businessImpactDetail
    db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover {
      case ex => AddBusinessImpactErr(ex.getMessage).asLeft[Done]
    }
  }

  def update(businessImpactDetail: BusinessImpactDetail): Future[Either[MerchantPortalError, Done]] = {
    println("updated business.............")
    import businessImpactDetail._
    val query = businessImpactTable.filter(_.merchantId === merchantId).map(col => (col.lowPaymentAllowed, col.lowPaymentReview, col.lowPaymentBlocked, col.medPaymentAllowed, col.medPaymentReview, col.medPaymentBlocked, col.highPaymentAllowed, col.highPaymentReview, col.highPaymentBlocked, col.updatedTimeStamp))
      .update(lowPaymentAllowed, lowPaymentReview, lowPaymentBlocked, medPaymentAllowed, medPaymentReview, medPaymentBlocked, highPaymentAllowed, highPaymentReview, highPaymentBlocked, updatedTimeStamp)
    db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover {
      case ex => UpdateBusinessImpactErr(ex.getMessage).asLeft[Done]
    }
  }

  def checkFlag(merchantId: String, partnerId: Int): Future[Either[MerchantPortalError, Boolean]] = {
    val containsBay = for {
      m <- businessImpactTable.filter(row => (row.partnerId === partnerId && row.merchantId === merchantId))
    } yield m
    val bayMentioned = containsBay.exists.result
    db.run(bayMentioned)
      .map(value => value.asRight[MerchantPortalError]).recover {
      case ex => CheckRiskScoreExist(ex.toString).asLeft[Boolean]
    }
  }
}

trait BusinessImpactTrait {

  class BusinessImpactTable(tag: Tag) extends Table[BusinessImpactDetail](tag, _schemaName = Option("MERCHANT_PORTAL_RISK") , "MERCHANT_RISK_SCORE") {

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

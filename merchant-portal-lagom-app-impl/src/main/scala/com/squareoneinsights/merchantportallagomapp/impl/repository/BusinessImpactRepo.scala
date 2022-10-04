package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import cats.data.EitherT
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
    val insertOrSave = for {
       checkFlag <- EitherT(checkFlag(businessImpactDetail.merchantId))
       finalOperation <- EitherT(if(checkFlag) update(businessImpactDetail) else insert(businessImpactDetail) )
    } yield(finalOperation)
    insertOrSave.value
  }

  def insert(businessImpactDetail: BusinessImpactDetail) = {
    println("insert business.............")
    val query = businessImpactTable += businessImpactDetail
    db.run(query).map(_ => Done.asRight[String]).recover {
      case ex => ex.getMessage.asLeft[Done]
    }
  }

  def update(businessImpactDetail: BusinessImpactDetail) = {
    println("updated business.............")
    import businessImpactDetail._
    val query = businessImpactTable.filter(_.merchantId === merchantId).map(col => (col.lowPaymentAllowed, col.lowPaymentReview, col.lowPaymentBlocked, col.medPaymentAllowed, col.medPaymentReview, col.medPaymentBlocked, col.highPaymentAllowed, col.highPaymentReview, col.highPaymentBlocked, col.updatedTimeStamp))
      .update(lowPaymentAllowed, lowPaymentReview, lowPaymentBlocked, medPaymentAllowed, medPaymentReview, medPaymentBlocked, highPaymentAllowed, highPaymentReview, highPaymentBlocked, updatedTimeStamp)
    db.run(query).map(_ => Done.asRight[String]).recover {
      case ex => ex.getMessage.asLeft[Done]
    }
  }

  def checkFlag(merchantId: String): Future[Either[String, Boolean]] = {
    val containsBay = for {
      m <- businessImpactTable
      if m.merchantId like s"%${merchantId}%"
    } yield m
    val bayMentioned = containsBay.exists.result
    db.run(bayMentioned)
      .map(value => value.asRight[String]).recover {
      case ex => ex.toString.asLeft[Boolean]
    }
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

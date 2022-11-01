package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.impl.common.{GetUserDetailErr, LogoutErr, MerchantPortalError, UpdateLogInPsqlErr, UpdateLogInRedisErr}
import com.squareoneinsights.merchantportallagomapp.impl.model.{Merchant, MerchantLogin, MerchantLoginActivity, MerchantLoginDetails}
import org.joda.time.LocalDate

import java.sql.{Date, Timestamp}
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}


class MerchantLoginRepo(db: Database)
                       (implicit ec: ExecutionContext) extends  MerchantLoginTrait with MerchantLoginActivityTrait with MerchantTrait {


  val merchantLoginTable = TableQuery[MerchantLoginTable]

  val merchantLoginActivityTable = TableQuery[MerchantLoginActivityTable]

  val merchantTable = TableQuery[MerchantTable]

  def getUserByName(userName: String, partnerId:Int): Future[Either[GetUserDetailErr, MerchantLoginDetails]] = {
    val query = (merchantLoginTable.filter(row => row.merchantId === userName && row.partnerId === partnerId)
      .join(merchantTable).on(_.merchantId === _.merchantId))
      .result.asTry.map { merchantWithTry =>
      val fromTry = Either.fromTry(merchantWithTry).leftMap(err => GetUserDetailErr(err.getMessage))
      val fromOption = fromTry.flatMap { fromTrySeq =>
        Either.fromOption(fromTrySeq.headOption, GetUserDetailErr(s"No Merchant found with userName:$userName"))
      }
      fromOption.map(x => MerchantLoginDetails(x._1.id,x._1.merchantId,x._1.partnerId, x._1.merchantName,x._2.merchantMcc, x._1.isLoggedInFlag))
    }
    db.run(query)
  }

  def updateMerchantLoginInfo(merchant: MerchantLoginDetails): Future[Either[MerchantPortalError, Done]] = {
    val action1 = merchantLoginTable.filter(row => row.merchantId === merchant.merchantId && row.partnerId === merchant.partnerId ).map(_.isLoggedInFlag).update(true)
    val action2 = merchantLoginActivityTable += MerchantLoginActivity(None,merchant.merchantId,merchant.partnerId,Some(Timestamp.valueOf(LocalDateTime.now())),None)

    val addUserQuery = DBIO.seq(action1,action2).transactionally
    db.run(addUserQuery)
      .map { _ =>
        Done.asRight[UpdateLogInRedisErr]
      }.recover {
      case ex => UpdateLogInPsqlErr("Failed to updated login detail").asLeft[Done]
    }
  }

  def logoutActivity(merchantId: String,  partnerId:Int): Future[Either[MerchantPortalError, Done]] = {
    val action2 = merchantLoginActivityTable += MerchantLoginActivity(None, merchantId, partnerId, None, Some(Timestamp.valueOf(LocalDateTime.now())))
    db.run(action2)
      .map { _ =>
        Done.asRight[MerchantPortalError]
      }.recover {
      case ex => LogoutErr(ex.getMessage).asLeft[Done]
    }
  }
  def updateMerchantLoginStatus(merchantName: String, partnerId:Int): Future[Either[MerchantPortalError, Done]] = {
    val action1 = merchantLoginTable.filter(row => row.merchantId === merchantName && row.partnerId === partnerId).map(_.isLoggedInFlag).update(false)
    //val action2 = merchantLoginActivityTable += MerchantLoginActivity(None, merchantName ,Some(Timestamp.valueOf(LocalDateTime.now())),None)

    val addUserQuery = DBIO.seq(action1).transactionally
    db.run(addUserQuery)
      .map { _ =>
        Done.asRight[MerchantPortalError]
      }.recover {
      case ex => LogoutErr(ex.getMessage).asLeft[Done]
    }
  }

}

trait MerchantLoginTrait {

  class MerchantLoginTable(tag: Tag) extends Table[MerchantLogin](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "MERCHANT_LOGIN") {

    def * = (id, merchantId,partnerId,merchantName, isLoggedInFlag) <> ((MerchantLogin.apply _).tupled, MerchantLogin.unapply)

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("MERCHANT_ID")

    def partnerId = column[Int]("PARTNER_ID")

    def merchantName = column[String]("MERCHANT_NAME")

    def isLoggedInFlag = column[Boolean]("IS_MERCHANT_ACTIVE")

  }
}

trait MerchantLoginActivityTrait  {

  class MerchantLoginActivityTable(tag: Tag) extends Table[MerchantLoginActivity](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "MERCHANT_LOGIN_ACTIVITY") {

    def * = (activityId,merchantId, partnerId,loginTime, logOutTime) <> ((MerchantLoginActivity.apply _).tupled, MerchantLoginActivity.unapply)

    def activityId = column[Option[Int]]("ACTIVITY_ID", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("MERCHANT_ID")

    def partnerId = column[Int]("PARTNER_ID")

    def loginTime = column[Option[Timestamp]]("LOGIN_TIME")

    def logOutTime = column[Option[Timestamp]]("LOGOUT_TIME")

  }
}


trait MerchantTrait {

  class MerchantTable(tag: Tag) extends Table[Merchant](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "MERCHANT") {

    def * = (merchantId , merchantMcc) <> ((Merchant.apply _).tupled, Merchant.unapply)

    def merchantId = column[String]("ID")

    def merchantMcc = column[String]("MCC")

  }
}

package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import com.squareoneinsights.merchantportallagomapp.impl.common.Db
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.impl.model.{Merchant, MerchantLogin, MerchantLoginActivity, MerchantLoginDetails}
import org.joda.time.LocalDate

import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}


class MerchantLoginRepo(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends Db with MerchantLoginTrait with MerchantLoginActivityTrait
with MerchantTrait {

  import config.profile.api._

  val merchantLoginTable = TableQuery[MerchantLoginTable]

  val merchantLoginActivityTable = TableQuery[MerchantLoginActivityTable]

  val merchantTable = TableQuery[MerchantTable]

  def getUserByName(userName: String): Future[Either[String, MerchantLoginDetails]] = {
    val query = (merchantLoginTable.filter(_.merchantId === userName)
      .join(merchantTable).on(_.merchantId === _.merchantId))
      .result.asTry.map { merchantWithTry =>
      val fromTry = Either.fromTry(merchantWithTry).leftMap(err => err.getMessage)
      val fromOption = fromTry.flatMap { fromTrySeq =>
        Either.fromOption(fromTrySeq.headOption, s"No Merchant found with userName:$userName")
      }
      fromOption.map(x => MerchantLoginDetails(x._1.id,x._1.merchantId, x._1.merchantName,x._2.merchantMcc, x._1.isLoggedInFlag))
    }
    db.run(query)
  }

  def updateMerchantLoginInfo(merchant: MerchantLoginDetails): Future[Either[String, Done]] = {
    val action1 = merchantLoginTable.filter(_.merchantId === merchant.merchantId ).map(_.isLoggedInFlag).update(true)
    val action2 = merchantLoginActivityTable += MerchantLoginActivity(None,merchant.merchantId,Some(new java.sql.Date(LocalDate.now().toDate.getTime)),None)

    val addUserQuery = DBIO.seq(action1,action2).transactionally
    db.run(addUserQuery)
      .map { _ =>
        Done.asRight[String]
      }.recover {
      case ex => ex.getMessage.asLeft[Done]
    }
  }
}


trait MerchantLoginTrait extends Db {

  import config.profile.api._

  class MerchantLoginTable(tag: Tag) extends Table[MerchantLogin](tag, _schemaName = Option("IFRM_UDS"), "MERCHANT_LOGIN") {

    def * = (id, merchantName,merchantId, isLoggedInFlag) <> ((MerchantLogin.apply _).tupled, MerchantLogin.unapply)

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("MERCHANT_ID", O.Unique)

    def merchantName = column[String]("MERCHANT_NAME")

    def isLoggedInFlag = column[Boolean]("IS_MERCHANT_ACTIVE")

  }
}



trait MerchantLoginActivityTrait extends Db {

  import config.profile.api._

  class MerchantLoginActivityTable(tag: Tag) extends Table[MerchantLoginActivity](tag, _schemaName = Option("IFRM_UDS"), "MERCHANT_LOGIN_ACTIVITY") {

    def * = (activityId,merchantId,loginTime, logOutTime) <> ((MerchantLoginActivity.apply _).tupled, MerchantLoginActivity.unapply)

    def activityId = column[Option[Int]]("ACTIVITY_ID", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("MERCHANT_ID")

    def loginTime = column[Option[Date]]("LOGIN_TIME")

    def logOutTime = column[Option[Date]]("LOGOUT_TIME")

  }
}


trait MerchantTrait extends Db {

  import config.profile.api._

  class MerchantTable(tag: Tag) extends Table[Merchant](tag, _schemaName = Option("IFRM_UDS"), "MERCHANT") {

    def * = (merchantId , merchantMcc) <> ((Merchant.apply _).tupled, Merchant.unapply)

    def merchantId = column[String]("ID")

    def merchantMcc = column[String]("MCC")

  }
}
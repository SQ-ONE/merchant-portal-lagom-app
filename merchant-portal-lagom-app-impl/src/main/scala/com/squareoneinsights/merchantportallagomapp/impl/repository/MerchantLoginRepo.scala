package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import com.squareoneinsights.merchantportallagomapp.impl.common.Db
import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.impl.model.{MerchantLogin, MerchantLoginActivity}
import org.joda.time.LocalDate

import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}


class MerchantLoginRepo(db: Database)(implicit ec: ExecutionContext) extends MerchantLoginTrait with MerchantLoginActivityTrait{



  val merchantLoginTable = TableQuery[MerchantLoginTable]

  val merchantLoginActivityTable = TableQuery[MerchantLoginActivityTable]

  def getUserByName(userName: String): Future[Either[String, MerchantLogin]] = {
    val query = merchantLoginTable.filter(_.merchantId === userName).result.asTry.map { merchantWithTry =>
      val fromTry = Either.fromTry(merchantWithTry).leftMap(err => err.getMessage)
      val fromOption = fromTry.flatMap { fromTrySeq =>
        Either.fromOption(fromTrySeq.headOption, s"No Merchant found with userName:$userName")
      }
      fromOption
    }
    db.run(query)
  }

  def updateMerchantLoginInfo(merchant: MerchantLogin): Future[Either[String, Done]] = {
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

trait MerchantLoginTrait {

  class MerchantLoginTable(tag: Tag) extends Table[MerchantLogin](tag, _schemaName = Option("merchant_portal"), "merchant_login") {

    def * = (id, merchantName,merchantId, isLoggedInFlag) <> ((MerchantLogin.apply _).tupled, MerchantLogin.unapply)

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("merchant_id", O.Unique)

    def merchantName = column[String]("merchant_name")

    def isLoggedInFlag = column[Boolean]("is_merchant_active")

  }
}

trait MerchantLoginActivityTrait  {

  class MerchantLoginActivityTable(tag: Tag) extends Table[MerchantLoginActivity](tag, _schemaName = Option("merchant_portal"), "merchant_login_activity") {

    def * = (activityId,merchantId,loginTime, logOutTime) <> ((MerchantLoginActivity.apply _).tupled, MerchantLoginActivity.unapply)

    def activityId = column[Option[Int]]("activity_id", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("merchant_id", O.Unique)

    def loginTime = column[Option[Date]]("login_time")

    def logOutTime = column[Option[Date]]("logout_time")

  }
}

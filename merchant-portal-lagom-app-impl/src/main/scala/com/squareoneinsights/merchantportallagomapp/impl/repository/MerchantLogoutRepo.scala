package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import com.squareoneinsights.merchantportallagomapp.impl.common.Db
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import cats.syntax.either._

import scala.concurrent.{ExecutionContext, Future}
import com.squareoneinsights.merchantportallagomapp.impl.model.{
  MerchantLoginActivity,
  Merchantlogin
}

import java.time.LocalDateTime

class MerchantLogoutRepo(val config: DatabaseConfig[JdbcProfile])(implicit
    ec: ExecutionContext
) extends Db
    with MerchantLogoutTrait {

  import config.profile.api._

  val usersTable = TableQuery[MerchantloginTable]
  val usersActivityTable = TableQuery[MerchantLoginActivityTable]

  def logoutMerchant(userId: String) = {
    val updatedFlag = usersTable
      .filter(_.merchantId === userId)
      .map(_.isMerchantActive)
      .update(false)

    db.run(updatedFlag).map(_ => Done.asRight[String]).recover { case ex =>
      ex.getMessage.asLeft[Done]
    }
  }

  def updateActivity(userId: String) = {
    val updatedActivity = usersActivityTable
      .filter(_.merchantId === userId)
      .map(_.logoutTime)
      .update(LocalDateTime.now())

    db.run(updatedActivity).map(_ => Done.asRight[String]).recover { case ex =>
      ex.getMessage.asLeft[Done]
    }
  }

}

trait MerchantLogoutTrait extends Db {

  import config.profile.api._

  class MerchantloginTable(tag: Tag)
      extends Table[Merchantlogin](
        tag,
        _schemaName = Option("merchant_portal"),
        "merchant_login"
      ) {

    def * = (
      id,
      merchantId,
      merchantName,
      merchantContact,
      merchantEmail,
      isMerchantActive,
      password,
      salt,
      provisional1,
      provisional2
    ) <> ((Merchantlogin.apply _).tupled, Merchantlogin.unapply)

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("merchant_id", O.Unique)

    def merchantName = column[String]("merchant_name")

    def merchantContact = column[String]("merchant_contact")

    def merchantEmail = column[String]("merchant_email")

    def isMerchantActive = column[Boolean]("is_merchant_active")

    def password = column[String]("password")

    def salt = column[String]("salt")

    def provisional1 = column[String]("provisional_1")

    def provisional2 = column[String]("provisional_2")

  }

  class MerchantLoginActivityTable(tag: Tag)
      extends Table[MerchantLoginActivity](
        tag,
        _schemaName = Option("merchant_portal"),
        "merchant_login_activity"
      ) {

    def * = (
      activityId,
      merchantId,
      loginTime,
      logoutTime
    ) <> ((MerchantLoginActivity.apply _).tupled, MerchantLoginActivity.unapply)

    def activityId = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("merchant_id", O.Unique)

    def loginTime = column[LocalDateTime]("login_time")

    def logoutTime = column[LocalDateTime]("logout_time")

  }

}

package com.squareoneinsights.merchantportallagomapp.impl.repository

import com.squareoneinsights.merchantportallagomapp.impl.common.Db
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import cats.syntax.either._
import scala.concurrent.{ExecutionContext, Future}


class MerchantLoginRepo(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends Db with MerchantLoginTrait {

  import config.profile.api._

  val usersTable = TableQuery[MerchantLoginTable]

  def getUserByName(userName: String): Future[Either[String, MerchantLogin]] = {
    val query = usersTable.filter(_.merchantId === userName).result.asTry.map { merchantWithTry =>
      val fromTry = Either.fromTry(merchantWithTry).leftMap(err => err.getMessage)
      val fromOption = fromTry.flatMap { fromTrySeq =>
        Either.fromOption(fromTrySeq.headOption, s"No Merchant found with userName:$userName")
      }
      fromOption
    }
    db.run(query)
  }
}


trait MerchantLoginTrait extends Db {

  import config.profile.api._

  class MerchantLoginTable(tag: Tag) extends Table[MerchantLogin](tag, _schemaName = Option("merchant_portal"), "merchant_login") {

    def * = (id, merchantName,merchantId, isLoggedInFlag) <> ((MerchantLogin.apply _).tupled, MerchantLogin.unapply)

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("merchant_id", O.Unique)

    def merchantName = column[String]("merchant_name")

    def isLoggedInFlag = column[Boolean]("is_merchant_active")

  }
}

case class MerchantLogin(id:Int,merchantId:String, merchantName:String,isLoggedInFlag:Boolean )

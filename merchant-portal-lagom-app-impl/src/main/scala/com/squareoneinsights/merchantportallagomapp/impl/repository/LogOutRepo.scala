package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import com.squareoneinsights.merchantportallagomapp.api.request.MerchantDetailsReq
import org.slf4j.{Logger, LoggerFactory}
import akka.Done
import cats.implicits.{catsSyntaxEitherId, _}
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantDetailsResp
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantDetails
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._

class LogOutRepo(db: Database)(implicit ec: ExecutionContext)
    extends MerchantDetailTrait {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  private val merchantDetailTable =
    TableQuery[MerchantDetailTable]

  def updatedToLogout(details: MerchantDetailsReq) = {
    val updatedFlag = merchantDetailTable
      .filter(_.merchantId === details.merchantId)
      .map(_.isLoginActive)
      .update(details.isLoginActive)
    db.run(updatedFlag).map(_ => Done.asRight[String]).recover { case ex =>
      ex.getMessage.asLeft[Done]
    }
  }

}

trait MerchantDetailTrait {

  class MerchantDetailTable(tag: Tag)
      extends Table[MerchantDetails](tag, "merchant_details") {

    def * = (
      merchantId,
      userName,
      merchantName,
      mccCode,
      isLoginActive,
      updateTimestamp
    ) <> ((MerchantDetails.apply _).tupled, MerchantDetails.unapply)

    def merchantId = column[Int]("merchant_id", O.AutoInc, O.Unique)

    def userName = column[String]("user_name")

    def merchantName = column[String]("merchant_name")

    def mccCode = column[Int]("mcc_code")

    def isLoginActive = column[String]("is_login_active")

    def updateTimestamp = column[LocalDateTime]("updated_timestamp")

  }
}

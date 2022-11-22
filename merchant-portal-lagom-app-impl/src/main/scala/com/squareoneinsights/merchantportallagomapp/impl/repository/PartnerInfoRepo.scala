package com.squareoneinsights.merchantportallagomapp.impl.repository

import cats.implicits.catsSyntaxEitherId
import com.squareoneinsights.merchantportallagomapp.api.response.PartnerInfo
import com.squareoneinsights.merchantportallagomapp.impl.common.{FailedToGetPartner, MerchantPortalError}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class PartnerInfoRepo(db: Database)
                     (implicit ec: ExecutionContext) extends PartnerInfoTrait {

  val partner = TableQuery[PartnerInfoTable]

  def getPartners = {
    val query = partner.result
    db.run(query).map { x =>
      x.map {
        seq => PartnerInfo(seq.id, seq.partnerName)
      }
    }.map(value => value.asRight[MerchantPortalError]).recover {
      case ex => FailedToGetPartner("Database connection error or Table doesn't exist").asLeft[Seq[PartnerInfo]]
    }
  }

}

trait PartnerInfoTrait {

  class PartnerInfoTable(tag: Tag) extends Table[PartnerInfo](tag, _schemaName = Option("MERCHANT_PORTAL_RISK"), "PARTNER_DETAIL") {

    def * = (partnerId, name) <> ((PartnerInfo.apply _).tupled, PartnerInfo.unapply)

    def partnerId = column[Int]("ID", O.Unique)

    def name = column[String]("PARTNER_NAME")

  }
}
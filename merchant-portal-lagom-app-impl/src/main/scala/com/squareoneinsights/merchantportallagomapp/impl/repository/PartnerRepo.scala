package com.squareoneinsights.merchantportallagomapp.impl.repository


import akka.Done
import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.impl.common.{GetMerchantErr, GetPartnerErr, MerchantTxnErr}

import scala.concurrent.{ExecutionContext, Future}

case class Partner(id:Int, name:String)

class PartnerRepo(db: Database)(implicit ec: ExecutionContext) extends PartnerTrait {

  val partnerTable    = TableQuery[PartnerTable]

  def fetchPartner: Future[Either[GetPartnerErr, Seq[Partner]]] = {
    val query = partnerTable.result .asTry.map { fromTryMerchant =>
      Either.fromTry(fromTryMerchant).leftMap(err => GetPartnerErr(err.getMessage))
    }
    db.run(query)
  }
}


trait PartnerTrait {

  class PartnerTable(tag: Tag) extends Table[Partner](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "PARTNER_DETAILS") {

    def * = (id , partnerName) <> ((Partner.apply _).tupled, Partner.unapply)

    def id = column[Int]("ID")

    def partnerName = column[String]("PARTNER_NAME")

  }
}
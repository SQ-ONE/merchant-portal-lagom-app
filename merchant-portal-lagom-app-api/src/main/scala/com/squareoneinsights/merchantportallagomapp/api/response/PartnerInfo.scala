package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class PartnerInfo(id: Int, partnerName: String)

object PartnerInfo {

  implicit val format: Format[PartnerInfo] = Json.format
}
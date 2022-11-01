package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Json, Format}

case class PartnerDetails (id:Int, partnerName:String)

object PartnerDetails {
  implicit val format: Format[PartnerDetails] = Json.format[PartnerDetails]
}

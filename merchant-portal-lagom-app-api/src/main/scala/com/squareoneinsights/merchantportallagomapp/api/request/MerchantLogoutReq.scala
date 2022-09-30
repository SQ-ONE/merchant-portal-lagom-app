package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

case class MerchantLogoutReq(
    userName: String
)

object MerchantLogoutReq {
  implicit val format: Format[MerchantLogoutReq] = Json.format
}

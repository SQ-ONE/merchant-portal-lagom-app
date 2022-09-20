package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

case class MerchantDetailsReq(
    merchantId: Int,
    userName: String,
    merchantName: String,
    mccCode: Int,
    isLoginActive: String
)

object MerchantDetailsReq {
  implicit val format: Format[MerchantDetailsReq] = Json.format
}

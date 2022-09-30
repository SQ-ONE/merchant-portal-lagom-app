package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class MerchantLoginResp(
    merchantId: String,
    merchantName: String,
    mccCode: String,
    isLoginActive: Boolean
)

object MerchantLoginResp {

  implicit val format: Format[MerchantLoginResp] = Json.format
}

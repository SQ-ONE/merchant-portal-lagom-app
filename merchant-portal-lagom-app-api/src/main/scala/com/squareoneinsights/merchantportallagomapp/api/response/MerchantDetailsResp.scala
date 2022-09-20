package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class MerchantDetailsResp(
    merchantId: Int,
    userName: String,
    merchantName: String,
    mccCode: Int,
    isLoginActive: String,
    approvalFlag: String
)

object MerchantDetailsResp {
  implicit val format: Format[MerchantDetailsResp] = Json.format
}

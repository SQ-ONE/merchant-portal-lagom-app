package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}


case class MerchantRiskScoreReq(merchantId: String,
                                oldRisk: String,
                                updatedRisk: String)

object MerchantRiskScoreReq {

  implicit val format: Format[MerchantRiskScoreReq] = Json.format
}

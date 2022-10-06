package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json, Reads}


case class MerchantRiskScoreReq(merchantId: String,
                                oldRisk: RiskType.Value,
                                updatedRisk: RiskType.Value)

object MerchantRiskScoreReq {
  implicit val enumReadsBucketLiability = Reads.enumNameReads(RiskType)
  implicit val format: Format[MerchantRiskScoreReq] = Json.format
}

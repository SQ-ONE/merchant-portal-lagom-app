package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json, Reads}

case class MerchantRiskScoreProducer(partnerId: Int, merchantId: String, oldRiskType: RiskType.Value, updatedListType: RiskType.Value)

object MerchantRiskScoreProducer {

  implicit val enumReadsBucketLiability = Reads.enumNameReads(RiskType)
  implicit val format: Format[MerchantRiskScoreProducer] = Json.format
}

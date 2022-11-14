package com.squareoneinsights.merchantportallagomapp.api.response

import com.squareoneinsights.merchantportallagomapp.api.request.RiskType
import play.api.libs.json.{Format, Json, Reads}

case class MerchantRiskScoreResp(merchantId: String,
                                 oldRisk:  RiskType.Value,
                                 updatedRisk:  RiskType.Value,
                                 approvalFlag: String)

object MerchantRiskScoreResp {

  implicit val enumReadsBucketLiability = Reads.enumNameReads(RiskType)
  implicit val format: Format[MerchantRiskScoreResp] = Json.format

  def getMerchantObj(merchantId: String, oldRisk: String) = {
    new MerchantRiskScoreResp(merchantId, RiskType.withName(oldRisk), RiskType.withName(oldRisk), "approve")
  }
}

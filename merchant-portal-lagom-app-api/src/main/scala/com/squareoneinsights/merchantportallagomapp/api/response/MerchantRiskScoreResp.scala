package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class MerchantRiskScoreResp(merchantId: String,
                                 oldRisk: String,
                                 updatedRisk: String,
                                 approvalFlag: String)

object MerchantRiskScoreResp {

  implicit val format: Format[MerchantRiskScoreResp] = Json.format

  def getMerchantObj(merchantId: String,oldRisk: String) = {
    new MerchantRiskScoreResp(merchantId, oldRisk, oldRisk, "approve")

  }
}

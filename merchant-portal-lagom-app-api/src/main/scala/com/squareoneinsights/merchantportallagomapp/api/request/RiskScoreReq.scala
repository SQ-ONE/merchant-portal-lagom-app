package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

case class RiskScoreReq(merchantId: String, isApproved: String)

object RiskScoreReq {

  implicit val format: Format[RiskScoreReq] = Json.format

}

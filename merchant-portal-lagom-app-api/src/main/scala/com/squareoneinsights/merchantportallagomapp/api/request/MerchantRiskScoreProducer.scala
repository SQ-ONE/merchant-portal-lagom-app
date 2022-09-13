package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

case class MerchantRiskScoreProducer(merchantId: String, listType: String)

object MerchantRiskScoreProducer {

  implicit val format: Format[MerchantRiskScoreProducer] = Json.format
}

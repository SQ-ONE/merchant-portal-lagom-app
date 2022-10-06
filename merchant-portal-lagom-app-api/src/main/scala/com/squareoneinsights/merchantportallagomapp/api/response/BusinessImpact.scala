package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class BusinessImpact(businessImpact: MerchantImpactDataResp)

object BusinessImpact {

  implicit val format : Format[BusinessImpact] = Json.format

  def getDate(businessImpact: MerchantImpactDataResp): BusinessImpact = {
    BusinessImpact.apply(businessImpact)
  }
}

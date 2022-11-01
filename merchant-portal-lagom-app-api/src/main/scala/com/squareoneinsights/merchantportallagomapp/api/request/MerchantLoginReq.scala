package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

case class MerchantLoginReq(userName:String, partnerId: Int, password:String)

object MerchantLoginReq {

  implicit val format: Format[MerchantLoginReq] = Json.format
}

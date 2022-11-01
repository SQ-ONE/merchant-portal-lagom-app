package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

case class LogOutReq(userName: String, partnerId:Int)

object LogOutReq {

  implicit val format: Format[LogOutReq] = Json.format
}

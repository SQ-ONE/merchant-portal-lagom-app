package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class LogoutResp(message: String)

object LogoutResp {

  implicit val format: Format[LogoutResp] = Json.format
}

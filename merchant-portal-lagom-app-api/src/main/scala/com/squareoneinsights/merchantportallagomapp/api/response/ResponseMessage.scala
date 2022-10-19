package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class ResponseMessage(message: String)

object ResponseMessage {

  implicit val format: Format[ResponseMessage] = Json.format
}

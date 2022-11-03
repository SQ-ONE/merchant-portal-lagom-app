package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class Logs(
                 logName: String,
                 logValue: String
               )

object Logs {

  implicit val format: Format[Logs] = Json.format
}

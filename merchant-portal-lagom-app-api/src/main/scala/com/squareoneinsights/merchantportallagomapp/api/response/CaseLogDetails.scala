package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class CaseLogDetails(logs: List[Logs])

object CaseLogDetails {

  implicit val format: Format[CaseLogDetails] = Json.format
}

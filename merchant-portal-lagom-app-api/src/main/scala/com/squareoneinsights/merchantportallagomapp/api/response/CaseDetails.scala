package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class CaseDetails(
    txnResult: String,
    violationDetails: String,
    txnId: String,
    txnAmount: String,
    txnTimestamp: String,
    investigatorComment: String,
    caseId: String
)

object CaseDetails {
  implicit val format: Format[CaseDetails] = Json.format
}

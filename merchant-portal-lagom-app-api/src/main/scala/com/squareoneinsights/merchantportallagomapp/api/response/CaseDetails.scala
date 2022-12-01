package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}
import java.sql.Timestamp

case class CaseDetails(
    txnResult: String,
    violationDetails: String,
    txnId: String,
    txnAmount: Double,
    txnTimestamp: String,
    investigatorComment: String,
    caseId: String,
    investigationStatus:String
)

object CaseDetails {
  implicit val format: Format[CaseDetails] = Json.format
}

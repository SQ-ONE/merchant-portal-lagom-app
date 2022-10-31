package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class TxnSearchCriteriaResult(
    channel: String,
    responseCode: String,
    txnAmount: String,
    txnTimestamp: String,
    txnType: String
)

object TxnSearchCriteriaResult {
  implicit val format: Format[TxnSearchCriteriaResult] = Json.format
}

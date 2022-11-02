package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}
import java.sql.Timestamp

case class TxnDetails(
    channel: String,
    customerId: String,
    txnId: String,
    txnAmount: Double,
    txnTimestamp: String,
    ifrmVerdict: String,
    instrument: String,
    location: String
)

object TxnDetails {
  implicit val format: Format[TxnDetails] = Json.format
}

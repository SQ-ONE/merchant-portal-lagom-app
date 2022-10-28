package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class MerchantTransactionDetails(
    txnDetails: TxnDetails,
    caseDetails: CaseDetails,
    caseLogDetails: CaseLogDetails
)

object MerchantTransactionDetails {
  implicit val format: Format[MerchantTransactionDetails] = Json.format
}

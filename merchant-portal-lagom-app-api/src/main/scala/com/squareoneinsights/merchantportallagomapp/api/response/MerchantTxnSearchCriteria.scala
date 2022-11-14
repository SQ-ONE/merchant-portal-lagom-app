package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class MerchantTxnSearchCriteria(txnSearchCriteria: List[TxnSearchCriteria])

case class TxnSearchCriteria(key:String, value:String)

object MerchantTxnSearchCriteria {
  implicit val txnSearchCriteriaformat: Format[TxnSearchCriteria] = Json.format

  implicit val format: Format[MerchantTxnSearchCriteria] = Json.format
}

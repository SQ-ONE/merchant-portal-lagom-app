package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

case class MerchantTxnSearchCriteria(
    criteria: String,
    result: List[String]
)

object MerchantTxnSearchCriteria {
  implicit val format: Format[MerchantTxnSearchCriteria] = Json.format
}

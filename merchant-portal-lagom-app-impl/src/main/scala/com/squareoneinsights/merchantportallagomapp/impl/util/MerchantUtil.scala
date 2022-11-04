package com.squareoneinsights.merchantportallagomapp.impl.util

object MerchantUtil {

  val conditionMap = Map("EQUAL" -> "=", "LESSTHEN" -> "<")

  val filterColumn = Map("channel" -> "CHANNEL", "responseCode" -> "RESPONSE_CODE", "txnAmount"-> "TXN_AMOUNT", "txnTimestamp" -> "TXN_TIMESTAMP", "txnType" -> "TXN_TYPE" )

}

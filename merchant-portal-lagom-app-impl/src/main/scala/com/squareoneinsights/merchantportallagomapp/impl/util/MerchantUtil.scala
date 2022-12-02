package com.squareoneinsights.merchantportallagomapp.impl.util

object MerchantUtil {

  val conditionMap = Map("EQUAL" -> "=", "LESSTHEN" -> "<")

  val filterColumn = Map("channel" -> "CHANNEL", "ifrmVerdict" -> "IFRM_VERDICT", "txnAmount"-> "TXN_AMOUNT", "txnTimestamp" -> "TXN_TIMESTAMP", "txnType" -> "INSTRUMENT" )

  def findLocation(lat:Double, long:Double): String = "Mumbai"
}

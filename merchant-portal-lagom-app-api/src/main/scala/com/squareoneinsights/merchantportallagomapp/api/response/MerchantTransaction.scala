package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

import java.sql.Timestamp

case class MerchantTransaction(txnId:String,
                               caseRefNo:String,
                               txnTimestamp:String,
                               txnAmount:Int,
                               ifrmVerdict:String,
                               investigationStatus:String,
                               channel:String,
                               txnType: String,
                               responseCode:String
                              )

object MerchantTransaction {
  implicit val format: Format[MerchantTransaction] = Json.format
}


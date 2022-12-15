package com.squareoneinsights.merchantportallagomapp.api.response

import play.api.libs.json.{Format, Json}

import java.sql.Timestamp

case class MerchantTransactionResp(txnId:String,
                                   caseRefNo:String,
                                   txnTimestamp:String,
                                   txnAmount:Double,
                                   ifrmVerdict:String,
                                   investigationStatus:String,
                                   channel:String,
                                   txnType: String,
                                   responseCode:String
                              )

object MerchantTransactionResp {
  implicit val format: Format[MerchantTransactionResp] = Json.format
}


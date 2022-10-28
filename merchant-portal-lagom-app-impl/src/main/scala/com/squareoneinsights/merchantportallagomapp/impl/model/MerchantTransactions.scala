package com.squareoneinsights.merchantportallagomapp.impl.model

import julienrf.json.derived
import play.api.libs.json.{Format, Json}

import java.sql.Timestamp

sealed trait MerchantTransactions

case class MerchantTransaction(
                                merchantId:String,
                                txnId:String,
                                caseRefNo:String,
                                txnTimestamp:Timestamp,
                                txnAmount:Int,
                                ifrmVerdict:String,
                                investigationStatus:String,
                                channel:String,
                                txnType:String,
                                responseCode:String,
                                customerId:String,
                                instrument:String,
                                location:String,
                                txnResult:String,
                                violationDetails:String,
                                investigatorComment:String,
                                caseId:String
                              )

case class MerchantTransactionLog(id:Option[Int], txnId:String, logName:String, logValue:String)

case class MerchantTransactionKafka(
                                merchantId:String,
                                txnId:String,
                                caseRefNo:String,
                                txnTimestamp:String,
                                txnAmount:Int,
                                ifrmVerdict:String,
                                investigationStatus:String,
                                channel:String,
                                txnType:String,
                                responseCode:String,
                                customerId:String,
                                instrument:String,
                                location:String,
                                txnResult:String,
                                violationDetails:String,
                                investigatorComment:String,
                                caseId:String
                              ) extends MerchantTransactions

case class MerchantTransactionLogKafka(txnId:String, logName:String, logValue:String) extends MerchantTransactions

object MerchantTransactions {
  implicit val format: Format[MerchantTransactions] = derived.oformat[MerchantTransactions]()

}

object MerchantTransactionKafka {

  implicit val format: Format[MerchantTransactionKafka] = Json.format[MerchantTransactionKafka]
}

object MerchantTransactionLogKafka {

  implicit val format: Format[MerchantTransactionLogKafka] =  Json.format
}
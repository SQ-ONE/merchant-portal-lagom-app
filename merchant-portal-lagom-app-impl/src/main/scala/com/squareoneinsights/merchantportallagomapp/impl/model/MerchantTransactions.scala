package com.squareoneinsights.merchantportallagomapp.impl.model

import julienrf.json.derived
import play.api.libs.json.Format
import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.sql.Timestamp

case class MerchantTransaction(
    partnerId: Int,
    merchantId: String,
    txnId: String,
    caseRefNo: String,
    txnTimestamp: Timestamp,
    txnAmount: Int,
    ifrmVerdict: String,
    investigationStatus: String,
    channel: String,
    txnType: String,
    responseCode: String,
    customerId: String,
    instrument: String,
    location: String,
    txnResult: String,
    violationDetails: String,
    investigatorComment: String,
    caseId: String
)

case class MerchantTransactionLog( caseId: String, logName: String, logValue: String)

sealed trait MerchantTransactionEvent

case class MerchantCaseUpdated(caseRefNo:String, investigationStatus:String, txnResult:String, investigatorComment:Option[String]) extends MerchantTransactionEvent

case class MerchantCaseCreated(   partnerId: Int,
                                  merchantId: String,
                                  txnId: String,
                                  caseRefNo: String,
                                  txnTimestamp: String,
                                  txnAmount: Int,
                                  ifrmVerdict: String,
                                  investigationStatus: String,
                                  channel: String,
                                  alertType: String,
                                  responseCode: String,
                                  customerId: String,  // entityId
                                  txnType: String,  // instrument
                                  lat: Double,
                                  long:Double,
                                  txnResult: String,
                                  violationDetails: String,
                                  investigatorComment: String,
                                  caseId: String
                              ) extends MerchantTransactionEvent

case class LogCreated(caseId:String, logName:String, logValue:String) extends MerchantTransactionEvent

object MerchantTransactionEvent {
  implicit val format: Format[MerchantTransactionEvent]                      = derived.oformat()
  implicit val merchantCaseDataFormat: OFormat[MerchantCaseUpdated]         = Json.format[MerchantCaseUpdated]
  implicit val merchantCaseCloserFormat: OFormat[MerchantCaseCreated]     = Json.format[MerchantCaseCreated]
  implicit val logCreatedFormat: OFormat[LogCreated]     = Json.format[LogCreated]

}
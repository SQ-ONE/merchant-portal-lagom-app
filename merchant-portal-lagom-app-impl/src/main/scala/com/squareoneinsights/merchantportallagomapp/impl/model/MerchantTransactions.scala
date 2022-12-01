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

case class CaseDetails(
                        caseRefNumber:String,
                        txnResult: String,
                        violationDetails: String,
                        txnId: String,
                        txnAmount: Double,
                        txnTimestamp: String,
                        investigatorComment: String,
                        caseId: String,
                        investigationStatus:String
                      )


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

case class MerchantTransactionDetails(
                                       txnDetails: TxnDetails,
                                       caseDetails: CaseDetails,
                                       caseLogDetails: List[MerchantTransactionLog]
                                     )

case class MerchantTransactionLog( caseRefNumber: String, logName: String, logValue: String)

case class AlertCategory( id: Int, categoryName: String)

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
                                  alertTypeId: Int,
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
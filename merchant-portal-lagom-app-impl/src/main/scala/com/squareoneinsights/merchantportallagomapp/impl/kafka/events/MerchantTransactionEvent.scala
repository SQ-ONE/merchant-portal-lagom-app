package com.squareoneinsights.merchantportallagomapp.impl.kafka.events

import julienrf.json.derived
import play.api.libs.json.{Format, Json, OFormat}

sealed trait MerchantTransactionEvent

case class MerchantCaseUpdated(caseRefNo:String, investigationStatus:String, txnResult:String, investigatorComment:Option[String], eventId:String) extends MerchantTransactionEvent

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
                                  caseId: String,
                                  eventId:String
                              ) extends MerchantTransactionEvent

case class LogCreated(logId:String,caseRefNum:String, logName:String, logValue:String) extends MerchantTransactionEvent

object MerchantTransactionEvent {
  implicit val format: Format[MerchantTransactionEvent]                      = derived.oformat()
  implicit val merchantCaseDataFormat: OFormat[MerchantCaseUpdated]         = Json.format[MerchantCaseUpdated]
  implicit val merchantCaseCloserFormat: OFormat[MerchantCaseCreated]     = Json.format[MerchantCaseCreated]
  implicit val logCreatedFormat: OFormat[LogCreated]     = Json.format[LogCreated]

}
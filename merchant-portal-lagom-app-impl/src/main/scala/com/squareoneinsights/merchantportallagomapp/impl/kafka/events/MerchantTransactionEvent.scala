package com.squareoneinsights.merchantportallagomapp.impl.kafka.events

import ai.x.play.json.Jsonx
import julienrf.json.derived
import play.api.libs.json.{Format, Json, OFormat}

sealed trait MerchantTransactionEvent

case class MerchantCaseUpdated(caseRefNo:String, investigationStatus:String, txnResult:String, investigatorComment:Option[String], eventId:String) extends MerchantTransactionEvent

case class MerchantCaseCreated(caseId: Int,
                               partnerId: Int,
                               caseRefNo: String,
                               txnAmount: Double,
                               txnTimestamp: String,
                               txnId: String,
                               txnType: String,
                               channel: String,
                               entityId: String,
                               entityCategory: String,
                               merchantId: String,
                               alertTypeId: Int,
                               responseCode: Option[String],
                               customerId: Option[String],
                               ifrmVerdict: String,
                               caseVerdict: String,
                               lastUpdatedTimestamp: Option[String],
                               currentlyAssignedTo: String,
                               remarks: String,
                               fraudType: String,
                               weight: Int,
                               currentStage: String,
                               currentStatus: String,
                               liability: String,
                               rrn: String,
                               isAcknowledged: Int,
                               isHold: Int,
                               bucket: String,
                               violationDetails: String,
                               caseDisplayNo: String,
                               lastActivityBy: String,
                               lastActionName: String,
                               txnResult: String,
                               lat:Double,
                               long:Double,
                               eventId:String
                              ) extends MerchantTransactionEvent

case class LogCreated(logId:String,caseRefNum:String, logName:String, logValue:String) extends MerchantTransactionEvent

object MerchantTransactionEvent {
  implicit val format: Format[MerchantTransactionEvent]                      = derived.oformat()
  implicit val merchantCaseDataFormat: OFormat[MerchantCaseUpdated]         = Json.format[MerchantCaseUpdated]
  implicit val merchantCaseCloserFormat: OFormat[MerchantCaseCreated]     = Jsonx.formatCaseClass[MerchantCaseCreated]
  implicit val logCreatedFormat: OFormat[LogCreated]     = Json.format[LogCreated]

}
package com.squareoneinsights.merchantportallagomapp.impl.model

import julienrf.json.derived
import play.api.libs.json.Format
import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.sql.Timestamp

sealed trait MerchantTransactions

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

case class MerchantTransactionLog(id: Option[Int], txnId: String, logName: String, logValue: String)

case class MerchantTransactionKafka(
    partnerId: Int,
    merchantId: String,
    txnId: String,
    caseRefNo: String,
    txnTimestamp: String,
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
) //extends MerchantTransactions

case class MerchantTransactionLogKafka(txnId: String, logName: String, logValue: String) extends MerchantTransactions

object MerchantTransactions {
  implicit val format: Format[MerchantTransactions] = derived.oformat[MerchantTransactions]()

}

object MerchantTransactionKafka {

  implicit val format: Format[MerchantTransactionKafka] = Json.format[MerchantTransactionKafka]
}

object MerchantTransactionLogKafka {

  implicit val format: Format[MerchantTransactionLogKafka] = Json.format
}

case class MerchantCaseCloser(caseRefNo: String, investigationStatus: String) extends MerchantTransactions

case class MerchantCaseNotation(caseId: Int, comment: String) extends MerchantTransactions

case class MerchantCaseData(
    partnerId: Int,
    merchantId: String,
    txnId: String,
    caseRefNo: String,
    txnTimestamp: String,
    txnAmount: Int,
    ifrmVerdict: String,
    investigationStatus: String,
    channel: String,
    alertTypeId: String,
    responseCode: String,
    customerId: String, // entityId
    txnType: String,    // instrument
    lat: Double,
    long: Double,
    txnResult: String,
    violationDetails: String,
    investigatorComment: String,
    caseId: String
) extends MerchantTransactions

object MerchantCaseData {
  implicit val format: Format[MerchantTransactions]                      = derived.oformat()
  implicit val merchantCaseDataFormat: OFormat[MerchantCaseData]         = Json.format[MerchantCaseData]
  implicit val merchantCaseCloserFormat: OFormat[MerchantCaseCloser]     = Json.format[MerchantCaseCloser]
  implicit val merchantCaseNotationFormat: OFormat[MerchantCaseNotation] = Json.format[MerchantCaseNotation]
}

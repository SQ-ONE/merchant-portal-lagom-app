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
    txnAmount: Double,
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

case class MerchantTransactionLog( logId:String, caseRefNumber: String, logName: String, logValue: String)

case class AlertCategory( id: Int, categoryName: String)


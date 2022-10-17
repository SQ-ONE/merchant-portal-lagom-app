package com.squareoneinsights.merchantportallagomapp.impl.repository

import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._

class MerchantTransactionRepo {

}

trait MerchantTransactionLogTrait {
  class MerchantTransactionLogTable(tag: Tag) extends Table[MerchantTransactionLog](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "MERCHANT_TRANSACTION_LOG") {

    def * = (id, txnId, logName, logValue) <> ((MerchantTransactionLog.apply _).tupled, MerchantTransactionLog.unapply)

    def id = column[Int]("TXN_ID")

    def txnId = column[String]("TXN_ID")

    def logName = column[String]("TXN_ID")

    def logValue = column[String]("TXN_ID")


  }
}

trait MerchantTransactionTrait  {

  class MerchantTransactionTable(tag: Tag) extends Table[MerchantTransaction](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "MERCHANT_TRANSACTION_DETAILS") {

    def * = (merchantId, txnId, caseRefNo, txnTimestamp, txnAmount, ifrmVerdict, investigationStatus, channel, txnType, responseCode, customerId,
      instrument, location, txnResult, violationDetails, investigatorComment, caseId) <> ((MerchantTransaction.apply _).tupled, MerchantTransaction.unapply)

  //  def activityId = column[Option[Int]]("ACTIVITY_ID", O.PrimaryKey, O.AutoInc)

    def merchantId = column[String]("MERCHANT_ID")

    def txnId = column[String]("TXN_ID")

    def caseRefNo = column[String]("CASE_REF_NO")

    def txnTimestamp = column[String]("TXN_TIMESTAMP")

    def txnAmount = column[String]("TXN_AMOUNT")

    def ifrmVerdict = column[String]("IFRM_VERDICT")

    def investigationStatus = column[String]("INVESTIGATION_STATUS")

    def channel = column[String]("CHANNEL")

    def txnType = column[String]("TXNTYPE")

    def responseCode = column[String]("RESPONSE_CODE")

    def customerId = column[String]("CUSTOMER_ID")

    def instrument = column[String]("INSTRUMENT")

    def location = column[String]("LOCATION")

    def txnResult = column[String]("TXN_RESULT")

    def violationDetails = column[String]("VIOLATION_DETAILS")

    def investigatorComment = column[String]("INVESTIGATOR_COMMENT")

    def caseId = column[String]("CASE_ID")

  }
}

case class MerchantTransaction(
                                merchantId:String,
                               txnId:String,
                               caseRefNo:String,
                               txnTimestamp:String,
                               txnAmount:String,
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

case class MerchantTransactionLog(id:Int, txnId:String, logName:String, logValue:String)

/*
"ID" serial4 NOT NULL,
"TXN_ID" VARCHAR(50)  NOT NULL,
"LOG_NAME"  VARCHAR(50) NULL,
"LOG_VALUE" VARCHAR(500) NULL
*/

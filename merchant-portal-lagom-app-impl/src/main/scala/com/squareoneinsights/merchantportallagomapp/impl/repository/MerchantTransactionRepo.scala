package com.squareoneinsights.merchantportallagomapp.impl.repository

import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.impl.common.{MerchantPortalError, MerchantTxnErr}
import slick.jdbc.GetResult

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

case class FilterTXN(key:String, condition:String, value:String)

class MerchantTransactionRepo(db: Database)
                             (implicit ec: ExecutionContext) extends MerchantTransactionTrait with MerchantTransactionLogTrait {

  val merchantTransactionTable = TableQuery[MerchantTransactionTable]
  val merchantTransactionLogTable = TableQuery[MerchantTransactionLogTable]


  def getTransactionsByType(merchantId: String, txnType: String): Future[Either[MerchantPortalError, Seq[MerchantTransaction]]] = {
    val query = merchantTransactionTable.filter(m => m.merchantId === merchantId && m.txnType === txnType)
      .result.asTry.map { merchantWithTry =>
      Either.fromTry(merchantWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
    }
    db.run(query)
  }

   def tailRecFilter(flt: List[FilterTXN]): String = {
     @tailrec
   def supportFilter(f: List[FilterTXN], acc:String): String = {
  if (f.isEmpty) acc else {
    supportFilter(f.tail, acc.concat(s"""AND "${f.head.key}" ${f.head.condition} '${f.head.value}' """))
  }
}
     supportFilter(flt, "")
  }



  implicit val getSupplierResult = GetResult(r => MerchantTransaction(r.nextString, r.nextString, r.nextString,
    r.nextString, r.nextInt, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString))

  def getTransactionsBySearch(merchantId: String, txnType: String, flt:List[FilterTXN]): Future[Either[MerchantPortalError, Seq[MerchantTransaction]]] = {

    val sql = sql""" select * from "IFRM_LIST_LIMITS"."MERCHANT_TRANSACTION_LOG" where
                     "MERCHANT_ID" = $merchantId AND "TXNTYPE" = $txnType ${tailRecFilter(flt)}
         """.as[MerchantTransaction]

    val resp =sql.asTry.map { merchantWithTry =>
      Either.fromTry(merchantWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
    }
    db.run(resp)
  }
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

    def txnAmount = column[Int]("TXN_AMOUNT")

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

case class MerchantTransactionLog(id:Int, txnId:String, logName:String, logValue:String)


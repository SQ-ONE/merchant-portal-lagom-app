package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantTransactionResp
import com.squareoneinsights.merchantportallagomapp.impl.common.{MerchantPortalError, MerchantTxnErr}
import com.squareoneinsights.merchantportallagomapp.impl.model.{MerchantTransaction, MerchantTransactionLog}
import slick.jdbc.GetResult

import java.sql.Timestamp
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
  def saveTransaction(merchantTxn:MerchantTransaction): Future[Either[MerchantPortalError, Done]] = {
    val query = merchantTransactionTable += merchantTxn
    db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover {
      case ex => MerchantTxnErr(ex.getMessage).asLeft[Done]
    }
  }

  def saveTransactionLog(merchantTxnLog:MerchantTransactionLog): Future[Either[MerchantPortalError, Done]] = {
    val query = merchantTransactionLogTable += merchantTxnLog
    db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover {
      case ex => MerchantTxnErr(ex.getMessage).asLeft[Done]
    }
  }
   def tailRecFilter(flt: List[FilterTXN]): String = {
     @tailrec
   def supportFilter(f: List[FilterTXN], acc:String): String = {
  if (f.isEmpty) acc else {
    supportFilter(f.tail, acc.concat(s""" AND "${f.head.key}" ${f.head.condition} '${f.head.value}' """))
  }
}
     supportFilter(flt, "")
  }



  implicit val getSupplierResult = GetResult(r => MerchantTransactionResp(r.nextString, r.nextString, r.nextTimestamp.toString,
    r.nextInt, r.nextString, r.nextString, r.nextString, r.nextString, r.nextString))

  def getTransactionsBySearch(merchantId: String, txnType: String, flt:List[FilterTXN]): Future[Either[MerchantPortalError, Seq[MerchantTransactionResp]]] = {

    val sql = sql""" SELECT "TXN_ID","CASE_REF_NO","TXN_TIMESTAMP","TXN_AMOUNT","IFRM_VERDICT", "INVESTIGATION_STATUS",
                     "CHANNEL","TXN_TYPE","RESPONSE_CODE"
                   FROM "IFRM_LIST_LIMITS"."MERCHANT_TRANSACTION_DETAILS" WHERE
                     "MERCHANT_ID" = '#$merchantId' AND "TXN_TYPE" = '#$txnType' #${tailRecFilter(flt)}
                     ORDER BY "TXN_TIMESTAMP" DESC
         """.as[MerchantTransactionResp]

    val resp =sql.asTry.map { merchantWithTry =>
      Either.fromTry(merchantWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
    }
    db.run(resp) // todo
  }
}

trait MerchantTransactionLogTrait {
  class MerchantTransactionLogTable(tag: Tag) extends Table[MerchantTransactionLog](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "MERCHANT_TRANSACTION_LOG") {

    def * = (id, txnId, logName, logValue) <> ((MerchantTransactionLog.apply _).tupled, MerchantTransactionLog.unapply)

    def id = column[Option[Int]]("ID")

    def txnId = column[String]("TXN_ID")

    def logName = column[String]("LOG_NAME")

    def logValue = column[String]("LOG_ID")


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

    def txnTimestamp = column[Timestamp]("TXN_TIMESTAMP")

    def txnAmount = column[Int]("TXN_AMOUNT")

    def ifrmVerdict = column[String]("IFRM_VERDICT")

    def investigationStatus = column[String]("INVESTIGATION_STATUS")

    def channel = column[String]("CHANNEL")

    def txnType = column[String]("TXN_TYPE")

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
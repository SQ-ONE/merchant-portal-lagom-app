package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantTransactionResp
import com.squareoneinsights.merchantportallagomapp.impl.common.MerchantPortalError
import com.squareoneinsights.merchantportallagomapp.impl.common.MerchantTxnErr
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantTransaction
import com.squareoneinsights.merchantportallagomapp.impl.model.MerchantTransactionLog
import slick.jdbc.GetResult

import java.sql.Timestamp
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.squareoneinsights.merchantportallagomapp.api.response.MerchantTransactionDetails
import com.squareoneinsights.merchantportallagomapp.api.response.TxnDetails
import com.squareoneinsights.merchantportallagomapp.api.response.CaseDetails
import com.squareoneinsights.merchantportallagomapp.api.response.CaseLogDetails
import com.squareoneinsights.merchantportallagomapp.api.response.Logs

case class FilterTXN(key: String, condition: String, value: String)

class MerchantTransactionRepo(db: Database)(implicit ec: ExecutionContext)
    extends MerchantTransactionTrait
    with MerchantTransactionLogTrait {

  val merchantTransactionTable    = TableQuery[MerchantTransactionTable]
  val merchantTransactionLogTable = TableQuery[MerchantTransactionLogTable]

  def getTransactionsByType(
      merchantId: String,
      txnType: String
  ): Future[Either[MerchantPortalError, Seq[MerchantTransaction]]] = {
    val query =
      merchantTransactionTable.filter(m => m.merchantId === merchantId && m.txnType === txnType).result.asTry.map {
        merchantWithTry =>
          Either.fromTry(merchantWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
      }
    db.run(query)
  }
  def saveTransaction(merchantTxn: MerchantTransaction): Future[Either[MerchantPortalError, Done]] = {
    val query = merchantTransactionTable += merchantTxn
    db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover { case ex =>
      MerchantTxnErr(ex.getMessage).asLeft[Done]
    }
  }

  def saveTransactionLog(merchantTxnLog: MerchantTransactionLog): Future[Either[MerchantPortalError, Done]] = {
    val query = merchantTransactionLogTable += merchantTxnLog
    db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover { case ex =>
      MerchantTxnErr(ex.getMessage).asLeft[Done]
    }
  }


  implicit val getSupplierResult = GetResult(r =>
    MerchantTransactionResp(
      r.nextString,
      r.nextString,
      r.nextTimestamp.toString,
      r.nextInt,
      r.nextString,
      r.nextString,
      r.nextString,
      r.nextString,
      r.nextString
    )
  )

  def getFilter(filterTXN: FilterTXN): String = filterTXN.key match {
     case "TXN_TIMESTAMP" => s"""AND "TXN_TIMESTAMP" >= '${filterTXN.value} 00:00:00.000' AND "TXN_TIMESTAMP" <= '${filterTXN.value} 23:59:59.900'"""
     case _ => s"""AND "#${filterTXN.key}" #${filterTXN.condition} '#${filterTXN.value}' """
   }

  def getTransactionsBySearch(
      merchantId: String,
      txnType: String,
      obj: FilterTXN,
      partnerId: Int
  ): Future[Either[MerchantPortalError, Seq[MerchantTransactionResp]]] = {

    val sql = sql""" SELECT "TXN_ID","CASE_REF_NO","TXN_TIMESTAMP","TXN_AMOUNT","IFRM_VERDICT", "INVESTIGATION_STATUS",
                     "CHANNEL","TXN_TYPE","RESPONSE_CODE"
                   FROM "IFRM_LIST_LIMITS"."MERCHANT_TRANSACTION_DETAILS" WHERE
                     "MERCHANT_ID" = '#$merchantId' AND "TXN_TYPE" = '#$txnType' #${getFilter(obj)}  AND "PARTNER_ID" = '#$partnerId'
                     ORDER BY "TXN_TIMESTAMP" DESC
         """.as[MerchantTransactionResp]

    val resp = sql.asTry.map { merchantWithTry =>
      Either.fromTry(merchantWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
    }
    db.run(resp) // todo
  }

  def getTransactionDetails(
                             txnType: String,
                             txnId: String,
                             merchantId: String,
                             partnerId: Int
                           ): Future[Either[String, MerchantTransactionDetails]] = {
    val query = merchantTransactionTable
      .filter { col =>
        (col.merchantId === merchantId && col.txnId === txnId && col.txnType === txnType && col.partnerId === partnerId)
      }
      .join(merchantTransactionLogTable)
      .on(_.txnId === _.txnId)
      .result
      .asTry
      .map { txnWithTry =>
        val fromTry =
          Either.fromTry(txnWithTry).leftMap(err => err.getMessage)
        val fromOption = fromTry.flatMap { fromTrySeq =>
          Either.fromOption(
            fromTrySeq.headOption,
            s"No transaction found with merchant id: $merchantId, with transaction id: $txnId and with transaction type: $txnType"
          )
        }
        fromOption.map { x =>
        {
          val txnDetails = TxnDetails(
            x._1.channel,
            x._1.customerId,
            x._1.txnId,
            x._1.txnAmount,
            x._1.txnTimestamp.toString,
            x._1.ifrmVerdict,
            x._1.instrument,
            x._1.location
          )

          val caseDetails = CaseDetails(
            x._1.txnResult,
            x._1.violationDetails,
            x._1.txnId,
            x._1.txnAmount,
            x._1.txnTimestamp.toString,
            x._1.investigatorComment,
            x._1.caseId: String
          )

          val logDetails = Logs(
            x._2.logName,
            x._2.logValue
          )

          val caseLogDetails = (List() :+ logDetails)
          MerchantTransactionDetails(txnDetails, caseDetails, caseLogDetails)
        }
        }
      }
    db.run(query)
  }
}

trait MerchantTransactionLogTrait {
  class MerchantTransactionLogTable(tag: Tag)
      extends Table[MerchantTransactionLog](tag, _schemaName = Option("IFRM_LIST_LIMITS"), "MERCHANT_TRANSACTION_LOGS") {

    def * = (id, txnId, logName, logValue) <> ((MerchantTransactionLog.apply _).tupled, MerchantTransactionLog.unapply)

    def id = column[Option[Int]]("ID")

    def txnId = column[String]("TXN_ID")

    def logName = column[String]("LOG_NAME")

    def logValue = column[String]("LOG_VALUE")

  }
}

trait MerchantTransactionTrait {

  class MerchantTransactionTable(tag: Tag)
      extends Table[MerchantTransaction](
        tag,
        _schemaName = Option("IFRM_LIST_LIMITS"),
        "MERCHANT_TRANSACTION_DETAILS"
      ) {

    def * = (
      partnerId,
      merchantId,
      txnId,
      caseRefNo,
      txnTimestamp,
      txnAmount,
      ifrmVerdict,
      investigationStatus,
      channel,
      txnType,
      responseCode,
      customerId,
      instrument,
      location,
      txnResult,
      violationDetails,
      investigatorComment,
      caseId
    ) <> ((MerchantTransaction.apply _).tupled, MerchantTransaction.unapply)

    //  def activityId = column[Option[Int]]("ACTIVITY_ID", O.PrimaryKey, O.AutoInc)

    def partnerId  = column[Int]("PARTNER_ID")
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

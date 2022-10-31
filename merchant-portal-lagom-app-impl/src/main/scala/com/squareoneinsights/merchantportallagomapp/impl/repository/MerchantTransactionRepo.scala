package com.squareoneinsights.merchantportallagomapp.impl.repository

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp
import scala.annotation.tailrec
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.api.response.{
  MerchantTransactionDetails,
  TxnDetails,
  CaseDetails,
  CaseLogDetails,
  Logs
}

import scala.concurrent.{ExecutionContext, Future}

class MerchantTransactionRepo(db: Database)(implicit ec: ExecutionContext)
    extends MerchantTransactionLogTrait
    with MerchantTransactionTrait {

  val merchantTransactionLog = TableQuery[MerchantTransactionLogTable]
  val merchantTransaction = TableQuery[MerchantTransactionTable]

  def getTransactionDetails(
      txnType: String,
      txnId: String,
      merchantId: String
  ): Future[Either[String, MerchantTransactionDetails]] = {
    val query = merchantTransaction
      .filter(_.merchantId === merchantId)
      .filter(_.txnId === txnId)
      .filter(_.txnType === txnType)
      .join(merchantTransactionLog)
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

            val caseLogDetails = CaseLogDetails(List() :+ logDetails)
            MerchantTransactionDetails(txnDetails, caseDetails, caseLogDetails)
          }
        }
      }
    db.run(query)
  }
}
trait MerchantTransactionLogTrait {
  class MerchantTransactionLogTable(tag: Tag)
      extends Table[MerchantTransactionLog](
        tag,
        _schemaName = Option("IFRM_LIST_LIMITS"),
        "MERCHANT_TRANSACTION_LOGS"
      ) {

    def * = (
      id,
      txnId,
      logName,
      logValue
    ) <> ((MerchantTransactionLog.apply _).tupled, MerchantTransactionLog.unapply)

    def id = column[Int]("ID")

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

    def merchantId = column[String]("MERCHANT_ID")

    def txnId = column[String]("TXN_ID")

    def caseRefNo = column[String]("CASE_REF_NO")

    def txnTimestamp = column[Timestamp]("TXN_TIMESTAMP")

    def txnAmount = column[Double]("TXN_AMOUNT")

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

case class MerchantTransactionLog(
    id: Int,
    txnId: String,
    logName: String,
    logValue: String
)

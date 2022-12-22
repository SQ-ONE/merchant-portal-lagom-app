package com.squareoneinsights.merchantportallagomapp.impl.repository

import akka.Done
import slick.jdbc.PostgresProfile.api._
import cats.syntax.either._
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantTransactionResp
import com.squareoneinsights.merchantportallagomapp.impl.common.MerchantPortalError
import com.squareoneinsights.merchantportallagomapp.impl.common.MerchantTxnErr
import com.squareoneinsights.merchantportallagomapp.impl.kafka.events.{LogCreated, MerchantCaseUpdated}
import com.squareoneinsights.merchantportallagomapp.impl.model.{AlertCategory, MerchantTransaction, MerchantTransactionLog}
import slick.jdbc.GetResult

import java.sql.Timestamp
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class FilterTXN(key: String, condition: String, value: String)

class MerchantTransactionRepo(db: Database)(implicit ec: ExecutionContext)
    extends MerchantTransactionTrait
    with MerchantTransactionLogTrait
    with MerchantTransactionCategoryTrait {

  val merchantTransactionTable    = TableQuery[MerchantTransactionTable]
  val merchantTransactionLogTable = TableQuery[MerchantTransactionLogTable]

  val merchantTransactionCategoryTable = TableQuery[MerchantTransactionCategoryTable]

  def getCategory(id:Int): Future[Either[MerchantTxnErr, AlertCategory]] ={
    val query =
      merchantTransactionCategoryTable.filter(m => m.id === id).result
        .asTry
        .map { txnWithTry =>
          val fromTry =
            Either.fromTry(txnWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
           fromTry.flatMap { fromTrySeq =>
            Either.fromOption(
              fromTrySeq.headOption,
              MerchantTxnErr(s"no category found with id = $id")
            )
          }
  }
  db.run(query)
  }

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

     db.run(merchantTransactionTable.filter(_.caseId ===merchantTxn.caseId).result).flatMap{ merchantWithTry =>
       println(merchantWithTry)
       val query =if(merchantWithTry.isEmpty) merchantTransactionTable += merchantTxn
                    else merchantTransactionTable.filter(_.caseId ===merchantTxn.caseId).update(merchantTxn)
         db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover { case ex =>
          MerchantTxnErr(ex.getMessage).asLeft[Done]
      }
      }
  }



  def updateByCaseRefNo(merchantCaseCloser: MerchantCaseUpdated): Future[Either[MerchantPortalError, Done]] = {
   val query = merchantCaseCloser.investigatorComment match {
     case Some(investigatorComment) => merchantTransactionTable
       .filter(_.caseRefNo === merchantCaseCloser.caseRefNo)
       .map(col =>( col.investigationStatus, col.txnResult, col.investigatorComment))
       .update((merchantCaseCloser.investigationStatus, merchantCaseCloser.txnResult, investigatorComment))

     case None => merchantTransactionTable
       .filter(_.caseRefNo === merchantCaseCloser.caseRefNo)
       .map(col =>( col.investigationStatus, col.txnResult))
       .update(merchantCaseCloser.investigationStatus, merchantCaseCloser.txnResult)
   }
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
    case "TXN_TIMESTAMP" =>
      s"""AND "TXN_TIMESTAMP" >= '${filterTXN.value} 00:00:00.000' AND "TXN_TIMESTAMP" <= '${filterTXN.value} 23:59:59.900'"""
    case "TXN_AMOUNT" => s"""AND "${filterTXN.key}" ${filterTXN.condition} '${filterTXN.value}' """
    case _ => s"""AND Lower("${filterTXN.key}") ${filterTXN.condition} Lower('${filterTXN.value}') """
  }

  def getTransactionsBySearch(
      merchantId: String,
      txnType: String,
      obj: FilterTXN,
      partnerId: Int
  ): Future[Either[MerchantPortalError, Seq[MerchantTransactionResp]]] = {

    val sql = sql""" SELECT "TXN_ID","CASE_REF_NO","TXN_TIMESTAMP","TXN_AMOUNT","IFRM_VERDICT", "INVESTIGATION_STATUS",
                     "CHANNEL","INSTRUMENT","RESPONSE_CODE"
                   FROM "MERCHANT_PORTAL_ALERT_TRANSACTION"."MERCHANT_TRANSACTION_DETAILS" WHERE
                     "MERCHANT_ID" = '#$merchantId' AND "TXN_TYPE" = '#$txnType' #${getFilter(obj)}  AND "PARTNER_ID" = '#$partnerId'
                     ORDER BY "TXN_TIMESTAMP" DESC
         """.as[MerchantTransactionResp]

    val resp = sql.asTry.map { merchantWithTry =>
      Either.fromTry(merchantWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
    }
    db.run(resp) // todo
  }

  def getTransaction(txnType: String,
                     txnId: String,
                     merchantId: String,
                     partnerId: Int): Future[Either[MerchantTxnErr, MerchantTransaction]]  = {
    val query = merchantTransactionTable
      .filter { col =>
        (col.merchantId === merchantId && col.txnId === txnId && col.txnType === txnType && col.partnerId === partnerId)
      }
      .result
      .asTry
      .map { txnWithTry =>
        val fromTry =
          Either.fromTry(txnWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
         fromTry.flatMap { fromTrySeq =>
          Either.fromOption(
            fromTrySeq.headOption,
            MerchantTxnErr(s"No transaction found with merchant id: $merchantId, with transaction id: $txnId and with transaction type: $txnType")
          )
        }
  }
    db.run(query)
  }

  def getLogs(caseRefNo:String) : Future[Either[MerchantTxnErr,Seq[(String,String)]]] = {
   val query = merchantTransactionLogTable.filter(_.caserefNo === caseRefNo).map(col => (col.logName, col.logValue))
      .result
      .asTry.map {
      merchantWithTry =>
        Either.fromTry(merchantWithTry).leftMap(err => MerchantTxnErr(err.getMessage))
    }
    db.run(query)
  }

  def insertCaseLogs(log:LogCreated): Future[Either[MerchantPortalError, Done]] = {
val data = MerchantTransactionLog(log.logId, log.caseRefNum,log.logName,log.logValue)
    db.run(merchantTransactionLogTable.filter(_.logId ===log.logId).result).flatMap{ merchantLogTry =>
      println(merchantLogTry)
      val query =if(merchantLogTry.isEmpty) merchantTransactionLogTable += data
      else merchantTransactionLogTable.filter(_.logId === data.logId).update(data)
      db.run(query).map(_ => Done.asRight[MerchantPortalError]).recover { case ex =>
        MerchantTxnErr(ex.getMessage).asLeft[Done]
      }
    }
  }

}



trait MerchantTransactionLogTrait {
  class MerchantTransactionLogTable(tag: Tag)
      extends Table[MerchantTransactionLog](
        tag,
        _schemaName = Option("MERCHANT_PORTAL_ALERT_TRANSACTION"),
        "MERCHANT_TRANSACTION_LOGS"
      ) {

    def * = ( logId,caserefNo, logName, logValue) <> ((MerchantTransactionLog.apply _).tupled, MerchantTransactionLog.unapply)

    def logId = column[String]("LOG_ID")

    def caserefNo = column[String]("CASE_REF_NO")

    def logName = column[String]("LOG_NAME")

    def logValue = column[String]("LOG_VALUE")

  }
}

trait MerchantTransactionTrait {

  class MerchantTransactionTable(tag: Tag)
      extends Table[MerchantTransaction](
        tag,
        _schemaName = Option("MERCHANT_PORTAL_ALERT_TRANSACTION"),
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

    def txnAmount = column[Double]("TXN_AMOUNT")

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


trait MerchantTransactionCategoryTrait {
  class MerchantTransactionCategoryTable(tag: Tag)
    extends Table[AlertCategory](
      tag,
      _schemaName = Option("MERCHANT_PORTAL_ALERT_TRANSACTION"),
      "ALERT_CATEGORY"
    ) {

    def * = ( id, categoryName) <> ((AlertCategory.apply _).tupled, AlertCategory.unapply)

    def id = column[Int]("ID")

    def categoryName = column[String]("CATEGORY_NAME")

  }
}

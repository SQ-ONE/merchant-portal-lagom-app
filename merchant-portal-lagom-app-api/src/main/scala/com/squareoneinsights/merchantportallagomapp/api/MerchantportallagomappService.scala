package com.squareoneinsights.merchantportallagomapp.api

import akka.Done
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service.restCall
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.squareoneinsights.merchantportallagomapp.api.request.LogOutReq
import com.squareoneinsights.merchantportallagomapp.api.request.MerchantLoginReq
import com.squareoneinsights.merchantportallagomapp.api.request.MerchantRiskScoreReq
import com.squareoneinsights.merchantportallagomapp.api.request.TransactionFilterReq
import com.squareoneinsights.merchantportallagomapp.api.response.BusinessImpact
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantImpactDataResp
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantLoginResp
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantRiskScoreResp
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantTransactionResp
import com.squareoneinsights.merchantportallagomapp.api.response.BusinessImpact
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantImpactDataResp
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantLoginResp
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantRiskScoreResp
import com.squareoneinsights.merchantportallagomapp.api.response.ResponseMessage
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantTxnSearchCriteria
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantTransactionDetails
import play.api.libs.json.Format
import play.api.libs.json.Json

trait MerchantportallagomappService extends Service {

  def hello(name: String): ServiceCall[NotUsed, String]

  def getRiskScore(merchantId: String, partnerId: Int): ServiceCall[NotUsed, MerchantRiskScoreResp]

  def addRiskType(partnerId: Int) : ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp]

  def getMerchantImpactData(merchantId: String, partnerId: Int): ServiceCall[NotUsed, BusinessImpact]

  def login: ServiceCall[MerchantLoginReq, MerchantLoginResp]

  def logOut: ServiceCall[LogOutReq, ResponseMessage]

  def getTransactions(txnType: String, merchantId: String, partnerId: Int): ServiceCall[NotUsed, List[MerchantTransactionResp]]

  def getTransactionsBySearch(
      txnType: String,
      merchantId: String,
      partnerId: Int
  ): ServiceCall[TransactionFilterReq, List[MerchantTransactionResp]]

  def getTxnSearchCriteriaList: ServiceCall[NotUsed, MerchantTxnSearchCriteria]

  def getTxnDetails(
      txnType: String,
      txnId: String,
      merchantId: String,
      partnerId: Int
  ): ServiceCall[NotUsed, MerchantTransactionDetails]

  final override def descriptor: Descriptor = {
    import Service._
    named("merchant-portal-lagom-apps")
      .withCalls(
        restCall(Method.GET,"/api/hello/:name", hello _),
        restCall(Method.GET, "/api/v1/merchantportal/risksetting/merchant/:merchantId/partner/:partnerId",  getRiskScore _),
        restCall(Method.POST, "/api/v1/merchantportal/risksetting/merchant/:merchantId/partner/:partnerId",  addRiskType _),
        restCall(Method.GET, "/api/v1/merchantportal/merchant/business/merchantId/:merchantId/partner/:partnerId",  getMerchantImpactData _),
        restCall(Method.POST, "/api/v1/merchantportal/login",  login),
        restCall(Method.POST, "/api/v1/merchantportal/logout",  logOut),
        restCall(Method.GET, "/api/v1/merchantportal/txn/:txnType/:merchantId/:partnerId", getTransactions _),
        restCall(Method.POST, "/api/v1/merchantportal/search/:txnType/:merchantId/:partnerId", getTransactionsBySearch _),
        restCall(
          Method.GET,
          "/api/v1/merchantportal/search/list",
          getTxnSearchCriteriaList
        ),
        restCall(
          Method.GET,
          "/api/v1/merchantportal/txn/:txnType/:txnId/:merchantId",
          getTxnDetails _
        )
      )
      .withAutoAcl(true)
  }
}

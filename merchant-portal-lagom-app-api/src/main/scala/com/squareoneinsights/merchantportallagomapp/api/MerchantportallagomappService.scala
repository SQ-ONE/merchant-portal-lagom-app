package com.squareoneinsights.merchantportallagomapp.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.restCall
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.squareoneinsights.merchantportallagomapp.api.request.{
  LogOutReq,
  MerchantLoginReq,
  MerchantRiskScoreReq
}
import com.squareoneinsights.merchantportallagomapp.api.response.{
  BusinessImpact,
  MerchantImpactDataResp,
  MerchantLoginResp,
  MerchantRiskScoreResp,
  MerchantTransaction,
  MerchantTxnSearchCriteria,
  MerchantTransactionDetails
}
import play.api.libs.json.{Format, Json}

trait MerchantportallagomappService extends Service {

  def hello(name: String): ServiceCall[NotUsed, String]

  def getRiskScore(
      merchantId: String
  ): ServiceCall[NotUsed, MerchantRiskScoreResp]

  def addRiskType: ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp]

  def getMerchantImpactData(
      merchantId: String
  ): ServiceCall[NotUsed, BusinessImpact]

  def login: ServiceCall[MerchantLoginReq, MerchantLoginResp]

  def logOut: ServiceCall[LogOutReq, Done]

  def getTransactions(
      txnType: String,
      merchantId: String
  ): ServiceCall[NotUsed, List[MerchantTransaction]]

  def getTransactionsBySearch(
      txnType: String,
      merchantId: String
  ): ServiceCall[NotUsed, List[MerchantTransaction]]

  def getTxnSearchCriteriaList: ServiceCall[NotUsed, MerchantTxnSearchCriteria]

  def getTxnDetails(
      txnType: String,
      txnId: String,
      merchantId: String
  ): ServiceCall[NotUsed, MerchantTransactionDetails]

  override final def descriptor: Descriptor = {
    import Service._
    named("merchant-portal-lagom-apps")
      .withCalls(
        restCall(Method.GET, "/api/hello/:name", hello _),
        restCall(
          Method.GET,
          "/api/v1/merchantportal/risksetting/merchant/:merchantId",
          getRiskScore _
        ),
        restCall(
          Method.POST,
          "/api/v1/merchantportal/risksetting/merchant/:merchantId",
          addRiskType
        ),
        restCall(
          Method.GET,
          "/api/v1/merchantportal/merchant/business/merchantId/:merchantId",
          getMerchantImpactData _
        ),
        restCall(Method.POST, "/api/v1/merchantportal/login", login),
        restCall(Method.POST, "/api/v1/merchantportal/logout", logOut),
        restCall(
          Method.GET,
          "/api/v1/merchantportal/txn/:txnType/:merchantId",
          getTransactions _
        ),
        restCall(
          Method.GET,
          "/api/v1/merchantportal/search/:txnType/:merchantId",
          getTransactionsBySearch _
        ),
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

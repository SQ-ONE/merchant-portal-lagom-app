  package com.squareoneinsights.merchantportallagomapp.api

  import akka.NotUsed
  import com.lightbend.lagom.scaladsl.api.transport.Method
  import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
  import com.squareoneinsights.merchantportallagomapp.api.request.{LogOutReq, MerchantLoginReq, MerchantRiskScoreReq, TransactionFilterReq}
  import com.squareoneinsights.merchantportallagomapp.api.response._

  trait MerchantportallagomappService extends Service {

  final override def descriptor: Descriptor = {
  import Service._
  named("merchant-portal-lagom-apps")
  .withCalls(
  restCall(
    Method.GET,
    "/api/hello/:name", hello _),
  restCall(
    Method.GET,
    "/api/v1/merchantportal/risksetting/merchant/:merchantId/partner/:partnerId", getRiskScore _),
  restCall(
    Method.POST,
    "/api/v1/merchantportal/risksetting/merchant/:merchantId/partner/:partnerId", addRiskType _),
  restCall(
    Method.GET,
    "/api/v1/merchantportal/merchant/business/merchantId/:merchantId/partner/:partnerId", getMerchantImpactData _),
  restCall(
    Method.POST,
    "/api/v1/merchantportal/login", login),
  restCall(
    Method.POST,
    "/api/v1/merchantportal/logout", logOut),
  restCall(
    Method.GET,
    "/api/v1/merchantportal/getPartners", getPartner),
  restCall(
    Method.POST,
    "/api/v1/merchantportal/logout", logOut),
  restCall(
    Method.GET,
    "/api/v1/merchantportal/txn/:txnType/:merchantId/:partnerId", getTransactions _),
  restCall(
    Method.POST,
    "/api/v1/merchantportal/txn/:txnType/:merchantId/:partnerId", getTransactionsBySearch _),
  restCall(
    Method.GET,
    "/api/v1/merchantportal/search/list/:partnerId", getTxnSearchCriteriaList _),
  restCall(
    Method.GET,
    "/api/v1/merchantportal/txn/:txnType/:txnId/:merchantId/:partnerId", getTxnDetails _)
  )
    .withAutoAcl(true).withExceptionSerializer(new CommonExceptionSerializer)
  }

  def hello(name: String
            ): ServiceCall[NotUsed, String]

  def getRiskScore(merchantId: String,
                   partnerId: Int
                  ): ServiceCall[NotUsed, MerchantRiskScoreResp]

  def addRiskType(merchantId: String, partnerId: Int): ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp]

  def addRiskTypeNew(merchantId: String, partnerId: Int): ServiceCall[MerchantRiskScoreReq, String]

  def getMerchantImpactData(merchantId: String,
                            partnerId: Int
                            ): ServiceCall[NotUsed, BusinessImpact]

  def login: ServiceCall[MerchantLoginReq, MerchantLoginResp]

  def logOut: ServiceCall[LogOutReq, ResponseMessage]

  def getPartner: ServiceCall[NotUsed, Seq[PartnerInfo]]

  def getTransactions(txnType: String,
                      merchantId: String,
                      partnerId: Int
                      ): ServiceCall[NotUsed, List[MerchantTransactionResp]]

  def getTransactionsBySearch(txnType: String,
                              merchantId: String,
                              partnerId: Int
                              ): ServiceCall[TransactionFilterReq, List[MerchantTransactionResp]]

  def getTxnSearchCriteriaList(partnerId: Int): ServiceCall[NotUsed, MerchantTxnSearchCriteria]

  def getTxnDetails(txnType: String,
                    txnId: String,
                    merchantId: String,
                    partnerId: Int
                    ): ServiceCall[NotUsed, MerchantTransactionDetails]
  }

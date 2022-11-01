package com.squareoneinsights.merchantportallagomapp.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.restCall
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.squareoneinsights.merchantportallagomapp.api.request.{LogOutReq, MerchantLoginReq, MerchantRiskScoreReq}
import com.squareoneinsights.merchantportallagomapp.api.response.{BusinessImpact, MerchantImpactDataResp, MerchantLoginResp, MerchantRiskScoreResp, PartnerInfo, ResponseMessage}
import play.api.libs.json.{Format, Json}

trait MerchantportallagomappService extends Service {

  def hello(name: String): ServiceCall[NotUsed, String]

  def getRiskScore(merchantId: String, partnerId: Int): ServiceCall[NotUsed, MerchantRiskScoreResp]

  def addRiskType(partnerId: Int) : ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp]

  def getMerchantImpactData(merchantId: String, partnerId: Int): ServiceCall[NotUsed, BusinessImpact]

  def login:ServiceCall[MerchantLoginReq, MerchantLoginResp]

  def logOut: ServiceCall[LogOutReq, ResponseMessage]

  def getPartner: ServiceCall[NotUsed, Seq[PartnerInfo]]

  override final def descriptor: Descriptor = {
    import Service._
    named("merchant-portal-lagom-apps")
      .withCalls(
        restCall(Method.GET,"/api/hello/:name", hello _),
        restCall(Method.GET, "/api/v1/merchantportal/risksetting/merchant/:merchantId/partner/:partnerId",  getRiskScore _),
        restCall(Method.POST, "/api/v1/merchantportal/risksetting/merchant/:merchantId/partner/:partnerId",  addRiskType _),
        restCall(Method.GET, "/api/v1/merchantportal/merchant/business/merchantId/:merchantId/partner/:partnerId",  getMerchantImpactData _),
        restCall(Method.POST, "/api/v1/merchantportal/login",  login),
        restCall(Method.POST, "/api/v1/merchantportal/logout",  logOut),
        restCall(Method.GET,  "/api/v1/merchantportal/getPartners", getPartner)
      ).withAutoAcl(true)
  }
}

package com.squareoneinsights.merchantportallagomapp.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.restCall
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.squareoneinsights.merchantportallagomapp.api.request.{MerchantLoginReq, MerchantRiskScoreReq}
import com.squareoneinsights.merchantportallagomapp.api.response.{MerchantImpactDataResp, MerchantLoginResp, MerchantRiskScoreResp}
import play.api.libs.json.{Format, Json}

trait MerchantportallagomappService extends Service {


  def hello(name: String): ServiceCall[NotUsed, String]

  def getRiskScore(merchantId: String): ServiceCall[NotUsed, MerchantRiskScoreResp]

  def addRiskType : ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp]

  def getMerchantImpactData(merchantId: String): ServiceCall[NotUsed, MerchantImpactDataResp]

  def login:ServiceCall[MerchantLoginReq, MerchantLoginResp]

  override final def descriptor: Descriptor = {
    import Service._
    named("merchant-portal-lagom-apps")
      .withCalls(
        restCall(Method.GET,"/api/hello/:name", hello _),
        restCall(Method.GET, "/api/v1/merchantportal/risksetting/merchant/:merchantId",  getRiskScore _),
        restCall(Method.POST, "/api/v1/merchantportal/risksetting/merchant",  addRiskType ),
        restCall(Method.GET, "/api/v1/merchantportal/merchant/business/merchantId/:merchantId",  getMerchantImpactData _),
        restCall(Method.POST, "/api/v1/merchantportal/login",  login)

      ).withAutoAcl(true)
  }
}

package com.squareoneinsights.merchantportallagomapp.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.restCall
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.squareoneinsights.merchantportallagomapp.api.request.MerchantRiskScoreReq
import com.squareoneinsights.merchantportallagomapp.api.response.MerchantRiskScoreResp
import play.api.libs.json.{Format, Json}

object MerchantportallagomappService  {
  val TOPIC_NAME = "greetings"
}

trait MerchantportallagomappService extends Service {


  def hello(id: String): ServiceCall[NotUsed, String]

  def getRiskScore(merchantId: String): ServiceCall[NotUsed, MerchantRiskScoreResp]

  //def addRiskType : ServiceCall[MerchantRiskScoreReq, MerchantRiskScoreResp]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("merchant-portal-lagom-app")
      .withCalls(
        restCall(Method.GET,"/api/hello/:id", hello _),
        restCall(Method.GET, "/api/v1/merchantportal/risksetting/merchant/:merchantId",  getRiskScore _)
       ).withAutoAcl(true)
  }
}

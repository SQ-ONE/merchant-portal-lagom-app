package com.squareoneinsights.merchantportallagomapp.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import com.squareoneinsights.merchantportallagomapp.api.request.MerchantRiskScoreReq
import com.wix.accord.dsl.Contextualizer
import org.scalatest.{AsyncWordSpec, Matchers}

class MerchantPortalSpec extends AsyncWordSpec with Matchers {

/*
  "The MerchantportallagomappService" should {
    "say hello" in ServiceTest.withServer(ServiceTest.defaultSetup.withCluster()) { ctx =>
      new MerchantportallagomappApplication(ctx) with LocalServiceLocator {

      }
    } { server =>
      val client = server.serviceClient.implement[MerchantportallagomappService]

      client.hello("Hello").invoke().map { response =>
        response should ===("Ok....")
      }
    }
  }
*/

  "Get Risk Score for MerchantID" should {
    "say hello" in ServiceTest.withServer(ServiceTest.defaultSetup.withCluster()) { ctx =>
      new MerchantportallagomappApplication(ctx) with LocalServiceLocator {

      }
    } { server =>
      val client = server.serviceClient.implement[MerchantportallagomappService]

      client.getRiskScore("merchant123").invoke().map { response =>
        response.merchantId should === ("merchant123")
      }
    }
  }

  "AddOrUpdate Risk Score for MerchantID" should {
    "say hello" in ServiceTest.withServer(ServiceTest.defaultSetup.withCluster()) { ctx =>
      new MerchantportallagomappApplication(ctx) with LocalServiceLocator {

      }
    } { server =>
      val client = server.serviceClient.implement[MerchantportallagomappService]
      val merchantRiskScore = MerchantRiskScoreReq.apply("merchantId11", "Medium", "High")

      client.addRiskType.invoke(merchantRiskScore).map { response =>
        response.merchantId should === ("merchantId11")
      }
    }
  }

  "Get Business Impact for MerchantID" should {
    "say hello" in ServiceTest.withServer(ServiceTest.defaultSetup.withCluster()) { ctx =>
      new MerchantportallagomappApplication(ctx) with LocalServiceLocator {

      }
    } { server =>
      val client = server.serviceClient.implement[MerchantportallagomappService]

      client.getMerchantImpactData("merchantId123").invoke().map { response =>
        response.merchantId should === ("merchant123")
      }
    }
  }
}

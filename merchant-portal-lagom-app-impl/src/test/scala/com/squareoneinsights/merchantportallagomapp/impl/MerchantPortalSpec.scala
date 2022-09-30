package com.squareoneinsights.merchantportallagomapp.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
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


}

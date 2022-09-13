package com.squareoneinsights.merchantportallagomapp.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import com.squareoneinsights.merchantportallagomapp.api._

class MerchantportallagomappServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new MerchantportallagomappApplication(ctx) with LocalServiceLocator
  }

  val client: MerchantportallagomappService = server.serviceClient.implement[MerchantportallagomappService]

  override protected def afterAll(): Unit = server.stop()

  "merchant-portal-lagom-app service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}

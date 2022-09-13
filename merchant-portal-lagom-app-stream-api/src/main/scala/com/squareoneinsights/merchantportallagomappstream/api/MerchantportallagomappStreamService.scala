package com.squareoneinsights.merchantportallagomappstream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

/**
  * The merchant-portal-lagom-app stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the MerchantportallagomappStream service.
  */
trait MerchantportallagomappStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("merchant-portal-lagom-app-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}


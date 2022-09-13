package com.squareoneinsights.merchantportallagomappstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.squareoneinsights.merchantportallagomappstream.api.MerchantportallagomappStreamService
import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService

import scala.concurrent.Future

/**
  * Implementation of the MerchantportallagomappStreamService.
  */
class MerchantportallagomappStreamServiceImpl(merchantportallagomappService: MerchantportallagomappService) extends MerchantportallagomappStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(merchantportallagomappService.hello(_).invoke()))
  }
}

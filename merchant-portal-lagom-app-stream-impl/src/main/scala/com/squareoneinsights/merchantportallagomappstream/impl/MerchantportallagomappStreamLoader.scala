package com.squareoneinsights.merchantportallagomappstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.squareoneinsights.merchantportallagomappstream.api.MerchantportallagomappStreamService
import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import com.softwaremill.macwire._

class MerchantportallagomappStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new MerchantportallagomappStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MerchantportallagomappStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[MerchantportallagomappStreamService])
}

abstract class MerchantportallagomappStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[MerchantportallagomappStreamService](wire[MerchantportallagomappStreamServiceImpl])

  // Bind the MerchantportallagomappService client
  lazy val merchantportallagomappService: MerchantportallagomappService = serviceClient.implement[MerchantportallagomappService]
}

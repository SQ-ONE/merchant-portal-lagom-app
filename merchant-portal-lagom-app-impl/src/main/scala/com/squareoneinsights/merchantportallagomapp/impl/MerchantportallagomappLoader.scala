package com.squareoneinsights.merchantportallagomapp.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._
import com.squareoneinsights.merchantportallagomapp.impl.kafka.{KafkaConsumeService, KafkaProduceService}
import com.squareoneinsights.merchantportallagomapp.impl.repository.MerchantRiskScoreDetailRepo
import play.api.db.{ConnectionPool, HikariCPComponents}
import play.api.db.evolutions.EvolutionsComponents

class MerchantportallagomappLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new MerchantportallagomappApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MerchantportallagomappApplication(context) with LagomDevModeComponents {
    }

  override def describeService = Some(readDescriptor[MerchantportallagomappService])
}

abstract class MerchantportallagomappApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with SlickPersistenceComponents
    with JdbcPersistenceComponents
    with EvolutionsComponents
    with AhcWSComponents
    with HikariCPComponents {

  override lazy val lagomServer: LagomServer = serverFor[MerchantportallagomappService](wire[MerchantportallagomappServiceImpl])
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = MerchantportallagomappSerializerRegistry
  lazy val merchantRiskScoreDetailRepo = wire[MerchantRiskScoreDetailRepo]
  lazy val KafkaProduceService = wire[KafkaProduceService]
  wire[KafkaConsumeService]
}

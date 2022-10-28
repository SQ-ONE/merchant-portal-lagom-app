package com.squareoneinsights.merchantportallagomapp.impl

import com.lightbend.lagom.scaladsl.akka.discovery.{
  AkkaDiscoveryComponents,
  AkkaDiscoveryServiceLocator
}
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.squareoneinsights.merchantportallagomapp.api.MerchantportallagomappService
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._
import com.squareoneinsights.merchantportallagomapp.impl.common.RedisUtility
import com.squareoneinsights.merchantportallagomapp.impl.kafka.{
  KafkaConsumeBusinessImpact,
  KafkaConsumeService,
  KafkaProduceService
}
import com.squareoneinsights.merchantportallagomapp.impl.repository.{
  BusinessImpactRepo,
  MerchantLoginRepo,
  MerchantOnboardRiskScore,
  MerchantRiskScoreDetailRepo,
  MerchantTransactionRepo
}
import com.typesafe.config.ConfigFactory
import play.api.db.{ConnectionPool, HikariCPComponents}
import play.api.db.evolutions.EvolutionsComponents
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class MerchantportallagomappLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new MerchantportallagomappApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MerchantportallagomappDevApplication(context)
      with LagomDevModeComponents

  override def describeService = Some(
    readDescriptor[MerchantportallagomappService]
  )
}

abstract class MerchantportallagomappDevApplication(
    context: LagomApplicationContext
) extends LagomApplication(context)
    with SlickPersistenceComponents
    with JdbcPersistenceComponents
    with EvolutionsComponents
    with AhcWSComponents
    with HikariCPComponents {
  override lazy val lagomServer: LagomServer =
    serverFor[MerchantportallagomappService](
      wire[MerchantportallagomappServiceImpl]
    )
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry =
    MerchantportallagomappSerializerRegistry
  lazy val merchantRiskScoreDetailRepo = wire[MerchantRiskScoreDetailRepo]
  lazy val kafkaProduceService = wire[KafkaProduceService]
  lazy val businessImpactRepo = wire[BusinessImpactRepo]
  lazy val merchantOnboardRiskScore = wire[MerchantOnboardRiskScore]
  lazy val merchantLoginRepo = wire[MerchantLoginRepo]
  lazy val redisUtility = wire[RedisUtility]
  lazy val merchantTransactionRepo = wire[MerchantTransactionRepo]
  wire[KafkaConsumeBusinessImpact]
}

abstract class MerchantportallagomappApplication(
    context: LagomApplicationContext
) extends LagomApplication(context)
    with SlickPersistenceComponents
    with JdbcPersistenceComponents
    with EvolutionsComponents
    with AhcWSComponents
    with HikariCPComponents {
  override lazy val lagomServer: LagomServer =
    serverFor[MerchantportallagomappService](
      wire[MerchantportallagomappServiceImpl]
    )
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry =
    MerchantportallagomappSerializerRegistry
  lazy val merchantRiskScoreDetailRepo = wire[MerchantRiskScoreDetailRepo]
  lazy val kafkaProduceService = wire[KafkaProduceService]
  lazy val businessImpactRepo = wire[BusinessImpactRepo]
  lazy val merchantOnboardRiskScore = wire[MerchantOnboardRiskScore]
  lazy val merchantLoginRepo = wire[MerchantLoginRepo]
  lazy val redisUtility = wire[RedisUtility]
  lazy val merchantTransactionRepo = wire[MerchantTransactionRepo]
  wire[KafkaConsumeBusinessImpact]
}

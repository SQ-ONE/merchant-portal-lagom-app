ThisBuild / organization := "com.squareoneinsights"

ThisBuild / version := "1.0"

 //lagomServiceLocatorPort in ThisBuild := 10000
 //lagomServiceGatewayPort in ThisBuild := 9010

// the Scala version that will be used for cross-compiled libraries
ThisBuild / scalaVersion := "2.12.4"

lagomCassandraEnabled in ThisBuild := false
// Workaround for scala-java8-compat issue affecting Lagom dev-mode
// https://github.com/lagom/lagom/issues/3344

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.2" % "provided"
val macwireAkka = "com.softwaremill.macwire" %% "macrosakka" % "2.3.2" % "provided"
val scalaCommon = "com.softwaremill.common" %% "tagging" % "2.2.1"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val accord = "com.wix" %% "accord-core" % "0.6.1"
val jwt = "com.pauldijou" %% "jwt-play-json" % "0.12.1"
val jsonExtensions = "ai.x" %% "play-json-extensions" % "0.10.0"
val joda = "joda-time" % "joda-time" % "2.9.9"
val playJsonJoda = "com.typesafe.play" %% "play-json-joda" % "2.6.0-RC1"
val playMailer = "com.typesafe.play" %% "play-mailer" % "6.0.1"
val fastParseCombinator = "com.lihaoyi" %% "fastparse" % "2.2.2"
val jbcrypt = "org.mindrot" % "jbcrypt" % "0.4"
val logbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % "6.4"
val lagomPac4j = "org.pac4j" %% "lagom-pac4j" % "2.0.0"
val pac4jHttp = "org.pac4j" % "pac4j-http" % "3.6.1"
val pac4jJwt = "org.pac4j" % "pac4j-jwt" % "3.6.1"
val nimbusJoseJwt = "com.nimbusds" % "nimbus-jose-jwt" % "6.0"
val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.12.1"
val scalaikeJdbc = "org.scalikejdbc" %% "scalikejdbc" % "3.0.0"
val scalikeJdbcConfig = "org.scalikejdbc" %% "scalikejdbc-config" % "3.0.0"
val postgresSql = "org.postgresql" % "postgresql" % "42.1.1"
val cats = "org.typelevel" %% "cats-core" % "1.4.0"
val redisClient = "com.github.etaty" %% "rediscala" % "1.8.0"
val slick = "com.typesafe.slick" %% "slick" % "3.3.3"
val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.10"
val lagomScaladslAkkaDiscovery = "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % "1.5.1"
val circeCore    = "io.circe" %% "circe-core"    % "0.14.1"
val circeGeneric = "io.circe" %% "circe-generic" % "0.14.1"
val circeParser  = "io.circe" %% "circe-parser"  % "0.14.1"
val kafkaClient =  "org.apache.kafka" % "kafka-clients" % "0.10.0.0"

//val debeziumApi = "io.debezium" % "debezium-api" % "1.9.5.Final"
//val debeziumDep = "io.debezium" % "debezium-connector-postgres" % "1.9.5.Final"
//val debeziumEmb = "io.debezium" % "debezium-embedded" % "1.9.5.Final"
val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "4.0.0"
val akkaQuartz =  "com.enragedginger" %% "akka-quartz-scheduler" % "1.9.2-akka-2.6.x"


lazy val `merchant-portal-lagom-apps` = (project in file("."))
  .aggregate(`merchant-portal-lagom-app-api`, `merchant-portal-lagom-app-impl`)

lazy val `merchant-portal-lagom-app-api` = (project in file("merchant-portal-lagom-app-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      accord,
      scalaikeJdbc,
      lagomScaladslPersistenceJdbc,
      scalikeJdbcConfig,
      redisClient
    )
  )

lazy val `merchant-portal-lagom-app-impl` = (project in file("merchant-portal-lagom-app-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceJdbc,
      lagomScaladslKafkaBroker,
      lagomScaladslApi,
      macwire,
      macwireAkka,
      scalaCommon,
      accord,
      jsonExtensions,
      playJsonJoda,
      jdbc,
      evolutions,
      postgresSql,
      cats,
      redisClient,
      circeCore,
      circeGeneric,
      circeParser,
      kafkaClient,
      guice,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi,
      jwt,
      pac4jJwt,
      nimbusJoseJwt,
      playJsonDerivedCodecs,
      lagomPac4j,
      pac4jHttp,
      akkaQuartz
   )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`merchant-portal-lagom-app-api`)

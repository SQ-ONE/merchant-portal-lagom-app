package com.squareoneinsights.merchantportallagomapp.impl.common

import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, Forbidden, RequestHeader, ResponseHeader}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.nimbusds.jwt.JWTParser
import com.squareoneinsights.merchantportallagomapp.impl.common.Pac4jAuthorizer.maxAgeInSeconds
import com.typesafe.config.ConfigFactory
import org.pac4j.core.config.Config
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.CookieClient
import org.pac4j.lagom.jwt.JwtAuthenticatorHelper
import org.pac4j.lagom.scaladsl.SecuredService

import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Pac4jAuthorizer(system: ActorSystem) extends SecuredService {

  private val config = ConfigFactory.load()

  val redis = new RedisUtility(system)

  lazy val serviceConfig: Config = {
    val config = new Config(jwtClient)
    config.getClients.setDefaultSecurityClients(jwtClient.getName)
    config
  }

  lazy val jwtClient: CookieClient = {
    val cookieClient = new CookieClient
    cookieClient.setCookieName("authToken")
    cookieClient.setAuthenticator(JwtAuthenticatorHelper.parse(config.getConfig("pac4j.lagom.jwt.authenticator")))
    cookieClient.setAuthorizationGenerator((_: WebContext, profile: CommonProfile) => {
      profile
    })
    cookieClient.setName("jwt_header")
    cookieClient
  }

  override def securityConfig: Config = serviceConfig

  def extractTokenHeader(requestHeader: RequestHeader): String = {
    val tokenO = requestHeader.getHeader("Cookie")
    val result = for {
      token <- tokenO
      authToken <- token.split("=").lift(1)
    } yield authToken
    result.getOrElse("")
  }


  def authorize[Request, Response] (serviceCall: (TokenContent, String) => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    authorizeServiceCall( serviceCall)
  }

  private def authorizeServiceCall[Request, Response](serviceCall: (TokenContent, String) => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] =
    authenticate((profile: CommonProfile) => ServerServiceCall.composeAsync { req =>
      if (profile.isExpired) {
        throw Forbidden("Session expired. Please login again.")
      }
      else {
        val userId = profile.getAttribute("merchantId").asInstanceOf[String]
        val username = profile.getId
        val tokenInRedisF = redis.getToken(userId)
        val z = tokenInRedisF.map { tokenInRedis =>
          val tokenInRequest = extractTokenHeader(req)
          tokenInRedis.filter(_ == tokenInRequest)
            .fold {
              throw Forbidden("Session expired. Please login again..")
            } { l =>
              serviceCall(TokenContent(userId, username), tokenInRequest)
            }
        }
        z
      }
    }
    )

  def getToken(userId: String) = {
    redis.getToken(userId)
  }

  def getUserDetailFromToken(token: String): (Date, String, Boolean) = {
    val tokenParser = JWTParser.parse(token)
    val expTimeFinal = tokenParser.getJWTClaimsSet.getExpirationTime
    val  convertLocaltDT: LocalDateTime = Instant.ofEpochMilli(expTimeFinal.getTime)
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime
    val userId = tokenParser.getJWTClaimsSet.getStringClaim("merchantId")
    (expTimeFinal, userId, convertLocaltDT.isAfter(LocalDateTime.now()))
  }

  private def addHeader[A](p: Future[A], token: String): Future[(ResponseHeader, A)] = {
    val responseHeader = ResponseHeader.Ok
      .withHeader("Set-Cookie", s"authToken=$token; Max-Age=$maxAgeInSeconds")
    p
      .map(result => (responseHeader, result))
  }
}

object Pac4jAuthorizer {
  val config = ConfigFactory.load()
  val maxAgeInSeconds = config.getInt("ifrm.cookie.max-age.seconds")
}

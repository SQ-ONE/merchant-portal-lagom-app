package com.squareoneinsights.merchantportallagomapp.impl.common

import cats.syntax.either._
import com.nimbusds.jose.JWSAlgorithm.HS256
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import play.api.libs.json.{Format, Json}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtJson}
import java.util.{Date, UUID}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object JwtTokenGenerator {

  val config = ConfigFactory.load()
  val secret = config.getString("jwt.encryption.key")

  def createToken(content: TokenContent, expiryDate: Date)(implicit
      format: Format[TokenContent],
      ec: ExecutionContext
  ): Future[Either[String, Token]] = Future {
    Either.fromTry(Try(generate(content, expiryDate))).leftMap {
      case ex: Throwable => ex.getMessage
    }
  }

  def generate(content: TokenContent, expiryDate: Date)(implicit
      format: Format[TokenContent]
  ) = {

    val user = new JWTClaimsSet.Builder()
      .issuer("https://pac4j.org")
      .subject(content.merchantId)
      .claim("merchantId", content.merchantId)
      .issueTime(new Date)
      .expirationTime(expiryDate)
      .jwtID(UUID.randomUUID.toString)
      .build

    val refreshClaim = JwtClaim(user.toString())
      .expiresIn(100L)
      .issuedNow
    val refreshToken = JwtJson.encode(refreshClaim, secret, JwtAlgorithm.HS256)

    Token(refreshToken)
  }

}

case class TokenContent(merchantId: String, merchantName: String)

object TokenContent {
  implicit val format: Format[TokenContent] = Json.format
}

case class Token(authToken: String)

object Token {
  implicit val format: Format[Token] = Json.format
}

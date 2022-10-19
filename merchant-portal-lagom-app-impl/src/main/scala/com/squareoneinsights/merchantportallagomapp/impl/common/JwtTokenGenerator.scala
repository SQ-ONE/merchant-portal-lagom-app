package com.squareoneinsights.merchantportallagomapp.impl.common

import cats.syntax.either._
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jwt.{JWTClaimsSet, JWTParser, SignedJWT}
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import play.api.libs.json.{Format, Json}
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import com.nimbusds.jose.JWSAlgorithm.HS256
import java.util.{Date, UUID}
import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits._

object JwtTokenGenerator {
  private val config = ConfigFactory.load()
  private val list = config.getConfigList("pac4j.lagom.jwt.authenticator.signatures")
  private val s = list.get(0).getObject("jwk").render(ConfigRenderOptions.concise())
  private val secret = config.getString("jwt.encryption.key")

  val refreshTokenExp: Int = config.getInt("jwt.token.refresh.expirationInSeconds")


  def createToken(content: TokenContent, date:Date)(implicit format: Format[TokenContent]):
  Future[Either[MerchantPortalError, Token]] = Future {
    Either.fromTry(Try(generate(content, date))).leftMap {
      case ex: Throwable =>  CreateLogInTokenErr("Failed to create login token")
    }
  }


  def generate(content: TokenContent, expiryDate: Date)(implicit format: Format[TokenContent]) = {

    val user = new JWTClaimsSet.Builder()
      .issuer("https://pac4j.org")
      .subject(content.merchantId)
      .claim("merchantId", content.merchantId)
      .claim("merchantName", content.merchantName)
      .issueTime(new Date)
      .expirationTime(expiryDate)
      .jwtID(UUID.randomUUID.toString)
      .build

    val octetSequenceKey: OctetSequenceKey = OctetSequenceKey.parse(s)
    val jwsHeader = new JWSHeader(HS256)
    val signedJWT = new SignedJWT(jwsHeader, user)
    signedJWT.sign(new MACSigner(octetSequenceKey))
    Token(signedJWT.serialize())
  }



    def generateRefreshToken(content: TokenContent, expiryDate: Date)(implicit format: Format[TokenContent]) =
      Future {
        Either.fromTry(Try({
          val userRefresh = new JWTClaimsSet.Builder()
            .issuer("https://squareoneinsights.com")
            .subject(content.merchantId)
            .claim("merchantId", content.merchantId)
            .claim("merchantName", content.merchantName)
            .claim("userName", content.merchantId)
            .claim("isRefreshToken", true)
            .jwtID(UUID.randomUUID.toString)
            .build

          val refreshClaim = JwtClaim(userRefresh.toString())
            .expiresIn(refreshTokenExp)
            .issuedNow

          val refreshToken = JwtJson.encode(refreshClaim, secret, JwtAlgorithm.HS256)

          Token(
            authToken = "",
            refreshToken = Some(refreshToken)
          )
        })).leftMap {
          case ex: Throwable => CreateLogInTokenErr("Failed to create login token")
        }
      }

}

case class TokenContent(merchantId:String,merchantName:String, isRefreshToken: Boolean = false)

object TokenContent {

  implicit val format: Format[TokenContent] = Json.format

}

case class Token(authToken: String, refreshToken: Option[String] = None)

object Token {
  implicit val format: Format[Token] = Json.format
}



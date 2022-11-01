package com.squareoneinsights.merchantportallagomapp.impl.common

import cats.syntax.either._
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import play.api.libs.json.{Format, Json}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtJson}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object JwtTokenGenerator {

  val config = ConfigFactory.load()
  val secret = config.getString("jwt.encryption.key")
  val authExpiration = ConfigFactory.load().getInt("jwt.token.auth.expirationInSeconds")
  val refreshExpiration = ConfigFactory.load().getInt("jwt.token.refresh.expirationInSeconds")
  val algorithm = JwtAlgorithm.HS256


  def createToken(content: TokenContent)(implicit format: Format[TokenContent],
                                                           ec: ExecutionContext):
  Future[Either[MerchantPortalError, Token]] = Future {
    Either.fromTry(Try(generateTokens(content))).leftMap {
      case ex: Throwable => CreateLogInTokenErr("Failed to create login token")
    }
  }

  def generateTokens(content: TokenContent)(implicit format: Format[TokenContent]): Token = {
    val authClaim = JwtClaim(Json.toJson(content).toString())
      .expiresIn(authExpiration)
      .issuedNow

    val refreshClaim = JwtClaim(Json.toJson(content.copy(isRefreshToken = true)).toString())
      .expiresIn(refreshExpiration)
      .issuedNow

    val authToken = JwtJson.encode(authClaim, secret, algorithm)
    val refreshToken = JwtJson.encode(refreshClaim, secret, algorithm)

    Token(
      authToken = authToken,
      refreshToken = refreshToken
    )
  }
}

case class TokenContent(merchantId:String, merchantName:String, isRefreshToken: Boolean = false)

object TokenContent {

  implicit val format: Format[TokenContent] = Json.format

}

case class Token(authToken: String, refreshToken:String = "")

object Token {
  implicit val format: Format[Token] = Json.format
}

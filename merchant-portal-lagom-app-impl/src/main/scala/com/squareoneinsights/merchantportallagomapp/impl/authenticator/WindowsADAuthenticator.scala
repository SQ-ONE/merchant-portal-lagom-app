package com.squareoneinsights.merchantportallagomapp.impl.authenticator

import cats.syntax.either._
// import com.squareoneinsights.ifrm.useraccessmanagement.impl.models.internal.UserByUserName
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.spec.PKCS8EncodedKeySpec
import java.util
import javax.naming.Context
import javax.naming.directory.InitialDirContext
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import java.security.KeyFactory
import javax.crypto.Cipher

class WindowsADAuthenticator {

}

object WindowsADAuthenticator {

  val log = LoggerFactory.getLogger(classOf[WindowsADAuthenticator])
  val WindowsADUrl = ConfigFactory.load().getString("windows.ad.url")
  val aDUserNameWithDomainName = ConfigFactory.load().getString("windows.ad.domain.name")
  val aDDomainName = ConfigFactory.load().getString("windows.ad.domain.name")
  val keystore = ConfigFactory.load().getString("windows.ad.keystore.path")
  val keystorePassword = ConfigFactory.load().getString("windows.ad.keystore.password")
  val bypass =
    if (ConfigFactory.load().hasPath("ifrm.windows.ad.authentication.bypass"))
      ConfigFactory.load().getBoolean("ifrm.windows.ad.authentication.bypass")
    else {
      log.info("No config -- ifrm.windows.ad.authentication.bypass -- has been provided. Proceeding with Windows AD Authentication.")
      false
    }
  val privateKey =  ConfigFactory.load().getString("ifrm.password.decrypt.pkcs8.private.key")

  def authenticateUser(user: String, password: String)(implicit ec: ExecutionContext):
  Future[Either[String, String]] = Future {
    if (bypass)
      user.asRight[String]
    else
      improvedADAuthenticator(user, password).bimap(identity(_), _ => user)
  }

  def improvedADAuthenticator(username: String, password: String): Either[String, InitialDirContext] = {
    val ssl = "true"
    val finalUserName = username.concat(aDDomainName)
    val finalPassword = decryptUsingPrivateKey(password)
    val environment = new util.Hashtable[String, String]
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    environment.put(Context.PROVIDER_URL, WindowsADUrl)
    environment.put(Context.SECURITY_AUTHENTICATION, "simple")
    environment.put(Context.SECURITY_PRINCIPAL, finalUserName)
    environment.put(Context.SECURITY_CREDENTIALS, finalPassword)

    if (ssl.equalsIgnoreCase("true")) {
      System.setProperty("javax.net.ssl.trustStore", keystore)
      System.setProperty("javax.net.ssl.trustStorePassword", keystorePassword)
      System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true")
      environment.put(Context.SECURITY_PROTOCOL, "ssl")
    }
    Either.fromTry(Try(new InitialDirContext(environment))).bimap(err => "Invalid user name and password combination or logging in out of shift.", identity(_))
  }

  def decryptUsingPrivateKey(encodedPassword: String) = {
    import play.shaded.oauth.org.apache.commons.codec.binary.Base64

    val encoded = Base64.decodeBase64(privateKey.getBytes())
    val keyFactory = KeyFactory.getInstance("RSA")
    val pkcs8EncodedKey = new PKCS8EncodedKeySpec(encoded)
    val key = keyFactory.generatePrivate(pkcs8EncodedKey)
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, key)

    val decryptedTextArray = cipher.doFinal(Base64.decodeBase64(encodedPassword.getBytes()))
    val decryptedText = new String(decryptedTextArray, StandardCharsets.US_ASCII)
    log.info(s"encodedPassword = ${encodedPassword}")
    log.info(s"decodedPassword = ${decryptedText}")
    decryptedText
  }

}

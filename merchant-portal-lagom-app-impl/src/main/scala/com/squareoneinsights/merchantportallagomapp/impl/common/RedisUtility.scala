package com.squareoneinsights.merchantportallagomapp.impl.common

import akka.Done
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import redis.RedisClient

import scala.concurrent.Future
import scala.util.Try
import cats.syntax.either._

import scala.concurrent.ExecutionContext.Implicits.global

class RedisUtility(system:ActorSystem) {

implicit val s = system
  val config = ConfigFactory.load()
  val redisHost = config.getString("ifrm.redis.host")
  val redisAuth = config.getString("ifrm.redis.password")

  val redis: RedisClient = RedisClient(redisHost, password = Some(redisAuth))

  def addToken(key: String, value: String) =
    redis.set(key, value)

  def addTokenToRedis(userName: String, authToken: String):
  Future[Either[String, Done]] = Future {
    Either.fromTry(Try(addToken(userName, authToken))).leftMap {
      case ex => ex.getMessage
    }.map(_ => Done)
  }

  def deleteTokenFromRedis(userName: String) = Future {
    Either.fromTry(Try(redis.del(userName))).leftMap {
      case ex => ex.getMessage
    }.map(_ => Done)
  }

}

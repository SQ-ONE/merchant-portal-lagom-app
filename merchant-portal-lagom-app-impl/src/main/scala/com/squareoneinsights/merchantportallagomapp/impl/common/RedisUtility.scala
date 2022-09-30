package com.squareoneinsights.merchantportallagomapp.impl.common

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import redis.RedisClient

class RedisUtility(system:ActorSystem) {

  implicit val s = system
  val config = ConfigFactory.load()
  val redisHost = config.getString("ifrm.redis.host")
  val redisAuth = config.getString("ifrm.redis.password")

  val redis: RedisClient = RedisClient(redisHost, password = Some(redisAuth))

  def addToken(key: String, value: String) =
    redis.set(key, value)

  def deleteToken(key: String) =
    redis.del(key)
}

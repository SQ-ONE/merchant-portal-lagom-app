package com.squareoneinsights.merchantportallagomapp.impl.common

import java.util

import org.apache.kafka.common.serialization.{Serializer, StringSerializer}
import play.api.libs.json._

class JsonSerializer[A: Writes] extends Serializer[A] {

  private val stringSerializer = new StringSerializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit =
    stringSerializer.configure(configs, isKey)

  override def serialize(topic: String, data: A): Array[Byte] =
    stringSerializer.serialize(topic, Json.stringify(Json.toJson(data)))

  override def close(): Unit =
    stringSerializer.close()

}

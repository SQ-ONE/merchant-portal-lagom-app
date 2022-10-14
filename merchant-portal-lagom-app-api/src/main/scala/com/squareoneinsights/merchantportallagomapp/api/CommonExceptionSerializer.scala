package com.squareoneinsights.merchantportallagomapp.api

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.{ExceptionSerializer, RawExceptionMessage}
import com.lightbend.lagom.scaladsl.api.transport._
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.collection.immutable
import scala.util.control.NonFatal

class CommonExceptionSerializer extends ExceptionSerializer {

  val log = LoggerFactory.getLogger(classOf[CommonExceptionSerializer])

  override def serialize(exception: Throwable, accept: immutable.Seq[MessageProtocol]): RawExceptionMessage = {
    val (errorCode, msg) = exception match {
      case dse: DeserializationException => (TransportErrorCode.BadRequest, new ExceptionMessage("Invalid request.", "Contact admin for further details."))
      case bre: BadRequest => (TransportErrorCode.BadRequest, new ExceptionMessage(s"${exception.getMessage}", s"Contact admin for further details."))
      case allExceptions => (TransportErrorCode.InternalServerError, new ExceptionMessage(s"${exception.getMessage}", "Contact admin for further details."))
    }

    val messageBytes = ByteString.fromString(Json.stringify(Json.obj("name" -> msg.name, "detail" -> msg.detail)))
    RawExceptionMessage(errorCode, MessageProtocol(Some("application/json"), None, None), messageBytes)
  }

  override def deserialize(message: RawExceptionMessage): Throwable = {
    val messageJson = try {
      Json.parse(message.message.iterator.asInputStream)
    } catch {
      case NonFatal(e) => Json.obj()
    }

    val jsExceptionMessage = for {
      name <- (messageJson \ "name").validate[String]
      detail <- (messageJson \ "detail").validate[String]
    } yield new ExceptionMessage(name, detail)

    val exceptionMessage = jsExceptionMessage match {
      case JsSuccess(message, _) => message
      case JsError(_) => new ExceptionMessage("UndeserializableException", message.message.utf8String)
    }

    TransportException.fromCodeAndMessage(message.errorCode, exceptionMessage)
  }

}

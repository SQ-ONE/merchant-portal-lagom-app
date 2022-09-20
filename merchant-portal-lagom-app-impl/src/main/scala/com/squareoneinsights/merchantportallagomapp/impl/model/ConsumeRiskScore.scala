package com.squareoneinsights.merchantportallagomapp.impl.model

import play.api.libs.json.{Format, Json}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

case class ConsumeRiskScore(merchantId: String, isApproved: String)

object ConsumeRiskScore {

  implicit val format: Format[ConsumeRiskScore] = Json.format

  def decoder(jsonStr: String): ConsumeRiskScore = {
    decode[ConsumeRiskScore](jsonStr) match {
      case Right(riskScore) => {
        println("riskScore=>" + riskScore)
        riskScore
      }
      case Left(e) => {
        println("riskScore error=>" + e)
        throw new Exception(e)
      }
    }
  }
}

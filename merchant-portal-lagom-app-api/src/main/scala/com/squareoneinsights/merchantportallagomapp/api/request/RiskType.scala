package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

object RiskType extends Enumeration {
  val Low = Value("Low")
  val Medium = Value("Medium")
  val High = Value("High")

  def withNameOpt(stringValue: String) = values.find(_.toString.equalsIgnoreCase(stringValue))

}

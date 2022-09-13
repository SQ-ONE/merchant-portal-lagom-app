package com.squareoneinsights.merchantportallagomapp.impl.model

import play.api.libs.json.{Format, Json}

case class PaymentTypeDetail(paymentBlock: Int,
                             paymentInReview: Int,
                             paymentAllow: Int)

object PaymentTypeDetail {

  implicit val format: Format[PaymentTypeDetail] = Json.format
}

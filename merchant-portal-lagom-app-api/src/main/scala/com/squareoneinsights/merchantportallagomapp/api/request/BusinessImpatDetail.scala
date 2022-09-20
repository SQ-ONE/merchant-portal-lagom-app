package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

case class BusinessImpactDetail(
    partnerId: Int,
    merchantId: String,
    lowPaymentAllowed: Int,
    lowPaymentReview: Int,
    lowPaymentBlocked: Int,
    medPaymentAllowed: Int,
    medPaymentReview: Int,
    medPaymentBlocked: Int,
    highPaymentAllowed: Int,
    highPaymentReview: Int,
    highPaymentBlocked: Int,
    updatedTimeStamp: LocalDateTime
)

object BusinessImpactDetail {

  implicit val format: Format[BusinessImpactDetail] = Json.format
}

package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

case class BusinessImpactDetail(partnerId: Int,
                                merchantId: String,
                                lowPaymentAllowed: Double,
                                lowPaymentReview: Double,
                                lowPaymentBlocked: Double,
                                medPaymentAllowed: Double,
                                medPaymentReview: Double,
                                medPaymentBlocked: Double,
                                highPaymentAllowed: Double,
                                highPaymentReview: Double,
                                highPaymentBlocked: Double,
                                updatedTimeStamp: LocalDateTime)

object BusinessImpactDetail {

  implicit val format: Format[BusinessImpactDetail] = Json.format
}


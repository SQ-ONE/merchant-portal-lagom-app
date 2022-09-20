package com.squareoneinsights.merchantportallagomapp.api.response

import com.squareoneinsights.merchantportallagomapp.api.request.BusinessImpactDetail
import play.api.libs.json.{Format, Json}

case class MerchantImpactDataResp(
    merchantId: String,
    low: PaymentType,
    medium: PaymentType,
    high: PaymentType
)

object MerchantImpactDataResp {

  implicit val format: Format[MerchantImpactDataResp] = Json.format

  def setMerchantBusinessData(consumedBusinessImpact: BusinessImpactDetail) = {
    import consumedBusinessImpact._
    MerchantImpactDataResp(
      merchantId,
      PaymentType(lowPaymentAllowed, lowPaymentReview, lowPaymentBlocked),
      PaymentType(medPaymentAllowed, medPaymentReview, medPaymentBlocked),
      PaymentType(highPaymentAllowed, highPaymentReview, highPaymentBlocked)
    )

  }
}

case class PaymentType(
    paymentAllow: Int,
    paymentInReview: Int,
    paymentBlock: Int
)

object PaymentType {

  implicit val format: Format[PaymentType] = Json.format
}

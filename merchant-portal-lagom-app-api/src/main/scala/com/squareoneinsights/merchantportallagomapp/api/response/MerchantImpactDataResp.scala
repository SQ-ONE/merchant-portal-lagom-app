package com.squareoneinsights.merchantportallagomapp.api.response

import com.squareoneinsights.merchantportallagomapp.api.request.BusinessImpactDetail
import play.api.libs.json.{Format, Json}

case class MerchantImpactDataResp(merchantId: String, low: PaymentType, medium: PaymentType, high: PaymentType)

object MerchantImpactDataResp {

  implicit val format: Format[MerchantImpactDataResp] = Json.format

  def setMerchantBusinessData(consumedBusinessImpact: BusinessImpactDetail) = {
    import consumedBusinessImpact._
    new MerchantImpactDataResp(merchantId, PaymentType(lowPaymentAllowed, lowPaymentReview, lowPaymentBlocked),
      PaymentType(medPaymentAllowed, medPaymentReview, medPaymentBlocked), PaymentType(highPaymentAllowed, highPaymentReview, highPaymentBlocked))
  }
}

case class PaymentType(paymentAllow: Double, paymentInReview: Double, paymentBlock: Double)

object PaymentType {

  implicit val format: Format[PaymentType] = Json.format
}

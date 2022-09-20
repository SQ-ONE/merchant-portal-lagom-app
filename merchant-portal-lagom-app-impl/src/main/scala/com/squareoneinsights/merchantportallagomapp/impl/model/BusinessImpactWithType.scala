package com.squareoneinsights.merchantportallagomapp.impl.model

case class BusinessImpactWithType(
    merchantId: String,
    paymentTypeDetail: PaymentTypeDetail,
    paymentType: String
)

package com.squareoneinsights.merchantportallagomapp.impl.model

import java.time.LocalDateTime

case class MerchantDetails(
    merchantId: Int,
    userName: String,
    merchantName: String,
    mccCode: Int,
    isLoginActive: String,
    updateTimestamp: LocalDateTime
)

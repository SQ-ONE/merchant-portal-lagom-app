package com.squareoneinsights.merchantportallagomapp.impl.model

import java.time.LocalDateTime

case class MerchantLoginActivity(
    activityId: Int,
    merchantId: String,
    loginTime: LocalDateTime,
    logoutTime: LocalDateTime
)

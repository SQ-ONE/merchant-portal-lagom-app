package com.squareoneinsights.merchantportallagomapp.impl.model

import java.time.LocalDateTime

case class MerchantRiskScore(requestId: Int,
                             partnerId: Int,
                             merchantId: String,
                             oldSliderPosition: String,
                             updatedSliderPosition: String,
                             approvalFlag: String,
                             isInsert: Int)

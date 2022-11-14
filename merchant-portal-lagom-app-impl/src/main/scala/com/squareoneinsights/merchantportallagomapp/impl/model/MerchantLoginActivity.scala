package com.squareoneinsights.merchantportallagomapp.impl.model

import java.sql.Timestamp


case class MerchantLoginActivity(activityId:Option[Int],merchantId:String, partnerId: Int, loginTime:Option[Timestamp], logOutTime:Option[Timestamp])

case class Merchant(merchantId:String, merchantMcc:String)

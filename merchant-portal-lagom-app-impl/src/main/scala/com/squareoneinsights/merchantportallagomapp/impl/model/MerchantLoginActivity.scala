package com.squareoneinsights.merchantportallagomapp.impl.model

import java.sql.Date


case class MerchantLoginActivity(activityId:Option[Int],merchantId:String, loginTime:Option[Date], logOutTime:Option[Date])

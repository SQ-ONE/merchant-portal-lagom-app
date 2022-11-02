package com.squareoneinsights.merchantportallagomapp.impl.model

case class MerchantLogin(id:Int, merchantId:String,  partnerId: Int, merchantName:String, isMerchantActive:Int, isLoggedInFlag:Int)

case class MerchantLoginDetails(id:Int, merchantId:String, merchantName:String, partnerId: Int, isLoggedInFlag:Int )

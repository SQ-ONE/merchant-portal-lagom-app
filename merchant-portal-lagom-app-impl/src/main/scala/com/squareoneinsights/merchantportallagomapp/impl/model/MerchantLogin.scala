package com.squareoneinsights.merchantportallagomapp.impl.model

case class MerchantLogin(id:Int,merchantId:String, merchantName:String,isLoggedInFlag:Boolean )

case class MerchantLoginDetails(id:Int,merchantId:String, merchantName:String,merchantMcc:String,isLoggedInFlag:Boolean )

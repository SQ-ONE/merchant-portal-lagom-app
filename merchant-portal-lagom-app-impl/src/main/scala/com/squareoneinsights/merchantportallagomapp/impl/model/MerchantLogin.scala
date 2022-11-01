package com.squareoneinsights.merchantportallagomapp.impl.model

case class MerchantLogin(id:Int,merchantId:String,partnerId:Int, merchantName:String,isLoggedInFlag:Boolean )

case class MerchantLoginDetails(id:Int,merchantId:String,partnerId:Int, merchantName:String,merchantMcc:String,isLoggedInFlag:Boolean )

package com.squareoneinsights.merchantportallagomapp.api.request

import play.api.libs.json.{Format, Json}

case class TransactionFilterReq(filterCondition:List[TransactionFilter])

case class TransactionFilter(key:String,condition:String,value:String)

object TransactionFilterReq {

  implicit val transactionFilterFormat: Format[TransactionFilter] = Json.format
  implicit val transactionFilterReqFormat: Format[TransactionFilterReq] = Json.format
}
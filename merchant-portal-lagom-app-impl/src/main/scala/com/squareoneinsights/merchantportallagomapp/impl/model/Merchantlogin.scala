package com.squareoneinsights.merchantportallagomapp.impl.model

case class Merchantlogin(
    id: Int,
    merchantId: String,
    merchantName: String,
    merchantContact: String,
    merchantEmail: String,
    isMerchantActive: Boolean = false,
    password: String,
    salt: String,
    provisional1: String,
    provisional2: String
)

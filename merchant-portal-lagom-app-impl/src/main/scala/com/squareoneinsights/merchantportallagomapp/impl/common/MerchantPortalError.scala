package com.squareoneinsights.merchantportallagomapp.impl.common

trait MerchantPortalError

case class AddMerchantErr(err: String) extends MerchantPortalError
case class RiskSettingProducerErr()  extends MerchantPortalError
case class GetMerchantOnboard(err: String) extends MerchantPortalError
case class GetMerchantErr(err: String) extends MerchantPortalError
case class CheckRiskScoreExist(err: String) extends MerchantPortalError
case class UpdatedRiskErr(err: String) extends MerchantPortalError
case class GetBusinessImpactErr(err: String) extends MerchantPortalError
case class AddBusinessImpactErr(err: String) extends MerchantPortalError
case class UpdateBusinessImpactErr(err: String) extends MerchantPortalError
case class BusinessImpactConsumerErr(err: String) extends MerchantPortalError
case class LoginErr(err: String) extends MerchantPortalError
case class LogoutErr(err: String) extends MerchantPortalError
case class LogoutRedisErr(err: String) extends MerchantPortalError

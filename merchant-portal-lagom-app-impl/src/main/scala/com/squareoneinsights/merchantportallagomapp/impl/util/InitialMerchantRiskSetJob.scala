package com.squareoneinsights.merchantportallagomapp.impl.util

import akka.actor.{ActorRef, ActorSystem}
import com.softwaremill.tagging.@@
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import org.slf4j.LoggerFactory

import java.util.TimeZone
import scala.concurrent.ExecutionContextExecutor

class InitialMerchantRiskSetJob(updateRiskToIfrmActor: ActorRef @@ UpdateRiskToIfrmActor) {
  val log = LoggerFactory.getLogger(classOf[InitialMerchantRiskSetJob])
  val system = ActorSystem("SchedulerSystem")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val scheduler = QuartzSchedulerExtension



  log.info("Inside ScheduleMerchantJob Started...................")
  QuartzSchedulerExtension.get(system).createSchedule("dailyScheduler", None,
    "0 */3 * ? * *", None, TimeZone.getTimeZone("Asia/Calcutta"))

  QuartzSchedulerExtension.get(system).schedule("dailyScheduler", updateRiskToIfrmActor,
    "Update")
}

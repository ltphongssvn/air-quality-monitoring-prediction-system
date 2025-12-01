// air-quality-monitoring-prediction-system/backend/app/actors/SensorMonitorActor.scala
package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import models.AQIReading

object SensorMonitorActor {
  
  sealed trait Command
  case class ProcessReading(reading: AQIReading) extends Command
  case class GetLatestReading(replyTo: ActorRef[Option[AQIReading]]) extends Command
  case class CheckThreshold(threshold: Int, replyTo: ActorRef[Boolean]) extends Command
  
  def apply(): Behavior[Command] = monitor(None)
  
  private def monitor(latestReading: Option[AQIReading]): Behavior[Command] = 
    Behaviors.receive { (context, message) =>
      message match {
        case ProcessReading(reading) =>
          context.log.info(s"Processing AQI reading: ${reading.aqi} from ${reading.sensorId}")
          monitor(Some(reading))
          
        case GetLatestReading(replyTo) =>
          replyTo ! latestReading
          Behaviors.same
          
        case CheckThreshold(threshold, replyTo) =>
          val exceeds = latestReading.exists(_.aqi > threshold)
          replyTo ! exceeds
          Behaviors.same
      }
    }
}

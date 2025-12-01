// air-quality-monitoring-prediction-system/backend/app/controllers/AQIController.scala
package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json.Json
import models.{AQIReading, Location}
import java.time.Instant
import scala.concurrent.ExecutionContext

@Singleton
class AQIController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController {

  // Placeholder data - will be replaced with repository
  private val sampleData = Seq(
    AQIReading(
      id = Some("1"),
      sensorId = "sensor-001",
      location = Location(34.0522, -118.2437, Some("Los Angeles"), Some("USA")),
      aqi = 42,
      pm25 = 10.5,
      pm10 = 22.3,
      o3 = 0.035,
      no2 = 0.015,
      co = 0.4,
      timestamp = Instant.now()
    )
  )

  def getAll(): Action[AnyContent] = Action {
    Ok(Json.toJson(sampleData))
  }

  def getById(id: String): Action[AnyContent] = Action {
    sampleData.find(_.id.contains(id)) match {
      case Some(reading) => Ok(Json.toJson(reading))
      case None => NotFound(Json.obj("error" -> s"AQI reading $id not found"))
    }
  }

  def create(): Action[AnyContent] = Action { request =>
    request.body.asJson.flatMap(_.asOpt[AQIReading]) match {
      case Some(reading) => Created(Json.toJson(reading))
      case None => BadRequest(Json.obj("error" -> "Invalid AQI reading data"))
    }
  }
}

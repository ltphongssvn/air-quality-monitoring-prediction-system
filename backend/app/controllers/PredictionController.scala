// air-quality-monitoring-prediction-system/backend/app/controllers/PredictionController.scala
package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import java.time.Instant
import scala.concurrent.ExecutionContext

case class PollutionPrediction(
  location: String,
  predictedAqi: Int,
  confidence: Double,
  predictedAt: Instant,
  validUntil: Instant
)

object PollutionPrediction {
  implicit val instantFormat: Format[Instant] = new Format[Instant] {
    def reads(json: JsValue): JsResult[Instant] = 
      json.validate[String].map(Instant.parse)
    def writes(instant: Instant): JsValue = 
      JsString(instant.toString)
  }
  implicit val format: OFormat[PollutionPrediction] = Json.format[PollutionPrediction]
}

@Singleton
class PredictionController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController {

  private val samplePredictions = Seq(
    PollutionPrediction(
      location = "Los Angeles",
      predictedAqi = 48,
      confidence = 0.85,
      predictedAt = Instant.now(),
      validUntil = Instant.now().plusSeconds(3600)
    )
  )

  def getAll(): Action[AnyContent] = Action {
    Ok(Json.toJson(samplePredictions))
  }

  def getByLocation(location: String): Action[AnyContent] = Action {
    samplePredictions.find(_.location.equalsIgnoreCase(location)) match {
      case Some(pred) => Ok(Json.toJson(pred))
      case None => NotFound(Json.obj("error" -> s"No prediction for $location"))
    }
  }
}

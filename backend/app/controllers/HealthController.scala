// air-quality-monitoring-prediction-system/backend/app/controllers/HealthController.scala
package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json.Json

@Singleton
class HealthController @Inject()(val controllerComponents: ControllerComponents) 
    extends BaseController {

  def check(): Action[AnyContent] = Action { implicit request =>
    Ok(Json.obj(
      "status" -> "healthy",
      "service" -> "air-quality-backend",
      "version" -> "0.1.0"
    ))
  }
}

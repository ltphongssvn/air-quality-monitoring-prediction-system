// air-quality-monitoring-prediction-system/backend/app/services/OpenWeatherMapService.scala
package services

import javax.inject._
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

case class WeatherData(
  temperature: Double,
  humidity: Int,
  windSpeed: Double,
  windDirection: Int,
  pressure: Double,
  description: String
)

object WeatherData {
  implicit val format: OFormat[WeatherData] = Json.format[WeatherData]
}

@Singleton
class OpenWeatherMapService @Inject()(
  ws: WSClient
)(implicit ec: ExecutionContext) {

  private val apiKey = sys.env.getOrElse("OPENWEATHERMAP_API_KEY", "")
  private val baseUrl = "https://api.openweathermap.org/data/2.5"

  def getCurrentWeather(lat: Double, lon: Double): Future[Option[WeatherData]] = {
    ws.url(s"$baseUrl/weather")
      .addQueryStringParameters(
        "lat" -> lat.toString,
        "lon" -> lon.toString,
        "appid" -> apiKey,
        "units" -> "metric"
      )
      .get()
      .map { response =>
        if (response.status == 200) {
          val json = response.json
          Some(WeatherData(
            temperature = (json \ "main" \ "temp").as[Double],
            humidity = (json \ "main" \ "humidity").as[Int],
            windSpeed = (json \ "wind" \ "speed").as[Double],
            windDirection = (json \ "wind" \ "deg").asOpt[Int].getOrElse(0),
            pressure = (json \ "main" \ "pressure").as[Double],
            description = (json \ "weather" \ 0 \ "description").as[String]
          ))
        } else None
      }
      .recover { case _ => None }
  }

  def getAirPollution(lat: Double, lon: Double): Future[Option[JsValue]] = {
    ws.url(s"$baseUrl/air_pollution")
      .addQueryStringParameters(
        "lat" -> lat.toString,
        "lon" -> lon.toString,
        "appid" -> apiKey
      )
      .get()
      .map { response =>
        if (response.status == 200) Some(response.json)
        else None
      }
      .recover { case _ => None }
  }
}

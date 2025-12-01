// air-quality-monitoring-prediction-system/backend/app/services/EPAAirNowService.scala
package services

import javax.inject._
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

case class AirNowObservation(
  dateObserved: String,
  hourObserved: Int,
  localTimeZone: String,
  reportingArea: String,
  stateCode: String,
  latitude: Double,
  longitude: Double,
  parameterName: String,
  aqi: Int,
  category: AirNowCategory
)

case class AirNowCategory(number: Int, name: String)

object AirNowCategory {
  implicit val format: OFormat[AirNowCategory] = Json.format[AirNowCategory]
}

object AirNowObservation {
  implicit val format: OFormat[AirNowObservation] = Json.format[AirNowObservation]
}

@Singleton
class EPAAirNowService @Inject()(
  ws: WSClient
)(implicit ec: ExecutionContext) {

  private val apiKey = sys.env.getOrElse("EPA_AIRNOW_API_KEY", "")
  private val baseUrl = "https://www.airnowapi.org"

  def getCurrentByZip(zipCode: String): Future[Seq[AirNowObservation]] = {
    ws.url(s"$baseUrl/aq/observation/zipCode/current/")
      .addQueryStringParameters(
        "format" -> "application/json",
        "zipCode" -> zipCode,
        "API_KEY" -> apiKey
      )
      .get()
      .map(parseObservations)
      .recover { case _ => Seq.empty }
  }

  def getCurrentByLatLon(lat: Double, lon: Double): Future[Seq[AirNowObservation]] = {
    ws.url(s"$baseUrl/aq/observation/latLong/current/")
      .addQueryStringParameters(
        "format" -> "application/json",
        "latitude" -> lat.toString,
        "longitude" -> lon.toString,
        "API_KEY" -> apiKey
      )
      .get()
      .map(parseObservations)
      .recover { case _ => Seq.empty }
  }

  def getForecastByZip(zipCode: String): Future[Seq[JsValue]] = {
    ws.url(s"$baseUrl/aq/forecast/zipCode/")
      .addQueryStringParameters(
        "format" -> "application/json",
        "zipCode" -> zipCode,
        "API_KEY" -> apiKey
      )
      .get()
      .map { response =>
        if (response.status == 200) response.json.asOpt[Seq[JsValue]].getOrElse(Seq.empty)
        else Seq.empty
      }
      .recover { case _ => Seq.empty }
  }

  private def parseObservations(response: WSResponse): Seq[AirNowObservation] = {
    if (response.status == 200) {
      response.json.asOpt[Seq[JsValue]].map { arr =>
        arr.flatMap { obs =>
          for {
            dateObserved <- (obs \ "DateObserved").asOpt[String]
            hourObserved <- (obs \ "HourObserved").asOpt[Int]
            localTimeZone <- (obs \ "LocalTimeZone").asOpt[String]
            reportingArea <- (obs \ "ReportingArea").asOpt[String]
            stateCode <- (obs \ "StateCode").asOpt[String]
            latitude <- (obs \ "Latitude").asOpt[Double]
            longitude <- (obs \ "Longitude").asOpt[Double]
            parameterName <- (obs \ "ParameterName").asOpt[String]
            aqi <- (obs \ "AQI").asOpt[Int]
            catNum <- (obs \ "Category" \ "Number").asOpt[Int]
            catName <- (obs \ "Category" \ "Name").asOpt[String]
          } yield AirNowObservation(
            dateObserved, hourObserved, localTimeZone, reportingArea,
            stateCode, latitude, longitude, parameterName, aqi,
            AirNowCategory(catNum, catName)
          )
        }
      }.getOrElse(Seq.empty)
    } else Seq.empty
  }
}

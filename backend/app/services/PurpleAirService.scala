// air-quality-monitoring-prediction-system/backend/app/services/PurpleAirService.scala
package services

import javax.inject._
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

case class PurpleAirSensor(
  sensorId: String,
  name: String,
  latitude: Double,
  longitude: Double,
  pm25: Double,
  pm10: Double,
  humidity: Int,
  temperature: Double,
  lastSeen: Long
)

object PurpleAirSensor {
  implicit val format: OFormat[PurpleAirSensor] = Json.format[PurpleAirSensor]
}

@Singleton
class PurpleAirService @Inject()(
  ws: WSClient
)(implicit ec: ExecutionContext) {

  private val apiKey = sys.env.getOrElse("PURPLEAIR_API_KEY", "")
  private val baseUrl = "https://api.purpleair.com/v1"

  def getSensorsInBoundingBox(
    nwLat: Double, nwLon: Double,
    seLat: Double, seLon: Double
  ): Future[Seq[PurpleAirSensor]] = {
    ws.url(s"$baseUrl/sensors")
      .addHttpHeaders("X-API-Key" -> apiKey)
      .addQueryStringParameters(
        "fields" -> "name,latitude,longitude,pm2.5,pm10,humidity,temperature,last_seen",
        "nwlat" -> nwLat.toString,
        "nwlng" -> nwLon.toString,
        "selat" -> seLat.toString,
        "selng" -> seLon.toString
      )
      .get()
      .map { response =>
        if (response.status == 200) parseSensors(response.json)
        else Seq.empty
      }
      .recover { case _ => Seq.empty }
  }

  def getSensorById(sensorIndex: Int): Future[Option[PurpleAirSensor]] = {
    ws.url(s"$baseUrl/sensors/$sensorIndex")
      .addHttpHeaders("X-API-Key" -> apiKey)
      .addQueryStringParameters(
        "fields" -> "name,latitude,longitude,pm2.5,pm10,humidity,temperature,last_seen"
      )
      .get()
      .map { response =>
        if (response.status == 200) parseSensor(response.json)
        else None
      }
      .recover { case _ => None }
  }

  private def parseSensors(json: JsValue): Seq[PurpleAirSensor] = {
    (json \ "data").asOpt[Seq[Seq[JsValue]]].map { data =>
      data.flatMap(parseSensorArray)
    }.getOrElse(Seq.empty)
  }

  private def parseSensor(json: JsValue): Option[PurpleAirSensor] = {
    for {
      sensor <- (json \ "sensor").asOpt[JsObject]
      name <- (sensor \ "name").asOpt[String]
      lat <- (sensor \ "latitude").asOpt[Double]
      lon <- (sensor \ "longitude").asOpt[Double]
    } yield PurpleAirSensor(
      sensorId = (sensor \ "sensor_index").asOpt[Int].map(_.toString).getOrElse(""),
      name = name,
      latitude = lat,
      longitude = lon,
      pm25 = (sensor \ "pm2.5").asOpt[Double].getOrElse(0.0),
      pm10 = (sensor \ "pm10").asOpt[Double].getOrElse(0.0),
      humidity = (sensor \ "humidity").asOpt[Int].getOrElse(0),
      temperature = (sensor \ "temperature").asOpt[Double].getOrElse(0.0),
      lastSeen = (sensor \ "last_seen").asOpt[Long].getOrElse(0L)
    )
  }

  private def parseSensorArray(arr: Seq[JsValue]): Option[PurpleAirSensor] = {
    if (arr.length >= 8) {
      Some(PurpleAirSensor(
        sensorId = arr.headOption.flatMap(_.asOpt[Int]).map(_.toString).getOrElse(""),
        name = arr.lift(1).flatMap(_.asOpt[String]).getOrElse(""),
        latitude = arr.lift(2).flatMap(_.asOpt[Double]).getOrElse(0.0),
        longitude = arr.lift(3).flatMap(_.asOpt[Double]).getOrElse(0.0),
        pm25 = arr.lift(4).flatMap(_.asOpt[Double]).getOrElse(0.0),
        pm10 = arr.lift(5).flatMap(_.asOpt[Double]).getOrElse(0.0),
        humidity = arr.lift(6).flatMap(_.asOpt[Int]).getOrElse(0),
        temperature = arr.lift(7).flatMap(_.asOpt[Double]).getOrElse(0.0),
        lastSeen = arr.lift(8).flatMap(_.asOpt[Long]).getOrElse(0L)
      ))
    } else None
  }
}

// air-quality-monitoring-prediction-system/backend/app/models/AQIReading.scala
package models

import play.api.libs.json._
import java.time.Instant

case class AQIReading(
  id: Option[String],
  sensorId: String,
  location: Location,
  aqi: Int,
  pm25: Double,
  pm10: Double,
  o3: Double,
  no2: Double,
  co: Double,
  timestamp: Instant
)

case class Location(
  latitude: Double,
  longitude: Double,
  city: Option[String],
  country: Option[String]
)

object Location {
  implicit val format: OFormat[Location] = Json.format[Location]
}

object AQIReading {
  implicit val instantFormat: Format[Instant] = new Format[Instant] {
    def reads(json: JsValue): JsResult[Instant] = 
      json.validate[String].map(Instant.parse)
    def writes(instant: Instant): JsValue = 
      JsString(instant.toString)
  }
  
  implicit val format: OFormat[AQIReading] = Json.format[AQIReading]
}

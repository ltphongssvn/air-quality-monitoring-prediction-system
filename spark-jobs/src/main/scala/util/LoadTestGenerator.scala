// air-quality-monitoring-prediction-system/spark-jobs/src/main/scala/util/LoadTestGenerator.scala
package util

import scala.util.Random
import java.time.Instant
import play.api.libs.json.Json

object LoadTestGenerator {
  private val random = new Random(42)
  private val sensorIds = (1 to 100).map(i => f"sensor-$i%03d")
  private val cities = Seq(
    ("Los Angeles", 34.0522, -118.2437),
    ("San Francisco", 37.7749, -122.4194),
    ("New York", 40.7128, -74.0060),
    ("Chicago", 41.8781, -87.6298),
    ("Houston", 29.7604, -95.3698)
  )

  def generateReading(): String = {
    val sensorId = sensorIds(random.nextInt(sensorIds.length))
    val (city, baseLat, baseLon) = cities(random.nextInt(cities.length))
    val lat = baseLat + (random.nextDouble() - 0.5) * 0.1
    val lon = baseLon + (random.nextDouble() - 0.5) * 0.1
    val aqi = 20 + random.nextInt(180)
    val pm25 = 5.0 + random.nextDouble() * 50
    val pm10 = 10.0 + random.nextDouble() * 80
    val o3 = 0.01 + random.nextDouble() * 0.1
    val no2 = 0.005 + random.nextDouble() * 0.05
    val co = 0.1 + random.nextDouble() * 1.0
    val timestamp = Instant.now().toString

    s"""{"sensorId":"$sensorId","latitude":$lat,"longitude":$lon,"aqi":$aqi,"pm25":$pm25,"pm10":$pm10,"o3":$o3,"no2":$no2,"co":$co,"timestamp":"$timestamp"}"""
  }

  def main(args: Array[String]): Unit = {
    val count = args.headOption.map(_.toInt).getOrElse(1000)
    (1 to count).foreach { i =>
      println(generateReading())
      if (i % 100 == 0) System.err.println(s"Generated $i messages")
    }
  }
}

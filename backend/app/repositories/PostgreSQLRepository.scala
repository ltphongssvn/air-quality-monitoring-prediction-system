// air-quality-monitoring-prediction-system/backend/app/repositories/PostgreSQLRepository.scala
package repositories

import javax.inject._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

case class AQIRecord(
  id: Option[Long],
  sensorId: String,
  latitude: Double,
  longitude: Double,
  aqi: Int,
  pm25: Double,
  pm10: Double,
  o3: Double,
  no2: Double,
  co: Double,
  timestamp: java.sql.Timestamp
)

@Singleton
class PostgreSQLRepository @Inject()(
  dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class AQIRecordsTable(tag: Tag) extends Table[AQIRecord](tag, "aqi_records") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sensorId = column[String]("sensor_id")
    def latitude = column[Double]("latitude")
    def longitude = column[Double]("longitude")
    def aqi = column[Int]("aqi")
    def pm25 = column[Double]("pm25")
    def pm10 = column[Double]("pm10")
    def o3 = column[Double]("o3")
    def no2 = column[Double]("no2")
    def co = column[Double]("co")
    def timestamp = column[java.sql.Timestamp]("timestamp")

    def * = (id.?, sensorId, latitude, longitude, aqi, pm25, pm10, o3, no2, co, timestamp) <> 
      ((AQIRecord.apply _).tupled, AQIRecord.unapply)
  }

  private val aqiRecords = TableQuery[AQIRecordsTable]

  def insert(record: AQIRecord): Future[Long] = {
    db.run((aqiRecords returning aqiRecords.map(_.id)) += record)
  }

  def findAll(limit: Int = 100): Future[Seq[AQIRecord]] = {
    db.run(aqiRecords.sortBy(_.timestamp.desc).take(limit).result)
  }

  def findById(id: Long): Future[Option[AQIRecord]] = {
    db.run(aqiRecords.filter(_.id === id).result.headOption)
  }

  def findBySensorId(sensorId: String): Future[Seq[AQIRecord]] = {
    db.run(aqiRecords.filter(_.sensorId === sensorId).sortBy(_.timestamp.desc).result)
  }

  def delete(id: Long): Future[Int] = {
    db.run(aqiRecords.filter(_.id === id).delete)
  }
}

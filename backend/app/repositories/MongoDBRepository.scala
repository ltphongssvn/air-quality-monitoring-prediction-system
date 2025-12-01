// air-quality-monitoring-prediction-system/backend/app/repositories/MongoDBRepository.scala
package repositories

import javax.inject._
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class MongoDBRepository @Inject()(implicit ec: ExecutionContext) {

  private val mongoUri = sys.env.getOrElse("MONGODB_URI", "mongodb://localhost:27017")
  private val dbName = sys.env.getOrElse("MONGODB_DATABASE", "air_quality")
  
  private lazy val client: MongoClient = MongoClient(mongoUri)
  private lazy val database: MongoDatabase = client.getDatabase(dbName)

  def getCollection[T](name: String)(implicit ct: ClassTag[T]): MongoCollection[Document] = {
    database.getCollection(name)
  }

  def insert(collection: String, doc: Document): Future[Unit] = {
    database.getCollection(collection)
      .insertOne(doc)
      .toFuture()
      .map(_ => ())
  }

  def insertMany(collection: String, docs: Seq[Document]): Future[Unit] = {
    database.getCollection(collection)
      .insertMany(docs)
      .toFuture()
      .map(_ => ())
  }

  def find(collection: String, filter: Document = Document()): Future[Seq[Document]] = {
    database.getCollection(collection)
      .find(filter)
      .toFuture()
  }

  def findOne(collection: String, filter: Document): Future[Option[Document]] = {
    database.getCollection(collection)
      .find(filter)
      .first()
      .toFutureOption()
  }

  def findRecent(collection: String, limit: Int = 100): Future[Seq[Document]] = {
    database.getCollection(collection)
      .find()
      .sort(descending("timestamp"))
      .limit(limit)
      .toFuture()
  }

  def update(collection: String, filter: Document, update: Document): Future[Long] = {
    database.getCollection(collection)
      .updateOne(filter, Document("$set" -> update))
      .toFuture()
      .map(_.getModifiedCount)
  }

  def delete(collection: String, filter: Document): Future[Long] = {
    database.getCollection(collection)
      .deleteOne(filter)
      .toFuture()
      .map(_.getDeletedCount)
  }

  def close(): Unit = client.close()
}

// air-quality-monitoring-prediction-system/spark-jobs/src/main/scala/graphx/PollutionSpreadGraph.scala
package graphx

import org.apache.spark.sql.SparkSession
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

case class SensorNode(
  sensorId: String,
  latitude: Double,
  longitude: Double,
  aqi: Double
)

object PollutionSpreadGraph {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Pollution Spread Graph")
      .getOrCreate()

    val sc = spark.sparkContext

    // Sample sensor data - in production, load from database
    val sensors = Seq(
      (1L, SensorNode("sensor-001", 34.0522, -118.2437, 42.0)),
      (2L, SensorNode("sensor-002", 34.0622, -118.2537, 55.0)),
      (3L, SensorNode("sensor-003", 34.0722, -118.2337, 38.0)),
      (4L, SensorNode("sensor-004", 34.0422, -118.2637, 61.0))
    )

    val vertices: RDD[(VertexId, SensorNode)] = sc.parallelize(sensors)

    // Edges based on geographic proximity (within threshold distance)
    val edges: RDD[Edge[Double]] = sc.parallelize(Seq(
      Edge(1L, 2L, calculateDistance(sensors(0)._2, sensors(1)._2)),
      Edge(1L, 3L, calculateDistance(sensors(0)._2, sensors(2)._2)),
      Edge(2L, 4L, calculateDistance(sensors(1)._2, sensors(3)._2)),
      Edge(3L, 4L, calculateDistance(sensors(2)._2, sensors(3)._2))
    ))

    val graph = Graph(vertices, edges)

    // PageRank to find influential pollution sources
    val ranks = graph.pageRank(0.0001).vertices
    println("Pollution influence ranks:")
    ranks.collect().foreach { case (id, rank) =>
      println(s"Sensor $id: $rank")
    }

    // Connected components for pollution clusters
    val cc = graph.connectedComponents().vertices
    println("\nPollution clusters:")
    cc.collect().foreach { case (id, cluster) =>
      println(s"Sensor $id -> Cluster $cluster")
    }

    // Propagate pollution using Pregel
    val propagated = propagatePollution(graph, maxIterations = 5)
    println("\nPropagated AQI values:")
    propagated.vertices.collect().foreach { case (id, node) =>
      println(s"Sensor ${node.sensorId}: AQI ${node.aqi}")
    }

    spark.stop()
  }

  def calculateDistance(a: SensorNode, b: SensorNode): Double = {
    val earthRadius = 6371.0 // km
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)

    val x = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
    val c = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x))
    earthRadius * c
  }

  def propagatePollution(graph: Graph[SensorNode, Double], maxIterations: Int): Graph[SensorNode, Double] = {
    val initialMsg = 0.0

    def vprog(id: VertexId, node: SensorNode, msg: Double): SensorNode = {
      if (msg > 0) node.copy(aqi = (node.aqi + msg * 0.3) / 1.3)
      else node
    }

    def sendMsg(triplet: EdgeTriplet[SensorNode, Double]): Iterator[(VertexId, Double)] = {
      val diff = triplet.srcAttr.aqi - triplet.dstAttr.aqi
      if (diff > 10) Iterator((triplet.dstId, diff * 0.5))
      else Iterator.empty
    }

    def mergeMsg(a: Double, b: Double): Double = (a + b) / 2

    Pregel(graph, initialMsg, maxIterations)(vprog, sendMsg, mergeMsg)
  }
}

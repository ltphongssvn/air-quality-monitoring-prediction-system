// air-quality-monitoring-prediction-system/spark-jobs/src/main/scala/ml/BreezeAnalytics.scala
package ml

import breeze.linalg._
import breeze.stats._
import breeze.interpolation._

object BreezeAnalytics {

  def correlationMatrix(data: Seq[Seq[Double]]): DenseMatrix[Double] = {
    val matrix = DenseMatrix(data.map(_.toArray): _*)
    val cols = matrix.cols
    val result = DenseMatrix.zeros[Double](cols, cols)
    
    for (i <- 0 until cols; j <- 0 until cols) {
      val col1 = matrix(::, i)
      val col2 = matrix(::, j)
      result(i, j) = correlation(col1, col2)
    }
    result
  }

  def correlation(a: DenseVector[Double], b: DenseVector[Double]): Double = {
    val n = a.length
    val meanA = mean(a)
    val meanB = mean(b)
    val stdA = stddev(a)
    val stdB = stddev(b)
    
    if (stdA == 0 || stdB == 0) 0.0
    else {
      val cov = sum((a - meanA) *:* (b - meanB)) / n
      cov / (stdA * stdB)
    }
  }

  def interpolateAQI(
    knownPoints: Seq[(Double, Double, Double)], // (lat, lon, aqi)
    targetLat: Double,
    targetLon: Double
  ): Double = {
    // Inverse Distance Weighting (IDW) interpolation
    val weights = knownPoints.map { case (lat, lon, _) =>
      val dist = haversineDistance(lat, lon, targetLat, targetLon)
      if (dist < 0.001) return knownPoints.find(p => 
        haversineDistance(p._1, p._2, targetLat, targetLon) < 0.001
      ).map(_._3).getOrElse(0.0)
      1.0 / Math.pow(dist, 2)
    }
    
    val weightedSum = knownPoints.zip(weights).map { case ((_, _, aqi), w) => aqi * w }.sum
    val totalWeight = weights.sum
    weightedSum / totalWeight
  }

  def haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double = {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2)
    R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  }

  def generateHeatmapData(
    readings: Seq[(Double, Double, Double)],
    gridSize: Int = 50,
    bounds: (Double, Double, Double, Double) // (minLat, maxLat, minLon, maxLon)
  ): Array[Array[Double]] = {
    val (minLat, maxLat, minLon, maxLon) = bounds
    val latStep = (maxLat - minLat) / gridSize
    val lonStep = (maxLon - minLon) / gridSize
    
    Array.tabulate(gridSize, gridSize) { (i, j) =>
      val lat = minLat + i * latStep
      val lon = minLon + j * lonStep
      interpolateAQI(readings, lat, lon)
    }
  }
}

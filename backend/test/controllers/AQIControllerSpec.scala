// air-quality-monitoring-prediction-system/backend/test/controllers/AQIControllerSpec.scala
package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

class AQIControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "AQIController GET /health" should {
    "return OK with healthy status" in {
      val request = FakeRequest(GET, "/health")
      val result = route(app, request).get

      status(result) mustBe OK
      contentAsString(result) must include("healthy")
    }
  }

  "AQIController GET /api/v1/aqi" should {
    "return OK with AQI readings" in {
      val request = FakeRequest(GET, "/api/v1/aqi")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }
}

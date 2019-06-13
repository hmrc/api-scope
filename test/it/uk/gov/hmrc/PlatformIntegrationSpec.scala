/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.uk.gov.hmrc

import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.{Application, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.controllers._
import uk.gov.hmrc.play.test.UnitSpec

trait PlatformIntegrationSpec extends UnitSpec with GuiceOneAppPerTest {

  val publishApiDefinition: Boolean

  override def newAppForTest(testData: TestData): Application = GuiceApplicationBuilder()
    .configure("run.mode" -> "Stub")
    .configure(Map(
      "publishApiDefinition" -> publishApiDefinition))
    .in(Mode.Test).build()

  trait Setup {
    implicit def mat: akka.stream.Materializer = app.injector.instanceOf[akka.stream.Materializer]
    val controller: ApiDefinitionController = app.injector.instanceOf[ApiDefinitionController]
  }
}

class PublishApiDefinitionEnabledSpec extends PlatformIntegrationSpec {
  val publishApiDefinition = true

  "microservice" should {

    "return the JSON definition" in new Setup {
      route(app, FakeRequest(GET, "/api/definition")) match {
        case Some(resultF) =>
          val result = await(resultF)
          status(result) shouldBe OK
          bodyOf(result) should include(""""context": "api-scope"""")

        case _ => fail
      }
    }

    "return the RAML" in new Setup {
      route(app, FakeRequest(GET, "/api/conf/1.0/application.raml")) match {
        case Some(resultF) =>
          val result = await(resultF)
          status(result) shouldBe OK
          bodyOf(result) should include("#%RAML 1.0")

        case _ => fail
      }
    }
  }
}

class PublishApiDefinitionDisabledSpec extends PlatformIntegrationSpec {
  val publishApiDefinition = false

  "microservice" should {

    "return a 204 from the definition endpoint" in new Setup {
      route(app, FakeRequest(GET, "/api/definition")) match {
        case Some(resultF) =>
          val result = await(resultF)
          status(result) shouldBe NO_CONTENT

        case _ => fail
      }
    }

    "return a 204 from the RAML endpoint" in new Setup {
      route(app, FakeRequest(GET, "/api/conf/1.0/application.raml")) match {
        case Some(resultF) =>
          val result = await(resultF)
          status(result) shouldBe NO_CONTENT

        case _ => fail
      }
    }
  }
}

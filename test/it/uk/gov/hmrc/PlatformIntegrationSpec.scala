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

import org.scalatest.BeforeAndAfterAll
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.controllers._
import it.uk.gov.hmrc.stubs.ServiceLocatorStub
import uk.gov.hmrc.play.test.UnitSpec

class PlatformIntegrationSpec extends UnitSpec with AppUsingStubs with ServiceLocatorStub with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    super.beforeAll()
    serviceLocatorWillAcceptTheRegistration()
  }

  trait Setup {
    val controller = app.injector.instanceOf[ApiDefinitionController]
  }

  "microservice" should {

    "register itself with the service locator" in new Setup {
      verifyServiceLocatorWasCalledToRegister(appName, appUrl)
    }

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

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

package it.uk.gov.hmrc.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.TestSuite
import play.api.libs.json.Json
import play.api.test.Helpers.{CONTENT_TYPE, JSON, NO_CONTENT}
import uk.gov.hmrc.connectors.Registration

trait ServiceLocatorStub extends WireMockStubbing with StubConfiguration {
  this: TestSuite =>

  val appName = "application-name"
  val appUrl = "http://localhost"

  override def stubConfiguration(configuration: Map[String, Any]) = {
    super.stubConfiguration(configuration) ++ Map(
      "appName" -> appName,
      "appUrl" -> appUrl,
      "microservice.services.service-locator.host" -> stubHost,
      "microservice.services.service-locator.port" -> stubPort,
      "microservice.services.service-locator.enabled" -> true
    )
  }

  def serviceLocatorWillAcceptTheRegistration() = {
    stubFor(post(urlMatching("/registration")).willReturn(aResponse().withStatus(NO_CONTENT)))
  }

  def verifyServiceLocatorWasCalledToRegister(appName: String, appUrl: String): Unit = {
    val registrationPayload = Json.toJson(Registration(appName, appUrl, Some(Map("third-party-api" -> "true")))).toString

    verify(1, postRequestedFor(urlPathMatching("/registration")).
      withHeader(CONTENT_TYPE, equalTo(JSON)).
      withRequestBody(equalToJson(registrationPayload)))
  }
}

/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apiscope.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.{failed, successful}

import akka.stream.Materializer
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2
import org.scalatest.prop.Tables.Table
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.libs.json.{JsDefined, JsString, Json}
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubControllerComponentsFactory, StubPlayBodyParsersFactory}
import play.mvc.Http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK}

import uk.gov.hmrc.apiscope.models.ConfidenceLevel._
import uk.gov.hmrc.apiscope.models.{ErrorCode, ErrorDescription, ErrorResponse, Scope}
import uk.gov.hmrc.apiscope.services.ScopeService
import uk.gov.hmrc.util.AsyncHmrcSpec

class ScopeControllerSpec extends AsyncHmrcSpec
  with GuiceOneAppPerSuite
  with StubControllerComponentsFactory
  with StubPlayBodyParsersFactory{

  val scope: Scope = Scope("key1", "name1", "desc1")

  val validScopeBody: String = """[{"key":"key1", "name":"name1", "description":"desc1"}]"""
  val validScopeBodyWithConfidenceLevel: String = """[{"key":"key1", "name":"name1", "description":"desc1", "confidenceLevel":200}]"""
  val scopeBodyMissingName: String = """[{"key":"key1", "description":"desc1"}]"""
  val scopeBodyMissingKeyAndDesc: String = """[{"name":"name1"},{"key":"key2","name":"name2"}]"""
  val scopeBodyWithInvalidConfidenceLevel: String = """[{"key":"key1", "name":"name1", "description":"desc1", "confidenceLevel":1001}]"""


  implicit lazy val materializer: Materializer = fakeApplication.materializer

  trait Setup {
    val mockScopeService: ScopeService = mock[ScopeService]
    val controllerComponents: ControllerComponents = stubControllerComponents()
    
    val underTest = new ScopeController(mockScopeService, controllerComponents, stubPlayBodyParsers(materializer))

    implicit lazy val request = FakeRequest()

    when(mockScopeService.saveScopes(any[Seq[Scope]])).thenReturn(successful(Seq()))
    when(mockScopeService.fetchScopes(Set(scope.key))).thenReturn(successful(Seq(scope)))
  }

  "createOrUpdateScope" should {

    "store scope and return 200 (ok) when the json payload is valid for the request" in new Setup {
      val result = underTest.createOrUpdateScope()(request.withBody(Json.parse(validScopeBody)))

      status(result) shouldBe OK
      verify(mockScopeService).saveScopes(Seq(Scope("key1", "name1", "desc1")))
    }

    "store scope with a particular confidence level, and return 200 (ok)" in new Setup {
      val result = underTest.createOrUpdateScope()(request.withBody(Json.parse(validScopeBodyWithConfidenceLevel)))

      status(result) shouldBe OK
      verify(mockScopeService).saveScopes(Seq(Scope("key1", "name1", "desc1", confidenceLevel = Some(L200))))
    }

    "fail with a 422 (invalid request) when the json payload is invalid for the request" in new Setup {

      val invalidRequests: TableFor2[String, Int] = Table(
        ("json payload", "expected response code"),
        (scopeBodyMissingName, 422),
        (scopeBodyMissingKeyAndDesc, 422),
        (scopeBodyWithInvalidConfidenceLevel, 422)
      )

      forAll (invalidRequests) { (invalidBody, expectedResponseCode) =>

        val result = underTest.createOrUpdateScope()(request.withBody(Json.parse(invalidBody)))

        status(result) shouldBe expectedResponseCode
        verify(mockScopeService, times(0)).saveScopes(*)
      }
    }

    "fail with a 500 (internal server error) when the service throws an exception" in new Setup {

      when(mockScopeService.saveScopes(any[Seq[Scope]])).thenReturn(failed(new RuntimeException()))

      val result = underTest.createOrUpdateScope()(request.withBody(Json.parse(validScopeBody)))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "fetchScope" should {

    "return 200 (ok) with the scope when the scope exists" in new Setup {

      val scope: Scope = Scope("key1", "name1", "desc1")

      when(mockScopeService.fetchScope("key1")).thenReturn(successful(Some(scope)))

      val result = underTest.fetchScope("key1")(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldEqual Json.toJson(scope)
    }

    "return 404 (not found) when the scope does not exist" in new Setup {

      when(mockScopeService.fetchScope("key1")).thenReturn(successful(None))

      val result = underTest.fetchScope("key1")(request)

      status(result) shouldBe NOT_FOUND
      contentAsJson(result) \ "code" shouldEqual JsDefined(JsString(ErrorCode.SCOPE_NOT_FOUND.toString))
    }

    "return 500 (internal service error) when the service throws an exception" in new Setup {

      when(mockScopeService.fetchScope("key1")).thenReturn(failed(new RuntimeException()))

      val result = underTest.fetchScope("key1")(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "fetchScopes" should {
      val scope1 = Scope("key1", "name1", "desc1")
      val scope2 = Scope("key2", "name2", "desc2")

    "return 200 (ok) with the scopes requested when keys parameter is defined" in new Setup {
      when(mockScopeService.fetchScopes(Set("key1", "key2"))).thenReturn(successful(Seq(scope1, scope2)))

      val result = underTest.fetchScopes("key1 key2")(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldEqual Json.toJson(Seq(scope1, scope2))
    }

    "return 200 (ok) with the scopes requested when keys parameter has duplicates" in new Setup {
      when(mockScopeService.fetchScopes(Set("key1", "key2"))).thenReturn(successful(Seq(scope1, scope2)))

      val result = underTest.fetchScopes("key1 key2 key1")(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldEqual Json.toJson(Seq(scope1, scope2))
    }

    "return 200 (ok) with all the scopes requested when keys is *" in new Setup {
      when(mockScopeService.fetchAll).thenReturn(successful(Seq(scope1, scope2)))

      val result = underTest.fetchScopes("*")(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldEqual Json.toJson(Seq(scope1, scope2))
    }

    "return 500 (internal service error) when the service throws an exception while fetching defined scopes" in new Setup {

      when(mockScopeService.fetchScopes(Set("key1", "key2"))).thenReturn(failed(new RuntimeException()))

      val result = underTest.fetchScopes("key1 key2")(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (internal service error) when the service throws an exception while fetching all scopes" in new Setup {

      when(mockScopeService.fetchAll).thenReturn(failed(new RuntimeException()))

      val result = underTest.fetchScopes("*")(request)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

  }

  "validate" should {
    "succeed with status 204 (NoContent) when the payload is valid" in new Setup {
      val regularResult = underTest.validate()(request.withBody(Json.parse(validScopeBody)))
      status(regularResult) shouldBe NO_CONTENT

      val resultWithConfidenceLevel = underTest.validate()(request.withBody(Json.parse(validScopeBodyWithConfidenceLevel)))
      status(resultWithConfidenceLevel) shouldBe NO_CONTENT
    }

    "fail with status 422 (UnprocessableEntity) when several elements are missing" in new Setup {

      val result = underTest.validate()(request.withBody(Json.parse(scopeBodyMissingKeyAndDesc)))

      contentAsJson(result) shouldEqual Json.toJson(
        ErrorResponse(ErrorCode.API_INVALID_JSON, "Json cannot be converted to API Scope",
          Some(Seq(
            ErrorDescription("(0)/description", "element is missing"),
            ErrorDescription("(0)/key", "element is missing"),
            ErrorDescription("(1)/description", "element is missing")
          ))))
    }

    "fail with status 422 (UnprocessableEntity) when the name element is missing" in new Setup {
      val result = underTest.validate()(request.withBody(Json.parse(scopeBodyMissingName)))

      contentAsJson(result) shouldEqual Json.toJson(
        ErrorResponse(ErrorCode.API_INVALID_JSON, "Json cannot be converted to API Scope",
          Some(Seq(
            ErrorDescription("(0)/name", "element is missing")
          ))))
    }

    "fail with status 422 (UnprocessableEntity) when the confidenceLevel is invalid" in new Setup {
      val result = underTest.validate()(request.withBody(Json.parse(scopeBodyWithInvalidConfidenceLevel)))

      contentAsJson(result) shouldEqual Json.toJson(
        ErrorResponse(ErrorCode.API_INVALID_JSON, "Json cannot be converted to API Scope",
          Some(Seq(
            ErrorDescription("(0)/confidenceLevel", "confidence level must be one of: 50, 200, 250, 500")
          )))
      )
    }
  }

}

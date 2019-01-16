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

package unit.controllers

import org.mockito.BDDMockito.given
import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import play.api.libs.json.{JsDefined, JsString, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.mvc.Http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK}
import uk.gov.hmrc.controllers.ScopeController
import uk.gov.hmrc.models.ConfidenceLevel._
import uk.gov.hmrc.models.{ErrorCode, ErrorDescription, ErrorResponse, Scope}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.services.ScopeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed

class ScopeControllerSpec extends UnitSpec with MockitoSugar with ScalaFutures with WithFakeApplication {

  implicit lazy val materializer = fakeApplication.materializer

  trait Setup {
    val mockScopeService = mock[ScopeService]
    
    val underTest = new ScopeController(mockScopeService)

    implicit lazy val request = FakeRequest()

    when(mockScopeService.saveScopes(any[Seq[Scope]])).thenReturn(Future(Seq()))
    given(mockScopeService.fetchScopes(Set(scope.key))).willReturn(Seq(scope))
  }

  "createOrUpdateScope" should {

    "store scope and return 200 (ok) when the json payload is valid for the request" in new Setup {
      val result = await(underTest.createOrUpdateScope()(request.withBody(Json.parse(validScopeBody))))

      status(result) shouldBe OK
      verify(mockScopeService).saveScopes(Seq(Scope("key1", "name1", "desc1")))
    }

    "store scope with a particular confidence level, and return 200 (ok)" in new Setup {
      val result = await(underTest.createOrUpdateScope()(request.withBody(Json.parse(validScopeBodyWithConfidenceLevel))))

      status(result) shouldBe OK
      verify(mockScopeService).saveScopes(Seq(Scope("key1", "name1", "desc1", confidenceLevel = Some(L200))))
    }

    "fail with a 422 (invalid request) when the json payload is invalid for the request" in new Setup {

      val invalidRequests = Table(
        ("json payload", "expected response code"),
        (scopeBodyMissingName, 422),
        (scopeBodyMissingKeyAndDesc, 422),
        (scopeBodyWithInvalidConfidenceLevel, 422)
      )

      forAll (invalidRequests) { (invalidBody, expectedResponseCode) =>

        val result = await(underTest.createOrUpdateScope()(request.withBody(Json.parse(invalidBody))))

        status(result) shouldBe expectedResponseCode
        verify(mockScopeService, Mockito.times(0)).saveScopes(any())
      }
    }

    "fail with a 500 (internal server error) when the service throws an exception" in new Setup {

      given(mockScopeService.saveScopes(any[Seq[Scope]])).willReturn(failed(new RuntimeException()))

      val result = await(underTest.createOrUpdateScope()(request.withBody(Json.parse(validScopeBody))))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "fetchScope" should {

    "return 200 (ok) with the scope when the scope exists" in new Setup {

      val scope = Scope("key1", "name1", "desc1")

      given(mockScopeService.fetchScope("key1")).willReturn(Some(scope))

      val result: Result = await(underTest.fetchScope("key1")(request))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldEqual Json.toJson(scope)
    }

    "return 404 (not found) when the scope does not exist" in new Setup {

      given(mockScopeService.fetchScope("key1")).willReturn(None)

      val result: Result = await(underTest.fetchScope("key1")(request))

      status(result) shouldBe NOT_FOUND
      jsonBodyOf(result) \ "code" shouldEqual JsDefined(JsString(ErrorCode.SCOPE_NOT_FOUND.toString))
    }

    "return 500 (internal service error) when the service throws an exception" in new Setup {

      given(mockScopeService.fetchScope("key1")).willReturn(failed(new RuntimeException()))

      val result: Result = await(underTest.fetchScope("key1")(request))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "fetchScopes" should {
      val scope1 = Scope("key1", "name1", "desc1")
      val scope2 = Scope("key2", "name2", "desc2")

    "return 200 (ok) with the scopes requested when keys parameter is defined" in new Setup {
      given(mockScopeService.fetchScopes(Set("key1", "key2"))).willReturn(Seq(scope1, scope2))

      val result: Result = await(underTest.fetchScopes("key1 key2")(request))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldEqual Json.toJson(Seq(scope1, scope2))
    }

    "return 200 (ok) with the scopes requested when keys parameter has duplicates" in new Setup {
      given(mockScopeService.fetchScopes(Set("key1", "key2"))).willReturn(Seq(scope1, scope2))

      val result: Result = await(underTest.fetchScopes("key1 key2 key1")(request))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldEqual Json.toJson(Seq(scope1, scope2))
    }

    "return 200 (ok) with all the scopes requested when keys is *" in new Setup {
      given(mockScopeService.fetchAll).willReturn(Seq(scope1, scope2))

      val result: Result = await(underTest.fetchScopes("*")(request))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldEqual Json.toJson(Seq(scope1, scope2))
    }

    "return 500 (internal service error) when the service throws an exception while fetching defined scopes" in new Setup {

      given(mockScopeService.fetchScopes(Set("key1", "key2"))).willReturn(failed(new RuntimeException()))

      val result: Result = await(underTest.fetchScopes("key1 key2")(request))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 500 (internal service error) when the service throws an exception while fetching all scopes" in new Setup {

      given(mockScopeService.fetchAll).willReturn(failed(new RuntimeException()))

      val result: Result = await(underTest.fetchScopes("*")(request))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

  }

  "validate" should {
    "succeed with status 204 (NoContent) when the payload is valid" in new Setup {
      val regularResult = await(underTest.validate()(request.withBody(Json.parse(validScopeBody))))
      status(regularResult) shouldBe NO_CONTENT

      val resultWithConfidenceLevel = await(underTest.validate()(request.withBody(Json.parse(validScopeBodyWithConfidenceLevel))))
      status(resultWithConfidenceLevel) shouldBe NO_CONTENT
    }

    "fail with status 422 (UnprocessableEntity) when several elements are missing" in new Setup {

      val result = await(underTest.validate()(request.withBody(Json.parse(scopeBodyMissingKeyAndDesc))))

      jsonBodyOf(result) shouldEqual Json.toJson(
        ErrorResponse(ErrorCode.API_INVALID_JSON, "Json cannot be converted to API Scope",
          Some(Seq(
            ErrorDescription("(0)/description", "element is missing"),
            ErrorDescription("(0)/key", "element is missing"),
            ErrorDescription("(1)/description", "element is missing")
          ))))
    }

    "fail with status 422 (UnprocessableEntity) when the name element is missing" in new Setup {
      val result = await(underTest.validate()(request.withBody(Json.parse(scopeBodyMissingName))))

      jsonBodyOf(result) shouldEqual Json.toJson(
        ErrorResponse(ErrorCode.API_INVALID_JSON, "Json cannot be converted to API Scope",
          Some(Seq(
            ErrorDescription("(0)/name", "element is missing")
          ))))
    }

    "fail with status 422 (UnprocessableEntity) when the confidenceLevel is invalid" in new Setup {
      val result = await(underTest.validate()(request.withBody(Json.parse(scopeBodyWithInvalidConfidenceLevel))))

      jsonBodyOf(result) shouldEqual Json.toJson(
        ErrorResponse(ErrorCode.API_INVALID_JSON, "Json cannot be converted to API Scope",
          Some(Seq(
            ErrorDescription("(0)/confidenceLevel", "confidence level must be one of: 50, 100, 200, 300")
          ))))
    }
  }

  val scope = Scope("key1", "name1", "desc1")

  val validScopeBody = """[{"key":"key1", "name":"name1", "description":"desc1"}]"""
  val validScopeBodyWithConfidenceLevel = """[{"key":"key1", "name":"name1", "description":"desc1", "confidenceLevel":200}]"""
  val scopeBodyMissingName = """[{"key":"key1", "description":"desc1"}]"""
  val scopeBodyMissingKeyAndDesc = """[{"name":"name1"},{"key":"key2","name":"name2"}]"""
  val scopeBodyWithInvalidConfidenceLevel = """[{"key":"key1", "name":"name1", "description":"desc1", "confidenceLevel":1001}]"""
}

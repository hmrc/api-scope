/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiscope

import java.util.concurrent.TimeUnit
import scala.concurrent.Await.result
import scala.concurrent.duration.Duration

import scalaj.http.Http

import play.api.libs.json.{JsValue, Json}

import uk.gov.hmrc.apiscope.repository.ScopeRepository

class ScopeFeatureSpec extends BaseFeatureSpec {

  val timeout = Duration(5, TimeUnit.SECONDS)

  val repository = app.injector.instanceOf[ScopeRepository]

  Feature("Publish Scope on request") {

    Scenario("Scope is created") {

      Given("The scope does not exist")

      When("A request is received for the scope")
      postScope(scopeRequest("read:employment", "Read Employment"))

      Then("The scope is created")
      fetchScope("read:employment") shouldEqual Json.parse(scope("read:employment", "Read Employment"))
    }

    Scenario("Scope is updated") {

      Given("The scope does exist")
      postScope(scopeRequest("read:employment", "Read Employment"))

      When("A request is received for the scope")
      postScope(scopeRequest("read:employment", "Read Employment Updated"))

      Then("The scope is updated")
      fetchScope("read:employment") shouldEqual Json.parse(scope("read:employment", "Read Employment Updated"))
    }
  }

  private def fetchScope(key: String): JsValue = {
    val response = Http(s"$serverUrl/scope/$key").asString.body
    Json.parse(response)
  }

  private def postScope(body: String) = {
    Http(s"$serverUrl/scope")
      .header("Content-Type", "application/json")
      .postData(body).asString
  }

  private def scopeRequest(key: String, name: String) = {
    s"""    [
       |       {
       |         "key" : "$key",
       |         "name" : "$name",
       |          "description" : "Ability to read employment information"
       |        }
       |    ]
    """.stripMargin.replaceAll("\n", "")
  }

  private def scope(key: String, name: String) = {
    s"""    {
       |     "key" : "$key",
       |     "name" : "$name",
       |     "description" : "Ability to read employment information"
       |    }
    """.stripMargin.replaceAll("\n", "")
  }

  override def afterEach(): Unit = {
    dropDatabase
  }

  def dropDatabase = {
    repository.collection.drop()
    result(repository.ensureIndexes, timeout)
  }

}

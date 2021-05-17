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

package uk.gov.hmrc.apiscope.models

import uk.gov.hmrc.play.test.UnitSpec

class ScopeRequestSpec extends UnitSpec {

  val scopeData = ScopeData("key", "name", "description")
  val scopeRequest = Seq(scopeData)

  val testCases = Map(
    "scope key is empty" -> { s: Seq[ScopeData] => Seq(scopeData.copy(key = "")) },
    "scope key is empty string" -> { s: Seq[ScopeData] => Seq(scopeData.copy(key = "   ")) },
    "scope name is empty" -> { s: Seq[ScopeData] => Seq(scopeData.copy(name = "")) },
    "scope name is empty string" -> { s: Seq[ScopeData] => Seq(scopeData.copy(name = "   ")) },
    "scope description is empty" -> { s: Seq[ScopeData] => Seq(scopeData.copy(description = "")) },
    "scope description is empty string" -> { s: Seq[ScopeData] => Seq(scopeData.copy(description = "   ")) }
  )
  "scopeRequest" should {
    testCases foreach {
      e => s"throw an exception when ${e._1}" in {
          verifyExceptionIsThrownFor(e._2)
        }
    }

    def verifyExceptionIsThrownFor(f: Seq[ScopeData] => Seq[ScopeData]) = {
      intercept[IllegalArgumentException](f(scopeRequest))
    }
  }
}

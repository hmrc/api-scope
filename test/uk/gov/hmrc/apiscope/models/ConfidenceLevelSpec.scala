/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{JsError, JsNumber, JsSuccess, Json}

import uk.gov.hmrc.apiscope.models.ConfidenceLevel._
import uk.gov.hmrc.util.HmrcSpec

class ConfidenceLevelSpec extends HmrcSpec {

  "read" should {
    "read valid confidence levels" in {
      Json.fromJson[ConfidenceLevel.Value](JsNumber(50)) shouldBe JsSuccess(L50)
      Json.fromJson[ConfidenceLevel.Value](JsNumber(100)) shouldBe JsSuccess(L200)
      Json.fromJson[ConfidenceLevel.Value](JsNumber(200)) shouldBe JsSuccess(L200)
      Json.fromJson[ConfidenceLevel.Value](JsNumber(250)) shouldBe JsSuccess(L250)
      Json.fromJson[ConfidenceLevel.Value](JsNumber(300)) shouldBe JsSuccess(L200)
    }

    "report invalid confidence levels" in {
      val error = JsError("confidence level must be one of: 50, 200, 250, 500")
      Json.fromJson[ConfidenceLevel.Value](JsNumber(0)) shouldBe error
      Json.fromJson[ConfidenceLevel.Value](JsNumber(1)) shouldBe error
      Json.fromJson[ConfidenceLevel.Value](JsNumber(101)) shouldBe error
    }
  }

  "write" should {
    "write out correct values" in {
      Json.toJson(L50) shouldBe JsNumber(50)
      Json.toJson(L200) shouldBe JsNumber(200)
      Json.toJson(L250) shouldBe JsNumber(250)
    }
  }
}

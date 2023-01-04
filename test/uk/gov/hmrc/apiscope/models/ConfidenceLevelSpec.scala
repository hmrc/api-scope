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

package uk.gov.hmrc.apiscope.models

import play.api.libs.json.{JsError, JsNumber, JsSuccess, Json}
import uk.gov.hmrc.apiscope.models.ConfidenceLevel.{format, _}
import uk.gov.hmrc.util.HmrcSpec

class ConfidenceLevelSpec extends HmrcSpec {

  "read" should {
    "read valid confidence levels" in {
      Json.fromJson[ConfidenceLevel](JsNumber(50)) shouldBe JsSuccess(L50)
      Json.fromJson[ConfidenceLevel](JsNumber(200)) shouldBe JsSuccess(L200)
      Json.fromJson[ConfidenceLevel](JsNumber(250)) shouldBe JsSuccess(L250)
      Json.fromJson[ConfidenceLevel](JsNumber(500)) shouldBe JsSuccess(L500)
    }
  }

  "write" should {
    "write out correct values" in {
      Json.toJson[ConfidenceLevel](L50) shouldBe JsNumber(50)
      Json.toJson[ConfidenceLevel](L200) shouldBe JsNumber(200)
      Json.toJson[ConfidenceLevel](L250) shouldBe JsNumber(250)
      Json.toJson[ConfidenceLevel](L500) shouldBe JsNumber(500)
    }
  }
}

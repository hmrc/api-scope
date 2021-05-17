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

import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.json.JsError

class ErrorCodeSpec extends UnitSpec {

  "read" should {
    "read valid error code" in {
      Json.fromJson[ErrorCode.Value](JsString("SCOPE_NOT_FOUND")) shouldBe JsSuccess(ErrorCode.SCOPE_NOT_FOUND)
    }

    "report invalid error code" in {
      val error = JsError("error code must be one of: SCOPE_NOT_FOUND")
      Json.fromJson[ErrorCode.Value](JsNumber(0)) should matchPattern {
        case e : JsError =>
      }
      Json.fromJson[ErrorCode.Value](JsString("NOT VALID")) should matchPattern {
        case e : JsError =>
      }
    }
  }
}

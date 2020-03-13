/*
 * Copyright 2020 HM Revenue & Customs
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

object ErrorCode extends Enumeration {

  implicit val format = EnumJson.enumFormat(ErrorCode)

  type ErrorCode = Value

  val SCOPE_NOT_FOUND = Value("SCOPE_NOT_FOUND")
  val INVALID_REQUEST_PAYLOAD = Value("INVALID_REQUEST_PAYLOAD")
  val UNKNOWN_ERROR = Value("UNKNOWN_ERROR")
  val API_INVALID_JSON = Value("API_INVALID_JSON")
  val API_SCOPE_ALREADY_IN_USE = Value("API_SCOPE_ALREADY_IN_USE")
}

case class ErrorResponse(code: ErrorCode.Value, message: String, details: Option[Seq[ErrorDescription]] = None)

object ErrorResponse {
  implicit val format1 = Json.format[ErrorDescription]
  implicit val format2 = EnumJson.enumFormat(ErrorCode)
  implicit val format3 = Json.format[ErrorResponse]
}

case class ErrorDescription(field: String, message: String)

object ErrorDescription {
  implicit val format = Json.format[ErrorDescription]
}

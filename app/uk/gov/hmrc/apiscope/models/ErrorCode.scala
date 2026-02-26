/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.*
import uk.gov.hmrc.apiplatform.modules.common.domain.services.SimpleEnumJsonFormatting

enum ErrorCode:
  case ScopeNotFound, InvalidRequestPayload, UnknownError, ApiInvalidJson, ApiScopeAlreadyInUse

object ErrorCode {
  def apply(text: String): Option[ErrorCode] = ErrorCode.values.find(_.toString.equalsIgnoreCase(text))

  def unsafeApply(text: String): ErrorCode =
    apply(text).getOrElse(throw new RuntimeException(s"$text is not a valid Error Code"))

  import play.api.libs.json.Format

  given Format[ErrorCode] = SimpleEnumJsonFormatting.createEnumFormatFor[ErrorCode]("Error Code", apply)
}

case class ErrorDescription(field: String, message: String)

case class ErrorResponse(code: ErrorCode, message: String, details: Option[Seq[ErrorDescription]] = None)

object ErrorDescription {
  given OFormat[ErrorDescription] = Json.format[ErrorDescription]
}

object ErrorResponse {
  given OFormat[ErrorResponse] = Json.format[ErrorResponse]
}

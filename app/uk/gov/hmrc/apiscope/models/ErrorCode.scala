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

import play.api.libs.json._
import uk.gov.hmrc.apiplatform.modules.common.domain.services.SealedTraitJsonFormatting

sealed trait ErrorCode

object ErrorCode {
  case object SCOPE_NOT_FOUND          extends ErrorCode
  case object INVALID_REQUEST_PAYLOAD  extends ErrorCode
  case object UNKNOWN_ERROR            extends ErrorCode
  case object API_INVALID_JSON         extends ErrorCode
  case object API_SCOPE_ALREADY_IN_USE extends ErrorCode
  val values: Set[ErrorCode]                 = Set(SCOPE_NOT_FOUND, INVALID_REQUEST_PAYLOAD, UNKNOWN_ERROR, API_INVALID_JSON, API_SCOPE_ALREADY_IN_USE)
  def apply(text: String): Option[ErrorCode] = ErrorCode.values.find(_.toString() == text.toUpperCase)

  def unsafeApply(text: String): ErrorCode = apply(text).getOrElse(throw new RuntimeException(s"$text is not a valid Error Code"))

  implicit val format: Format[ErrorCode] = SealedTraitJsonFormatting.createFormatFor[ErrorCode]("Error Code", apply)
}

case class ErrorResponse(code: ErrorCode, message: String, details: Option[Seq[ErrorDescription]] = None)

object ErrorResponse {
  implicit val format1: OFormat[ErrorDescription] = Json.format[ErrorDescription]
  implicit val format3: OFormat[ErrorResponse]    = Json.format[ErrorResponse]
}

case class ErrorDescription(field: String, message: String)

object ErrorDescription {
  implicit val format: OFormat[ErrorDescription] = Json.format[ErrorDescription]
}

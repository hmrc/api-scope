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

package uk.gov.hmrc.apiscope.repository

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.apiscope.models.{ConfidenceLevel, Scope}
import uk.gov.hmrc.play.json.Union

private[repository] object PlayHmrcMongoFormatters {

//  implicit val formatScope: OFormat[Scope] = Union.from[Scope]("confidenceLevel")
//    .and[Scope](ConfidenceLevel.L50.toString)
//    .and[Scope](ConfidenceLevel.L200.toString)
//    .and[Scope](ConfidenceLevel.L250.toString)
//    .and[Scope](ConfidenceLevel.L500.toString)
//    .format

  implicit val formatScopeL50: OFormat[ConfidenceLevel.L50.type] = Json.format[ConfidenceLevel.L50.type]
  implicit val formatScopeL200: OFormat[ConfidenceLevel.L200.type] = Json.format[ConfidenceLevel.L200.type]
  implicit val formatScopeL250: OFormat[ConfidenceLevel.L250.type] = Json.format[ConfidenceLevel.L250.type]
  implicit val formatScopeL500: OFormat[ConfidenceLevel.L500.type] = Json.format[ConfidenceLevel.L500.type]

}

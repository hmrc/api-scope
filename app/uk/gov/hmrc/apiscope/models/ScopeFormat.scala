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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.play.json.Union

/** */
object ScopeFormat extends OFormat[Scope] {

//  implicit val formatScope: OFormat[Scope] = Union.from[Scope]("confidenceLevel")
//    .and[Scope](ConfidenceLevel.L50.toString)
//    .and[Scope](ConfidenceLevel.L200.toString)
//    .and[Scope](ConfidenceLevel.L250.toString)
//    .and[Scope](ConfidenceLevel.L500.toString)
//    .format

  private val scopeWrites = Json.writes[Scope]
  private val scopeReads = (
    (__ \ "key").read[String] and
      (__ \ "name").read[String] and
      (__ \ "description").read[String] and
      (__ \ "confidenceLevel").readNullable[ConfidenceLevel]
    ) { Scope }

  implicit val scopeFormats = OFormat(scopeReads, scopeWrites)

  override def writes(scope: Scope): JsObject = {
    scopeWrites.writes(scope)
  }

  override def reads(json: JsValue): JsResult[Scope] = {
    scopeReads.reads(json)
  }
}
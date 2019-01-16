/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.models

import play.api.libs.json.Json
import uk.gov.hmrc.models.ConfidenceLevel.ConfidenceLevel

case class ScopeData(key: String, name: String, description: String, confidenceLevel: Option[ConfidenceLevel] = None) {
  require(key.trim.nonEmpty, s"scope key is required")
  require(name.trim.nonEmpty, s"scope name is required")
  require(description.trim.nonEmpty, s"scope description is required")
}

object ScopeData {
  implicit val format1 = Json.format[ScopeData]
}

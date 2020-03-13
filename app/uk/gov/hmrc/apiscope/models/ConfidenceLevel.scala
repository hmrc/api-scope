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

object ConfidenceLevel extends Enumeration {
  type ConfidenceLevel = Value

  val L50, L100, L200, L300, L500 = Value

  private val fromInt = Map(
    50 -> L50,
    100 -> L100,
    200 -> L200,
    300 -> L300
  )
  private val toInt = fromInt.map(_.swap)


  val errorMessage = s"confidence level must be one of: ${fromInt.keys.toSeq.sorted.mkString(", ")}"

  implicit val reads = Reads[ConfidenceLevel] { json =>
    json.asOpt[Int].flatMap(fromInt.get)
      .map(JsSuccess(_))
      .getOrElse(JsError(errorMessage))
  }

  implicit val writes = Writes[ConfidenceLevel] { LoC => JsNumber(toInt(LoC)) }
}

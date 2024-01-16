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

sealed abstract class ConfidenceLevel(val value: Int)

object ConfidenceLevel {

  case object L50  extends ConfidenceLevel(50)
  case object L200 extends ConfidenceLevel(200)
  case object L250 extends ConfidenceLevel(250)
  case object L500 extends ConfidenceLevel(500)
  val values: Set[ConfidenceLevel]             = Set(L50, L200, L250, L500)
  def apply(int: Int): Option[ConfidenceLevel] = ConfidenceLevel.values.find(_.value == int)

  def unsafeApply(int: Int): ConfidenceLevel = apply(int).getOrElse(throw new RuntimeException(s"$int is not a valid Confidence Level"))

  private val reads: Reads[ConfidenceLevel] = {
    case JsNumber(number) => apply(number.intValue).fold[JsResult[ConfidenceLevel]] { JsError(s"$number is not a valid Confidence Level") }(JsSuccess(_))
    case e                => JsError(s"Cannot parse Confidence Level from '$e'")
  }

  private val writes: Writes[ConfidenceLevel] = level => JsNumber(level.value)

  implicit val format: Format[ConfidenceLevel] = Format(reads, writes)

}

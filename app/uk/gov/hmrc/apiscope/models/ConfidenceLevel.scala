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

import enumeratum.values.{IntEnum, IntEnumEntry, IntPlayJsonValueEnum}

import scala.collection.immutable

sealed abstract class ConfidenceLevel(val value: Int) extends IntEnumEntry

object ConfidenceLevel extends IntEnum[ConfidenceLevel] with IntPlayJsonValueEnum[ConfidenceLevel] {
  val values: immutable.IndexedSeq[ConfidenceLevel] = findValues

  def fromDbLevel(dbLevel: Int): ConfidenceLevel = {
    dbLevel match {
      case 50 => L50
      case 100 => L200
      case 200 => L200
      case 250 => L250
      case 300 => L200
      case 500 => L500
    }
  }

  case object L50 extends ConfidenceLevel(50)
  case object L200 extends ConfidenceLevel(200)
  case object L250 extends ConfidenceLevel(250)
  case object L500 extends ConfidenceLevel(500)
}

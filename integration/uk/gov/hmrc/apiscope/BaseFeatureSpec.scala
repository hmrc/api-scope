/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apiscope

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

abstract class BaseFeatureSpec extends FeatureSpec
    with GuiceOneServerPerSuite
    with GivenWhenThen
    with ScalaFutures
    with BeforeAndAfter
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with Matchers {

  val serverUrl = s"http://localhost:$port"
}

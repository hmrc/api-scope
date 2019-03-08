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

package it.uk.gov.hmrc

import it.uk.gov.hmrc.stubs.StubConfiguration
import org.scalatest.TestSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Mode}
import uk.gov.hmrc.http.HeaderCarrier

trait AppUsingStubs extends GuiceOneAppPerSuite with StubConfiguration {
  this: TestSuite =>

  override def fakeApplication(): Application =
    GuiceApplicationBuilder().configure(stubConfiguration()).in(Mode.Test).build()

  override def stubConfiguration(configuration: Map[String, Any]) = {
    configuration ++ Map(
      "microservice.services.service-locator.enabled" -> true,
      "publishApiDefinition" -> true
    )
  }

  implicit def mat: akka.stream.Materializer = app.injector.instanceOf[akka.stream.Materializer]

  implicit val hc: HeaderCarrier = HeaderCarrier()
}

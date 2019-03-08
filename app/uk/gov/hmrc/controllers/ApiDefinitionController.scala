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

package uk.gov.hmrc.controllers

import controllers.Assets
import enumeratum.{EnumEntry, PlayEnum}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.views.txt
import uk.gov.hmrc.play.bootstrap.controller.BackendController

@Singleton
class ApiDefinitionController @Inject()(config: ApiDefinitionConfiguration, assets: Assets, cc: ControllerComponents)
  extends BackendController(cc) {

  def definition = Action {
    if(config.publishApiDefinition) {
      Ok(txt.definition(config)).withHeaders(CONTENT_TYPE -> JSON)
    } else {
      NotFound
    }
  }

  def raml(version: String, file: String) = Action {
    if(config.publishApiDefinition) {
      Ok(txt.application())
    } else {
      NotFound
    }
  }
}

case class ApiDefinitionConfiguration(publishApiDefinition: Boolean, versions: Seq[ApiVersionConfiguration])

case class ApiVersionConfiguration(version: String, status: ApiStatusType, access: ApiAccess, endpointsEnabled: Boolean)

sealed trait ApiStatusType extends EnumEntry

object ApiStatusType extends PlayEnum[ApiStatusType] {
  val values = findValues

  final case object ALPHA extends ApiStatusType

  final case object BETA extends ApiStatusType

  final case object STABLE extends ApiStatusType

  final case object DEPRECATED extends ApiStatusType

  final case object RETIRED extends ApiStatusType

}

sealed trait ApiAccessType extends EnumEntry

object ApiAccessType extends PlayEnum[ApiAccessType] {
  val values = findValues

  final case object PRIVATE extends ApiAccessType

  final case object PUBLIC extends ApiAccessType

}

case class ApiAccess(`type`: ApiAccessType, whitelistedApplicationIds: Option[Seq[String]], isTrial: Option[Boolean] = None)
object ApiAccess {
  implicit val format: OFormat[ApiAccess] = Json.format[ApiAccess]
}


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

package uk.gov.hmrc.config

import javax.inject.{Inject, Provider}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.controllers._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class ConfigurationModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[ApiDefinitionConfiguration].toProvider[ApiDefinitionConfigurationProvider]
    )
  }
}

class ApiDefinitionConfigurationProvider @Inject()(runModeConfiguration: Configuration, servicesConfig: ServicesConfig)
  extends Provider[ApiDefinitionConfiguration] {

  override def get() = {
    val versionRegex = "\\d+\\.\\d+".r

    def versionConfiguration(version: String) = {
      val accessType = runModeConfiguration.getOptional[String](s"api.$version.access.type")
        .flatMap(ApiAccessType.withNameOption)
        .getOrElse(ApiAccessType.PUBLIC)

      val status = runModeConfiguration.getOptional[String](s"api.$version.status")
        .flatMap(ApiStatusType.withNameOption)
        .getOrElse(ApiStatusType.ALPHA)

      val whitelistedApplicationIds = runModeConfiguration.getOptional[Seq[String]](s"api.$version.access.whitelistedApplicationIds")

      val isTrial = runModeConfiguration.getOptional[Boolean](s"api.$version.access.isTrial")

      val endpointsEnabled = runModeConfiguration.getOptional[Boolean](s"api.$version.endpointsEnabled").getOrElse(true)

      ApiVersionConfiguration(version, status, ApiAccess(accessType, whitelistedApplicationIds, isTrial), endpointsEnabled)
    }

    val apiVersionConfigurations = (for {
      api <- runModeConfiguration.getOptional[Configuration]("api")
      versions = api.keys.flatMap(key => versionRegex.findFirstIn(key).toSet).toSeq.sorted
    } yield {
      versions.map(versionConfiguration)
    }).getOrElse(Seq.empty)

    val publishApiDefinition = runModeConfiguration.getOptional[Boolean]("publishApiDefinition").getOrElse(false)

    ApiDefinitionConfiguration(publishApiDefinition, apiVersionConfigurations)
  }
}



/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.Singleton
import com.typesafe.config.Config
import javax.inject.Inject
import play.api.mvc.EssentialFilter
import play.api.{Application, Configuration}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}

@Singleton
class ApiScopeGlobal @Inject()(
    val loggingFilter: MicroserviceLoggingFilter,
    val microserviceAuditFilter: MicroserviceAuditFilter,
    val auditConnector: AuditConnector
                              ) extends DefaultMicroserviceGlobal with RunMode {

  override def authFilter: Option[EssentialFilter] = None

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")
}

@Singleton
class ControllerConfiguration @Inject()(conf: Configuration) extends ControllerConfig {
  lazy val controllerConfigs: Config = conf.underlying.atKey("controllers")
}

@Singleton
class MicroserviceAuditFilter @Inject()(
  config: ControllerConfig,
  val auditConnector: AuditConnector
                                       ) extends AuditFilter with AppName with MicroserviceFilterSupport {
  override def controllerNeedsAuditing(controllerName: String) = config.paramsForController(controllerName).needsAuditing
}

@Singleton
class MicroserviceLoggingFilter @Inject()(config: ControllerConfig) extends LoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String) = config.paramsForController(controllerName).needsLogging
}

@Singleton
class MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig("auditing")
}

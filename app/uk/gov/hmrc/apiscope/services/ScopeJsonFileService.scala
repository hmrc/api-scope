/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apiscope.services

import javax.inject.{Inject, Singleton}
import play.api.Logger.logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.apiscope.models.Scope
import uk.gov.hmrc.apiscope.repository.ScopeRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class ScopeJsonFileService @Inject()(scopeRepository: ScopeRepository,
                                     fileReader: ScopeJsonFileReader)(implicit val ec: ExecutionContext) {

  private def saveScopes(scopes: Seq[Scope]): Future[Seq[Scope]] =
    Future.sequence(scopes.map(scopeRepository.save))

  try {
    fileReader.readFile.map(s => Json.parse(s).validate[Seq[Scope]] match {
      case JsSuccess(scopes: Seq[Scope], _) =>
        logger.info(s"Inserting ${scopes.size} Scopes from bundled file")
        saveScopes(scopes)
      case JsError(errors) => logger.error("Unable to parse JSON into Scopes", errors.mkString("; "))
    })
  } catch {
    case _: java.nio.file.NoSuchFileException => logger.info("No Scopes file found to process")
    case NonFatal(e) =>
      logger.error("Scopes file does not contain valid JSON", e)
  }
}

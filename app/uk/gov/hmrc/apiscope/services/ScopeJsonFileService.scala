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

import play.api.Logger.logger
import play.api.libs.json.{JsArray, JsObject, Json}
import uk.gov.hmrc.apiscope.models.Scope
import uk.gov.hmrc.apiscope.repository.ScopeRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ScopeJsonFileService @Inject()(scopeRepository: ScopeRepository,
                                     fileReader: ScopeJsonFileReader)(implicit val ec: ExecutionContext) {

  private def saveScopes(scopes: Seq[Scope]): Future[Seq[Scope]] =
    Future.sequence(scopes.map(scopeRepository.save))

  Try(Json.parse(fileReader.readFile) match {
    case parsed: JsArray => parsed.as[Seq[Scope]]
    case parsed: JsObject => new JsArray().append(parsed).as[Seq[Scope]]
  }).map(saveScopes)
    .recover {
      case e: Exception =>
        logger.error("Unable to parse JSON scopes file:", e)
        None
    }
}

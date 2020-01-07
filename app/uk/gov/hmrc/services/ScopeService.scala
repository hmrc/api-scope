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

package uk.gov.hmrc.services

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.models.Scope
import uk.gov.hmrc.repository.ScopeRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScopeService @Inject()(scopeRepository: ScopeRepository)(implicit val ec: ExecutionContext) {

  def saveScopes(scopes: Seq[Scope]): Future[Seq[Scope]] =
    Future.sequence(scopes.map(scopeRepository.save))

  def fetchScope(key: String) : Future[Option[Scope]] =
    scopeRepository.fetch(key)

  def fetchScopes(keys: Set[String]) : Future[Seq[Scope]] = {
    val futures = keys.map(scopeRepository.fetch)
    Future.sequence(futures).map(_.flatten.toSeq)
  }

  def fetchAll: Future[Seq[Scope]] =
    scopeRepository.fetchAll()

}

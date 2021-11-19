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

package uk.gov.hmrc.apiscope.repository

import javax.inject.{Inject, Singleton}

import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.Cursor
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.json.Json._
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.apiscope.models.Scope

private object ScopeFormats {
  implicit val objectIdFormats = ReactiveMongoFormats.objectIdFormats
  implicit val scopeFormat = Json.format[Scope]
}

@Singleton
 class ScopeRepository @Inject()(mongo: ReactiveMongoComponent)(implicit val ec: ExecutionContext)
  extends ReactiveRepository[Scope, BSONObjectID]("scope", mongo.mongoConnector.db,
    ScopeFormats.scopeFormat, ReactiveMongoFormats.objectIdFormats) {


  ensureIndex("key", "keyIndex")

  def save(scope: Scope) : Future[Scope] =  {
    collection
      .update(false).one(Json.obj("key" -> scope.key), scope, upsert = true)
      .map(_ => scope)
  }

  private def ensureIndex(field: String, indexName: String, isUnique: Boolean = true, isBackground: Boolean = true): Future[Boolean] = {
    def createIndex = Index(
      key = Seq(field -> IndexType.Ascending),
      name = Some(indexName),
      unique = isUnique,
      background = isBackground
    )

    collection.indexesManager.ensure(createIndex)
  }

  def fetch(key: String): Future[Option[Scope]] =  {
    collection.find[JsObject, Scope](Json.obj("key" -> key), None).one[Scope]. map {
      case Some(s) => Some(s)
      case None =>
        logger.info(s"The scope $key doesn't exist")
        None
    }
  }

  def fetchAll(): Future[Seq[Scope]] =  {
    collection.find[JsObject, Scope](Json.obj(), None).cursor[Scope]().collect[Seq](Int.MaxValue,Cursor.FailOnError[Seq[Scope]]())
  }

}

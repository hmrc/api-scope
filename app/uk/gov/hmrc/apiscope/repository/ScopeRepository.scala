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

package uk.gov.hmrc.apiscope.repository

import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, ReturnDocument}
import org.mongodb.scala.{MongoClient, MongoCollection}
import play.api.Logger
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.apiscope.models.Scope
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, CollectionFactory, PlayMongoRepository}

import javax.inject.{Inject, Singleton}
import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}


object ScopeFormats {
  implicit val scopeFormat:OFormat[Scope] = Json.format[Scope]
}

@Singleton
 class ScopeRepository @Inject()(mongoComponent: MongoComponent)(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[Scope](
    mongoComponent = mongoComponent,
    collectionName = "scope",
    domainFormat = ScopeFormats.scopeFormat,
    indexes = Seq(IndexModel(ascending("key"),
      IndexOptions()
        .name("keyIndex")
        .background(true)
        .unique(true))),
    replaceIndexes = true
  ) {
  private val logger = Logger(this.getClass)

  override lazy val collection: MongoCollection[Scope] =
    CollectionFactory
      .collection(mongoComponent.database, collectionName, domainFormat)
      .withCodecRegistry(
        fromRegistries(
          fromCodecs(
            Codecs.playFormatCodec(domainFormat)
          ),
          MongoClient.DEFAULT_CODEC_REGISTRY
        )
      )

  def save(scope: Scope) : Future[Scope] =  {
    var updateSeq = Seq(
      set("key", Codecs.toBson(scope.key)),
      set("name", Codecs.toBson(scope.name)),
      set("description", Codecs.toBson(scope.description)))
    scope.confidenceLevel match {
      case Some(value) =>
        logger.info(s"confidenceLevel value id ${value} and value enumeration ${value.value}")
        updateSeq = updateSeq :+ set("confidenceLevel", Codecs.toBson(value))
      case None => Future.successful(None)
    }
    logger.info(s"updateSeq: $updateSeq")
    collection.findOneAndUpdate(equal("key", Codecs.toBson(scope.key)),
      update = combine(updateSeq: _*),
      options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
    ).map(_.asInstanceOf[Scope]).head()
  }

  def fetch(key: String): Future[Option[Scope]] =  {
    collection.find(equal("key", key)).headOption()
      .flatMap {
        case Some(scope) => Future.successful(Some(scope))
        case None =>
          logger.info(s"The scope $key doesn't exist")
          Future.successful(None)
      }
  }

  def fetchAll(): Future[Seq[Scope]] =  {
    collection.find().toFuture()
  }

}

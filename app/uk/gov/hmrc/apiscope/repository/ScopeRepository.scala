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

package uk.gov.hmrc.apiscope.repository

import javax.inject.{Inject, Singleton}
import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}

import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, ReturnDocument}

import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import uk.gov.hmrc.apiscope.models.{ConfidenceLevel, Scope}

private object ScopeFormats {

  implicit val scopeRead: Reads[Scope] = (
    (JsPath \ "key").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "confidenceLevel").readNullable[Int]
        .map[Option[ConfidenceLevel]](_ match {
          case None      => None
          case Some(50)  => Some(ConfidenceLevel.L50)
          case Some(100) => Some(ConfidenceLevel.L200)
          case Some(200) => Some(ConfidenceLevel.L200)
          case Some(250) => Some(ConfidenceLevel.L250)
          case Some(300) => Some(ConfidenceLevel.L200)
          case Some(500) => Some(ConfidenceLevel.L500)
          case Some(i)   => throw new RuntimeException(s"Bad data in confidence level of $i")
        })
  )(Scope.apply _)

  implicit val scopeWrites: OWrites[Scope] = Json.writes[Scope]
  implicit val scopeFormat: OFormat[Scope] = OFormat(scopeRead, scopeWrites)
}

@Singleton
class ScopeRepository @Inject() (mongoComponent: MongoComponent)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[Scope](
      mongoComponent = mongoComponent,
      collectionName = "scope",
      domainFormat = ScopeFormats.scopeFormat,
      indexes = Seq(IndexModel(
        ascending("key"),
        IndexOptions()
          .name("keyIndex")
          .background(true)
          .unique(true)
      )),
      replaceIndexes = true,
      extraCodecs = Seq(Codecs.playFormatCodec(ScopeFormats.scopeFormat))
    ) {
  private val logger = Logger(this.getClass)

  def save(scope: Scope): Future[Scope] = {
    var updateSeq = Seq(
      set("key", Codecs.toBson(scope.key)),
      set("name", Codecs.toBson(scope.name)),
      set("description", Codecs.toBson(scope.description))
    )
    scope.confidenceLevel match {
      case Some(value) =>
        logger.info(s"confidenceLevel value id ${value} and value enumeration ${value.value}")
        updateSeq = updateSeq :+ set("confidenceLevel", Codecs.toBson(value))
      case None        => Future.successful(None)
    }
    logger.info(s"updateSeq: $updateSeq")
    collection.findOneAndUpdate(
      equal("key", Codecs.toBson(scope.key)),
      update = combine(updateSeq: _*),
      options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
    ).map(_.asInstanceOf[Scope]).head()
  }

  def fetch(key: String): Future[Option[Scope]] = {
    collection.find(equal("key", key)).headOption()
      .flatMap {
        case Some(scope) => Future.successful(Some(scope))
        case None        =>
          logger.info(s"The scope $key doesn't exist")
          Future.successful(None)
      }
  }

  def fetchAll(): Future[Seq[Scope]] = {
    collection.find().toFuture()
  }

}

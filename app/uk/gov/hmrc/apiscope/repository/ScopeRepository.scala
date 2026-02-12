/*
 * Copyright 2024 HM Revenue & Customs
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
import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import org.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, ReturnDocument}

import play.api.Logger
import play.api.libs.functional.syntax.*
import play.api.libs.json.{Reads, *}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.play.json.Mappings

import uk.gov.hmrc.apiscope.models.Scope

private object ScopeFormats {

  private def fromIntIncludingOldValues(level: Int): Try[ConfidenceLevel] = level match {
    case 600 => Success(ConfidenceLevel.L600)
    case 500 => Success(ConfidenceLevel.L500)
    case 300 => Success(ConfidenceLevel.L200)
    case 250 => Success(ConfidenceLevel.L250)
    case 200 => Success(ConfidenceLevel.L200)
    case 100 => Success(ConfidenceLevel.L200)
    case 50  => Success(ConfidenceLevel.L50)
    case _   => Failure(throw new NoSuchElementException(s"Bad data in confidence level of $level"))
  }

  private val mapping = Mappings.mapTry[Int, ConfidenceLevel](fromIntIncludingOldValues, _.level)

  given Format[ConfidenceLevel] = mapping.jsonFormat

  given mongoScopeFmt: OFormat[Scope] = Json.format[Scope]
}

@Singleton
class ScopeRepository @Inject() (mongoComponent: MongoComponent)(using ExecutionContext)
    extends PlayMongoRepository[Scope](
      mongoComponent = mongoComponent,
      collectionName = "scope",
      domainFormat = ScopeFormats.mongoScopeFmt,
      indexes = Seq(IndexModel(
        ascending("key"),
        IndexOptions()
          .name("keyIndex")
          .background(true)
          .unique(true)
      )),
      replaceIndexes = true
    ) {
  private val logger                 = Logger(this.getClass)
  override lazy val requiresTtlIndex = false

  def save(scope: Scope): Future[Scope] = {
    val updateSeq = Seq(
      set("key", Codecs.toBson(scope.key)),
      set("name", Codecs.toBson(scope.name)),
      set("description", Codecs.toBson(scope.description))
    ) ++
      (scope.confidenceLevel.fold[Seq[Bson]](
        Seq.empty // or Seq(unset("confidenceLevel")) to set this too
      )(value => {
        logger.info(s"confidenceLevel value id ${value} and value enumeration ${value.level}")
        Seq(
          set("confidenceLevel", Codecs.toBson(value))
        )
      }))

    logger.info(s"updateSeq: $updateSeq")

    collection.findOneAndUpdate(
      equal("key", Codecs.toBson(scope.key)),
      update = combine(updateSeq*),
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

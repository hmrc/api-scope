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

import org.mongodb.scala.bson.{BsonDocument, BsonString}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.apiscope.models.ConfidenceLevel._
import uk.gov.hmrc.apiscope.models.Scope
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.util.AsyncHmrcSpec
import org.mongodb.scala.Document
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

import scala.concurrent.ExecutionContext.Implicits.global


class ScopeRepositorySpec extends AsyncHmrcSpec
  with BeforeAndAfterEach with BeforeAndAfterAll
  with GuiceOneAppPerSuite
  with MongoApp[Scope]
  with Eventually
   {

  val scope1 = Scope("key1", "name1", "description1")
  val scope2 = Scope("key2", "name2", "description2", confidenceLevel = Some(L200))
  val repo = repository.asInstanceOf[ScopeRepository]

  override protected def repository: PlayMongoRepository[Scope] = app.injector.instanceOf[ScopeRepository]

  private def getIndexes(): List[BsonDocument] = {
    await(repo.collection.listIndexes().map(toBsonDocument).toFuture().map(_.toList))
  }

  private def toBsonDocument(index: Document): BsonDocument = {
   val d = index.toBsonDocument
   // calling index.remove("v") leaves index untouched - convert to BsonDocument first..
   d.remove("v") // version
   d.remove("ns")
   d
  }

  "saveScope" should {
    "create scopes and retrieve them from database" in {
      await(repo.save(scope1))
      await(repo.save(scope2))

      await(repo.fetch(scope1.key)).get shouldBe scope1
      await(repo.fetch(scope2.key)).get shouldBe scope2
    }

    "update a scope" in {
      await(repo.save(scope1))
      await(repo.save(scope2))

      val updatedScope1 = Scope(scope1.key, "updatedName1", "updatedDescription1")
      val updatedScope2 = Scope(scope2.key, "updatedName2", "updatedDescription2", confidenceLevel = Some(L50))

      await(repo.save(updatedScope1))
      await(repo.save(updatedScope2))

      await(repo.fetch(scope1.key)).get shouldEqual updatedScope1
      await(repo.fetch(scope2.key)).get shouldEqual updatedScope2
    }
  }

  "fetchAll" should {
    "retrieve all the scopes from database" in {

      await(repo.save(scope1))
      await(repo.save(scope2))

      val allScopes = await(repo.fetchAll())

      allScopes should contain allOf(scope1, scope2)
    }
  }

  "The indexes in the 'scope' collection" should {
    "have all the indexes" in {

      val indexes = getIndexes()
      indexes.size mustEqual 2
      indexes.map(ind => ind.get("name"))  contains  BsonString("keyIndex")
      indexes.map(ind => ind.get("key")) contains BsonDocument("key" -> 1)
    }
  }
}

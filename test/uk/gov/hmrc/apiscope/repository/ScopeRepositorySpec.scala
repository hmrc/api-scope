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

import scala.concurrent.ExecutionContext.Implicits.global

import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala.Document
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.{BsonDocument, BsonString}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Format, JsObject, Json}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import uk.gov.hmrc.apiscope.models.Scope
import uk.gov.hmrc.util.AsyncHmrcSpec

class ScopeRepositorySpec extends AsyncHmrcSpec
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite
    with DefaultPlayMongoRepositorySupport[Scope] {

  val basicScope: Scope         = Scope("key1", "name1", "description1")
  val scopeConfidence200: Scope = Scope("key2", "name2", "description2", confidenceLevel = Some(ConfidenceLevel.L200))
  val scopeConfidence500: Scope = Scope("key3", "name3", "description3", confidenceLevel = Some(ConfidenceLevel.L500))

  override val repository: ScopeRepository    = app.injector.instanceOf[ScopeRepository]
  override implicit lazy val app: Application = appBuilder.build()

  private def getIndexes(): List[BsonDocument] = {
    await(repository.collection.listIndexes().map(toBsonDocument).toFuture().map(_.toList))
  }

  def insertRaw(raw: JsObject) = {
    val db = mongoComponent.database.withCodecRegistry(
      CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(Codecs.playFormatCodec[Scope](repository.domainFormat)),
        CodecRegistries.fromCodecs(Codecs.playFormatCodec[JsObject](implicitly[Format[JsObject]])),
        DEFAULT_CODEC_REGISTRY
      )
    )
    await(db.getCollection[JsObject](repository.collectionName).insertOne(raw).toFuture())
  }

  private def toBsonDocument(index: Document): BsonDocument = {
    val d = index.toBsonDocument
    // calling index.remove("v") leaves index untouched - convert to BsonDocument first..
    d.remove("v") // version
    d.remove("ns")
    d
  }

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )

  "saveScope" should {
    "create scopes and retrieve them from database" in {
      await(repository.save(basicScope))
      await(repository.save(scopeConfidence200))

      await(repository.fetch(basicScope.key)).get shouldBe basicScope
      await(repository.fetch(scopeConfidence200.key)).get shouldBe scopeConfidence200
    }

    "create scope with ConfidenceLevel of 500 and retrieve from database" in {
      await(repository.save(scopeConfidence500))

      await(repository.fetch(scopeConfidence500.key)).get shouldBe scopeConfidence500
    }

    "create scope with ConfidenceLevel of 250 and retrieve from database" in {
      await(repository.save(basicScope.copy(confidenceLevel = Some(ConfidenceLevel.L250))))

      await(repository.fetch(basicScope.key)).head.confidenceLevel shouldBe Some(ConfidenceLevel.L250)
    }

    "create scope with ConfidenceLevel of 600 and retrieve from database" in {
      await(repository.save(basicScope.copy(confidenceLevel = Some(ConfidenceLevel.L600))))

      await(repository.fetch(basicScope.key)).head.confidenceLevel shouldBe Some(ConfidenceLevel.L600)
    }

    "update a scope" in {
      await(repository.save(basicScope))
      await(repository.save(scopeConfidence200))

      val updatedScope1 = Scope(basicScope.key, "updatedName1", "updatedDescription1")
      val updatedScope2 = Scope(scopeConfidence200.key, "updatedName2", "updatedDescription2", confidenceLevel = Some(ConfidenceLevel.L50))

      await(repository.save(updatedScope1))
      await(repository.save(updatedScope2))

      await(repository.fetch(basicScope.key)).get shouldEqual updatedScope1
      await(repository.fetch(scopeConfidence200.key)).get shouldEqual updatedScope2
    }
  }
  "read a scope" should {
    val scopeName        = "some scope name"
    val scopeKey         = "read:some-scope-key"
    val scopeDescription = "some scope description"
    "map deprecated confidence level 100 to supported 200" in {

      val outdatedScope: JsObject =
        Json.obj("key" -> scopeKey, "name" -> scopeName, "description" -> scopeDescription, "confidenceLevel" -> 100)
      insertRaw(outdatedScope)

      val scopesRead = await(repository.fetchAll())
      scopesRead.size shouldEqual 1
      scopesRead.head.confidenceLevel shouldEqual Some(ConfidenceLevel.L200)
    }

    "map deprecated confidence level 300 to supported 200" in {
      val outdatedScope: JsObject =
        Json.obj("key" -> scopeKey, "name" -> scopeName, "description" -> scopeDescription, "confidenceLevel" -> 300)
      insertRaw(outdatedScope)

      val scopesRead = await(repository.fetchAll())
      scopesRead.size shouldEqual 1
      scopesRead.head.confidenceLevel shouldEqual Some(ConfidenceLevel.L200)
    }

    "handle an unsupported confidence level" in {
      val invalidScope: JsObject =
        Json.obj("key" -> scopeKey, "name" -> scopeName, "description" -> scopeDescription, "confidenceLevel" -> 666)
      insertRaw(invalidScope)

      val e: RuntimeException = intercept[RuntimeException] {
        await(repository.fetch(scopeKey))
      }
      e.getMessage should include("Bad data in confidence level of 666")
    }

    "handle a key which cannot be found" in {
      val scopesRead: Option[Scope] = await(repository.fetch("some-non-existent-key"))
      scopesRead shouldBe Option.empty
    }
  }

  "fetchAll" should {
    "retrieve all the scopes from database" in {
      await(repository.save(basicScope))
      await(repository.save(scopeConfidence200))

      val allScopes = await(repository.fetchAll())

      allScopes.should(contain.allOf(basicScope, scopeConfidence200))
    }
  }

  "The indexes in the 'scope' collection" should {
    "have all the indexes" in {
      val indexes = getIndexes()

      indexes.size mustEqual 2

      indexes.map(ind => ind.get("name")) contains BsonString("keyIndex")
      indexes.map(ind => ind.get("key")) contains BsonDocument("key" -> 1)
    }
  }
}

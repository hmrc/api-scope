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

package unit.repository

import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import uk.gov.hmrc.models.ConfidenceLevel._
import uk.gov.hmrc.models.Scope
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}
import uk.gov.hmrc.play.http.metrics.NoopMetrics
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.repository.ScopeRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ScopeRepositorySpec extends UnitSpec
  with ScalaFutures with MongoSpecSupport
  with BeforeAndAfterEach with BeforeAndAfterAll
  with Eventually {

  private val reactiveMongoComponent = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = mongoConnectorForTest }

  private val repository = createRepository

  val scope1 = Scope("key1", "name1", "description1")
  val scope2 = Scope("key2", "name2", "description2", confidenceLevel = Some(L200))

  private def createRepository = new ScopeRepository(reactiveMongoComponent) {
    override lazy val metrics = NoopMetrics
  }

  private def dropRepository(repo: ScopeRepository) = {
    await(repo.drop)
  }

  private def getIndexes(repo: ScopeRepository): List[Index] = {
    val indexesFuture = repo.collection.indexesManager.list()
    await(indexesFuture)
  }

  override def beforeEach() {
    dropRepository(repository)
    await(repository.ensureIndexes)
  }

  override protected def afterAll() {
    dropRepository(repository)
  }

  "saveScope" should {
    "create scopes and retrieve them from database" in {
      await(repository.save(scope1))
      await(repository.save(scope2))

      await(repository.fetch(scope1.key)).get shouldBe scope1
      await(repository.fetch(scope2.key)).get shouldBe scope2
    }

    "update a scope" in {
      await(repository.save(scope1))
      await(repository.save(scope2))

      val updatedScope1 = Scope(scope1.key, "updatedName1", "updatedDescription1")
      val updatedScope2 = Scope(scope2.key, "updatedName2", "updatedDescription2", confidenceLevel = Some(L50))

      await(repository.save(updatedScope1))
      await(repository.save(updatedScope2))

      await(repository.fetch(scope1.key)).get shouldEqual updatedScope1
      await(repository.fetch(scope2.key)).get shouldEqual updatedScope2
    }
  }

  "fetchAll" should {
    "retrieve all the scopes from database" in {

      await(repository.save(scope1))
      await(repository.save(scope2))

      val allScopes = await(repository.fetchAll())

      allScopes should contain theSameElementsAs Seq(scope1, scope2)
    }
  }

  "The indexes in the 'scope' collection" should {
    "have all the indexes" in {

      import scala.concurrent.duration._

      val expectedIndexes = List(
        Index(key = Seq("key" -> IndexType.Ascending), name = Some("keyIndex"), unique = true, background = true, version = Some(2)),
        Index(key = Seq("_id" -> IndexType.Ascending), name = Some("_id_"), unique = false, background = false, version = Some(2))
      )

      val repo: ScopeRepository = createRepository

      eventually(timeout(4.seconds), interval(100.milliseconds)) {
        getIndexes(repo) shouldBe expectedIndexes
      }

      dropRepository(repo)
    }
  }
}

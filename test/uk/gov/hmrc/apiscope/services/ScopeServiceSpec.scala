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

package uk.gov.hmrc.apiscope.services

import org.mockito.BDDMockito.given
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.apiscope.models.ConfidenceLevel._
import uk.gov.hmrc.apiscope.models.Scope
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apiscope.repository.ScopeRepository
import uk.gov.hmrc.apiscope.services.ScopeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ScopeServiceSpec extends UnitSpec with MockitoSugar with ScalaFutures {

  val scope1 = Scope("key1", "name1", "description1")
  val scope2 = Scope("key2", "name2", "description2", confidenceLevel = Some(L200))

  trait Setup {
    val mockScopeRepository = mock[ScopeRepository]
    
    val underTest = new ScopeService(mockScopeRepository)
  }

  "saveScopes" should {
    "save scopes in repository" in new Setup {

      val scopes = Seq(scope1, scope2)
      given(mockScopeRepository.save(scope1)).willReturn(Future.successful(scope1))
      given(mockScopeRepository.save(scope2)).willReturn(Future.successful(scope2))

      val result = await(underTest.saveScopes(scopes))

      result shouldEqual scopes
      verify(mockScopeRepository).save(scope1)
      verify(mockScopeRepository).save(scope2)
    }

    "fail when one scope fails" in new Setup {

      val scopes = Seq(scope1, scope2)

      when(mockScopeRepository.save(scope1)).thenReturn(Future.failed(new RuntimeException("Can not save scope")))

      val future = underTest.saveScopes(scopes)

      whenReady(future.failed) { ex =>
        ex shouldBe an [RuntimeException]
      }
    }
  }

  "fetchScope" should {
    "fetch a scope from the repository" in new Setup {

      when(mockScopeRepository.fetch(scope1.key)).thenReturn(Some(scope1))

      val result = await(underTest.fetchScope(scope1.key))

      result shouldEqual Some(scope1)
    }

    "fail when repository returns an error" in new Setup {

      when(mockScopeRepository.fetch(scope1.key)).thenReturn(Future.failed(new RuntimeException()))

      val future = underTest.fetchScope(scope1.key)

      whenReady(future.failed) { ex =>
        ex shouldBe an [RuntimeException]
      }
    }
  }

  "fetchAll" should {
    "Return all the scopes from the repository" in new Setup {

      val scopes = Seq(scope1, scope2).toList

      when(mockScopeRepository.fetchAll()).thenReturn(scopes)

      val result = await(underTest.fetchAll)

      result should contain theSameElementsAs scopes
    }

    "fail when repository returns an error" in new Setup {

      when(mockScopeRepository.fetchAll()).thenReturn(Future.failed(new RuntimeException()))

      val future = underTest.fetchAll

      whenReady(future.failed) { ex =>
        ex shouldBe an [RuntimeException]
      }
    }
  }

  "fetchScopes" should {
    "Return the scopes from the repository" in new Setup {

      val scopes = Seq(scope1, scope2).toList

      when(mockScopeRepository.fetch("scope1")).thenReturn(Some(scope1))
      when(mockScopeRepository.fetch("scope2")).thenReturn(Some(scope2))
      when(mockScopeRepository.fetch("unknown")).thenReturn(None)

      val result = await(underTest.fetchScopes(Set("scope1", "scope2", "unknown")))

      result should contain theSameElementsAs scopes
    }

    "fail when one of the request fails" in new Setup {

      when(mockScopeRepository.fetch("scope1")).thenReturn(Some(scope1))
      when(mockScopeRepository.fetch("scope2")).thenReturn(Future.failed(new RuntimeException()))

      val future = underTest.fetchScopes(Set("scope1", "scope2"))

      whenReady(future.failed) { ex =>
        ex shouldBe an [RuntimeException]
      }
    }
  }

}

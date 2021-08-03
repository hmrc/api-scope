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

import uk.gov.hmrc.apiscope.models.ConfidenceLevel._
import uk.gov.hmrc.apiscope.models.Scope
import uk.gov.hmrc.apiscope.repository.ScopeRepository
import uk.gov.hmrc.util.AsyncHmrcSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class ScopeJsonFileServiceSpec extends AsyncHmrcSpec {

  val scope1 = Scope("key1", "name1", "description1")
  val scope2 = Scope("key2", "name2", "description2", confidenceLevel = Some(L200))
  val scope1AsJsonString = """{"key": "key1", "name": "name1", "description": "description1"}"""
  val bothScopesAsJsonString =
    """[{"key": "key1", "name": "name1", "description": "description1"},
      |{"key": "key2", "name": "name2", "description": "description2", "confidenceLevel": 200}]""".stripMargin

  trait Setup {
    val mockScopeRepository = mock[ScopeRepository]
    val mockFileReader = mock[ScopeJsonFileReader]
  }

  "saveScopes" should {
    "save single scope in repository" in new Setup {
      when(mockScopeRepository.save(scope1)).thenReturn(successful(scope1))
      when(mockFileReader.readFile).thenReturn(scope1AsJsonString)

      new ScopeJsonFileService(mockScopeRepository, mockFileReader)

      verify(mockScopeRepository).save(scope1)
      verify(mockFileReader).readFile
    }

    "save multiple scopes in repository" in new Setup {
      when(mockScopeRepository.save(scope1)).thenReturn(successful(scope1))
      when(mockScopeRepository.save(scope2)).thenReturn(successful(scope2))
      when(mockFileReader.readFile).thenReturn(bothScopesAsJsonString)

      new ScopeJsonFileService(mockScopeRepository, mockFileReader)

      verify(mockScopeRepository).save(scope1)
      verify(mockScopeRepository).save(scope2)
      verify(mockFileReader).readFile
    }

    "catch invalid JSON" in new Setup {
      when(mockFileReader.readFile).thenReturn("blah rubbish")

      new ScopeJsonFileService(mockScopeRepository, mockFileReader)

      verify(mockScopeRepository, never).save(scope1)
      verify(mockScopeRepository, never).save(scope2)
      verify(mockFileReader).readFile
    }
  }
}
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

package uk.gov.hmrc.apiscope.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.apiscope.models.ErrorCode._
import uk.gov.hmrc.apiscope.models.{Scope, ScopeData}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.apiscope.services.ScopeService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ScopeController @Inject()(scopeService: ScopeService, cc: ControllerComponents, playBodyParsers: PlayBodyParsers)(implicit val ec: ExecutionContext)
  extends BackendController(cc) {

  def createOrUpdateScope(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    handleRequest[Seq[ScopeData]](request) {
      scopeRequest => {
        Logger.info(s"Creating api-scope from request: $request")
        val scopes = scopeRequest.map {
          scopeData => Scope(scopeData.key, scopeData.name, scopeData.description, scopeData.confidenceLevel)
        }
        scopeService.saveScopes(scopes).map {
          scopes =>
            Logger.info(s"api-scope successfully created: $scopes")
            Ok(Json.toJson(scopes))
        } recover recovery
      }
    }
  }

  def fetchScope(key: String): Action[AnyContent] = Action.async {
    scopeService.fetchScope(key).map {
      case Some(scope) => Ok(Json.toJson(scope))
      case None => NotFound(error(SCOPE_NOT_FOUND, s"Scope not found with key: $key"))
    } recover recovery
  }

  def fetchScopes(scopes: String): Action[AnyContent] = Action.async {
    Logger.info(s"Fetching scopes: $scopes")
    val future: Future[Seq[Scope]] = scopes match {
      case "*" => scopeService.fetchAll
      case spaceSeparatedScopes =>
        scopeService.fetchScopes(spaceSeparatedScopes.split("\\s+").toSet)
    }

    future map(scopes => Ok(Json.toJson(scopes))) recover recovery
  }

  def validate: Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    handleRequest[Seq[ScopeData]](request) (_ => Future.successful(NoContent))
  }

  private def recovery: PartialFunction[Throwable, Result] = {
    case e =>
      Logger.error(s"An unexpected error occurred: ${e.getMessage}", e)
      InternalServerError(error(UNKNOWN_ERROR, "An unexpected error occurred"))
  }

}
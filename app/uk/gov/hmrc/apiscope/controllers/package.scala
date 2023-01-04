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

package uk.gov.hmrc.apiscope

import util.ApplicationLogger

import scala.concurrent.Future
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.apiscope.models.ErrorCode.{API_INVALID_JSON, SCOPE_NOT_FOUND}
import uk.gov.hmrc.apiscope.models.{ErrorCode, ErrorDescription, ErrorResponse}

package object controllers extends ApplicationLogger {


  private def validate[T](request:Request[JsValue])(implicit tjs: Reads[T]): Either[Result, JsResult[T]] = {
    try {
      Right(request.body.validate[T])
    } catch {
      case e: Throwable => Left(UnprocessableEntity(error(ErrorCode.INVALID_REQUEST_PAYLOAD, e.getMessage)))
    }
  }

  def handleRequest[T](request: Request[JsValue])(f: T => Future[Result])(implicit tjs: Reads[T]): Future[Result] = {

    val either: Either[Result, JsResult[T]] = validate(request)

    either.fold(Future.successful, {
      result => result.fold(
        errors => Future.successful(UnprocessableEntity(validationResult(errors))),
        entity => f(entity)
      )
    })
  }

  def validationResult(errors : Seq[(JsPath, Seq[JsonValidationError])]): JsValue = {

    val errs: Seq[ErrorDescription] = errors flatMap { case (jsPath, seqValidationError) =>
      seqValidationError map {
        validationError =>
          val isMissingPath = validationError.message == "error.path.missing"
          val message = if (isMissingPath) "element is missing" else validationError.message
          ErrorDescription(jsPath.toString, message)
      }
    }

    toJson(ErrorResponse(API_INVALID_JSON, "Json cannot be converted to API Scope", Some(errs)))
  }

  def recovery: PartialFunction[Throwable, Result] = {
    case nfe: NotFoundException => NotFound(error(SCOPE_NOT_FOUND, nfe.getMessage))
    case e =>
      logger.error(s"An unexpected error occurred: ${e.getMessage}", e)
      InternalServerError(error(ErrorCode.UNKNOWN_ERROR, "An unexpected error occurred"))
  }

  def error(code: ErrorCode.Value, message: String): JsValue = {
    toJson(ErrorResponse(code, message))
  }

}

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

package controllers

import forms.CreateProjectFormProvider
import javax.inject.Inject
import models.Project
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result, Results}
import renderer.Renderer
import services.ProjectsService
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class CreateProjectController @Inject()(
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer,
    projectsService: ProjectsService,
    formProvider: CreateProjectFormProvider
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request =>
      renderer.render("create-project.njk").map(Ok(_))
  }

  private def renderView(form: Form[Project],
                         status: Results.Status)(implicit request: Request[AnyContent]): Future[Result] = {

    val json = Json.obj(
      "form" -> form
    )

    renderer.render("create-project.njk", json).map(status(_))
  }

  def onSubmit: Action[AnyContent] = Action.async {
    implicit request => {
      form
        .bindFromRequest()
          .fold(
            formWithErrors => {
              println("There were errors")
              renderView(formWithErrors, Results.BadRequest)
            },
            value => {
              //Put in mongo
              println("Success")
              Future.successful(Ok)
            }
          )
    }
  }

}

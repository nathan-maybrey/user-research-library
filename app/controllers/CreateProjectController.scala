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
import models.{Phase, Project}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result, Results}
import renderer.Renderer
import repositories.ProjectsRepository
import services.ProjectsService
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class CreateProjectController @Inject()(
    val controllerComponents: MessagesControllerComponents,
    renderer: Renderer,
    projectsService: ProjectsService,
    projectsRepository: ProjectsRepository,
    formProvider: CreateProjectFormProvider
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request =>
      renderView(form, Results.Ok)
  }

  private def renderView(form: Form[Project],
                         status: Results.Status)(implicit request: Request[AnyContent]): Future[Result] = {

    val json = Json.obj(
      "form" -> form,
      "phases" -> phaseJsonList(form.data.get("phase"), phaseList)
    )

    renderer.render("create-project.njk", json).map(status(_))
  }

  def onSubmit: Action[AnyContent] = Action.async {
    implicit request => {
      form
        .bindFromRequest()
          .fold(
            formWithErrors => {
              renderView(formWithErrors, Results.BadRequest)
            },
            value => {
              projectsRepository.createProject(value)
                .map( _ => Redirect(routes.IndexController.onPageLoad()))
            }
          )
    }
  }

  //TODO: Move this list out of this file
  val phaseList: Seq[Phase] = Seq(
    Phase("Alpha"),
    Phase("Beta"),
    Phase("Discovery"),
    Phase("Live")
  )

  private def phaseJsonList(value: Option[String], phases: Seq[Phase]): Seq[JsObject] = {
    val phaseJsonList = phases.map {
      phase =>
        Json.obj(
          "text" -> phase.phase,
          "value" -> phase.phase,
          "selected" -> value.contains(phase.phase)
        )
    }

    Json.obj("value" -> "", "text" -> "") +: phaseJsonList
  }

}

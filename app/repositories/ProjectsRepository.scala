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

package repositories

import javax.inject.Inject
import models.Project
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import utils.EncryptionTools

import scala.concurrent.{ExecutionContext, Future}

class ProjectsRepository @Inject()(mongo: ReactiveMongoApi,
                                   encryptionTools: EncryptionTools)(implicit ec: ExecutionContext) {

  private val collectionName: String = "projects"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  def createProject(project: Project) = {

    val document = Json.obj(
      "_id" -> encryptionTools.uuid,
      "name" -> project.name,
      "details" -> project.details,
      "phase" -> project.phase.phase
    )

    collection.flatMap {
      _.insert(ordered = false)
        .one(document)
        .map {
          lastError =>
            lastError.ok
        }
    }
  }

  def getProjects: Future[JSONCollection] = {

    val query = Json.obj(
      "name" -> Json.obj("$regex" -> "")
    )

    collection.flatMap{
      _.find(query)
        .cursor[JSONCollection]()
        .collect()
    }
  }

}

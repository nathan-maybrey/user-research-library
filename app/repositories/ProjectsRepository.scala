package repositories

import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi

class ProjectsRepository @Inject()(mongo: ReactiveMongoApi) {

  def getAllProjects = ???

}

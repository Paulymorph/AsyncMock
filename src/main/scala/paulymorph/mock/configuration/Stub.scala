package paulymorph.mock.configuration

import akka.http.scaladsl.server.Route

case class Stub(predicate: Predicate, response: Response) extends Routable {
  override def toRoute: Route = predicate.toDirective(response.toRoute)
}

sealed trait Response extends Routable

case class SimpleResponse(text: String) extends Response {
  import akka.http.scaladsl.server.Directives.complete
  override def toRoute: Route = complete(text)
}
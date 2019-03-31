package paulymorph.mock.configuration

import akka.http.scaladsl.server.{Directive0, Route}

trait Routable {
  def toRoute: Route
}

trait Directable {
  def toDirective: Directive0
}
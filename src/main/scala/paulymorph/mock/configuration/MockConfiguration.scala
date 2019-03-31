package paulymorph.mock.configuration

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait MockConfiguration extends Routable {
  def port: Int
  def stubs: Seq[Stub]
}

case class SimpleMockConfiguration(port: Int, stubs: Seq[Stub]) extends MockConfiguration {
  val protocol = "simple"
  override def toRoute: Route = {
    stubs.map(_.toRoute).fold(reject)(_ ~ _)
  }
}
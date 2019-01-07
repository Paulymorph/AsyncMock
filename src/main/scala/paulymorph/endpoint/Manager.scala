package paulymorph.endpoint

import akka.http.scaladsl.server.Route

import scala.concurrent.Future

trait Manager {
  def startEndpoint(port: Int, route: Route): Future[Unit]
  def stopEndpoint(port: Int): Future[Unit]
  def replaceEndpoint(port: Int, newRoute: Route): Future[Unit]
}

case class PortAlreadyInUse(port: Int) extends RuntimeException {
  override def getMessage: String = s"Port $port is already in use"
}

case class NoEndpointStartedOnPort(port: Int) extends RuntimeException {
  override def getMessage: String = s"There is no endpoint on port $port"
}

package paulymorph.endpoint

import akka.http.scaladsl.server.Route

import scala.concurrent.Future

trait EndpointManager {
  def startEndpoint(port: Int, route: Route): Future[Either[PortAlreadyInUse, Unit]]
  def stopEndpoint(port: Int): Future[Either[NoEndpointStartedOnPort, Unit]]
  def replaceEndpoint(port: Int, newRoute: Route): Future[Either[NoEndpointStartedOnPort, Unit]]
  def startOrReplaceEndpoint(port: Int, route: Route): Future[Unit]
}

case class PortAlreadyInUse(port: Int) extends RuntimeException {
  override def getMessage: String = s"Port $port is already in use"
}

case class NoEndpointStartedOnPort(port: Int) extends RuntimeException {
  override def getMessage: String = s"There is no endpoint on port $port"
}

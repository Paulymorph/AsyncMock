package paulymorph.mock.manager

import paulymorph.mock.configuration.MockConfiguration

import scala.concurrent.Future

trait MockEndpointManager {
  def addMock(mock: MockConfiguration): Future[Unit]
  def deleteMock(port: Int): Future[MockConfiguration]
  def getMock(port: Int): Future[MockConfiguration]
}

case class PortAlreadyInUse(port: Int) extends RuntimeException {
  override def getMessage: String = s"Port $port is already in use"
}

case class NoEndpointStartedOnPort(port: Int) extends RuntimeException {
  override def getMessage: String = s"There is no endpoint on port $port"
}

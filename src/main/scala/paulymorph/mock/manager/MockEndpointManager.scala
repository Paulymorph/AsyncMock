package paulymorph.mock.manager

import paulymorph.mock.configuration.StubConfiguration

import scala.concurrent.Future

trait MockEndpointManager {
  def addMock(mock: StubConfiguration): Future[Unit]
  def deleteMock(port: Int): Future[StubConfiguration]
  def getMock(port: Int): Future[StubConfiguration]
}

case class PortAlreadyInUse(port: Int) extends RuntimeException {
  override def getMessage: String = s"Port $port is already in use"
}

case class NoEndpointStartedOnPort(port: Int) extends RuntimeException {
  override def getMessage: String = s"There is no endpoint on port $port"
}

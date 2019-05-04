package paulymorph

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import paulymorph.mock.configuration.MockConfiguration
import paulymorph.mock.manager.{AdminMockConfigurationManager, EndpointManagerImpl}

import scala.concurrent.Future

class AsyncMock(adminPort: Int, startingMocks: Seq[MockConfiguration] = Seq.empty) {
  implicit val actorSystem = ActorSystem("default")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  def start: Future[Unit] = {
    for {
      _ <- adminMockManager.start
      _ = logger.info(s"Server successfully started! Visit http://localhost:$adminPort/swagger")
    } yield ()
  }

  def stop: Future[Unit] =
    for {
      _ <- adminMockManager.stop
      _ <- actorSystem.terminate()
      _ = logger.info("AsyncMock successfully stopped")
    } yield ()

  private val endpointManager = new EndpointManagerImpl

  private val adminMockManager = AdminMockConfigurationManager(adminPort, endpointManager)

  private val logger = Logger[AsyncMock]
}

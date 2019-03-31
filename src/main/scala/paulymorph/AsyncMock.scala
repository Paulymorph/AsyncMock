package paulymorph

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import paulymorph.mock.manager.AdminMockConfigurationManager

object AsyncMock {
  implicit val actorSystem = ActorSystem("default")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  def main(args: Array[String]): Unit = {
    logger.info("Starting AsyncMock...")

    val adminPort = 2525
    val endpointManager = AdminMockConfigurationManager(adminPort)

    for {
      _ <- endpointManager.start
      _ = logger.info(s"Server successfully started! Visit http://localhost:$adminPort")
    } yield ()
  }

  private val logger = Logger[AsyncMock.type]
}

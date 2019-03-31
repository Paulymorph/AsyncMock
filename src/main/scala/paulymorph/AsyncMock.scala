package paulymorph

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import paulymorph.endpoint.EndpointManagerImpl

object AsyncMock {
  implicit val actorSystem = ActorSystem("default")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  def main(args: Array[String]): Unit = {
    logger.info("Starting AsyncMock...")

    val endpointManager = new EndpointManagerImpl
    val adminRoute = {
      import akka.http.scaladsl.server.Directives.complete
      complete("Hello world!!!")
    }
    val adminPort = 2525

    for {
      adminStartResult <- endpointManager.startEndpoint(adminPort, adminRoute)
      _ = adminStartResult match {
        case Right(_) => logger.info(s"Server successfully started! Visit http://localhost:$adminPort")
        case _ => logger.error(s"Couldn't start admin endpoint on $adminPort")
      }
    } yield ()
  }

  private val logger = Logger[AsyncMock.type]
}

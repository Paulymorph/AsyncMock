package paulymorph

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import paulymorph.mock.configuration.MockConfiguration
import paulymorph.mock.manager.{AdminMockConfigurationManager, EndpointManagerImpl}

import scala.io.Source
import scala.util.control.NonFatal

object AsyncMock {
  implicit val actorSystem = ActorSystem("default")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  def main(args: Array[String]): Unit = {
    logger.info(s"Starting AsyncMock with args $args...")

    handleCommandLineArguments(args)

    val adminPort = 2525
    val adminMockManager = AdminMockConfigurationManager(adminPort, endpointManager)

    for {
      _ <- adminMockManager.start
      _ = logger.info(s"Server successfully started! Visit http://localhost:$adminPort")
    } yield ()
  }

  def handleCommandLineArguments(args: Seq[String]): Unit = {
    args match {
      case Seq() => ()

      case Seq(fileFlag, filePath, tail@_*) if List("-f", "--file").contains(fileFlag) =>
        import io.circe.parser.decode
        import paulymorph.mock.configuration.JsonUtils._
        try {
          val fileContents = Source.fromFile(filePath).mkString
          decode[MockConfiguration](fileContents).foreach {
            endpointManager.addMock(_)
              .foreach(_ => logger.info(s"Added mock from configuration file $filePath"))
          }
        } catch {
          case NonFatal(exception) => logger.warn(s"Couldn't create a mock from file $filePath", exception)
        }
        handleCommandLineArguments(tail)

      case Seq(unknown, tail@_*) =>
        logger.warn(s"$unknown is not recognized")
        handleCommandLineArguments(tail)
    }
  }

  private val endpointManager = new EndpointManagerImpl

  private val logger = Logger[AsyncMock.type]
}

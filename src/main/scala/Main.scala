import com.typesafe.scalalogging.Logger
import paulymorph.AsyncMock
import paulymorph.mock.configuration.stub.MockConfiguration

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.Try

object Main {
  implicit val ec = ExecutionContext.global
  import paulymorph.mock.configuration.json.JsonUtils._

  def main(args: Array[String]): Unit = {
    logger.info(s"Starting AsyncMock with args $args...")

    val startingMocks = handleCommandLineArguments(args)
    val server = new AsyncMock(2525, startingMocks)

    val serverLifetime = for {
      _ <- server.start
      _ = logger.info("Press ENTER to close application")
      _ = scala.io.StdIn.readLine()
      _ = logger.info("Stopping AsyncMock")
      _ <- server.stop
    } yield ()

    serverLifetime.failed.foreach(exception => {
        logger.error(s"Fatal error in execution. Exiting...", exception)
        System.exit(1)
      }
    )
  }

  def handleCommandLineArguments(args: Seq[String]): Seq[MockConfiguration] = {
    args match {
      case Seq() => Seq.empty

      case Seq(fileFlag, filePath, tail@_*) if List("-f", "--file").contains(fileFlag) =>
        import io.circe.parser.decode
        val parsedOpt = Try {
          val fileContents = Source.fromFile(filePath).mkString
          decode[MockConfiguration](fileContents)
            .getOrElse(throw new IllegalArgumentException(s"$filePath is incorrect mock"))
        }.toOption.toSeq

        parsedOpt ++ handleCommandLineArguments(tail)

      case Seq(unknown, tail@_*) =>
        logger.warn(s"$unknown is not recognized")
        handleCommandLineArguments(tail)
    }
  }

  private val logger = Logger[Main.type]
}

package paulymorph.endpoint

import java.net.BindException

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.concurrent.Future

class ManagerImpl(implicit actorSystem: ActorSystem, materializer: Materializer) extends Manager {
  private val portBindings = scala.collection.concurrent.TrieMap.empty[Int, ServerBinding]
  implicit val ec = actorSystem.dispatcher

  override def startEndpoint(port: Int, route: Route): Future[Unit] = {
    val bindingFuture = for {
      binding <- Http().bindAndHandle(route, "localhost", port)
      _ = portBindings += port -> binding
    } yield ()
    bindingFuture.recover {
      case _: BindException => throw PortAlreadyInUse(port)
    }
  }

  override def stopEndpoint(port: Int): Future[Unit] = {
    val binding = portBindings.remove(port).getOrElse(throw NoEndpointStartedOnPort(port))
    binding.unbind().map(_ => ())
  }

  override def replaceEndpoint(port: Int, newRoute: Route): Future[Unit] =
    for {
      _ <- stopEndpoint(port)
      _ <- startEndpoint(port, newRoute)
    } yield ()
}

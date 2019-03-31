package paulymorph.endpoint

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.scalatest.{Assertion, AsyncFlatSpec, Matchers}

import scala.concurrent.Future
import scala.util.Random

class EndpointManagerImpSpec extends AsyncFlatSpec with Matchers {

  type FixtureParam = (Int, EndpointManagerImpl)

  implicit val actorSystem = ActorSystem("test")
  implicit val materializer = ActorMaterializer()
  implicit override val executionContext = actorSystem.dispatcher

  def withWiring(test: FixtureParam => Future[Assertion]) = {
    val manager = new EndpointManagerImpl
    val port = 8000 + Random.nextInt(1000)
    println(s"$port")
    test((port, manager))
      .flatMap { assertion =>
        for {
          _ <- manager.stopEndpoint(port)
        } yield assertion
      }
  }

  val testRoute = akka.http.scaladsl.server.Directives.complete("Hello")

  def testRequest(port: Int) = {
    val uri = Uri(s"http://localhost:$port")
    HttpRequest(uri = uri)
  }

  behavior of "Manager"

  it should "start endpoints on specific ports and successfully stop them" in withWiring { case (port, manager) =>
    for {
      startResult <- manager.startEndpoint(port, testRoute)
      _ = startResult shouldBe 'right
      httpResponse <- Http().singleRequest(testRequest(port))
      answer <- Unmarshal(httpResponse.entity).to[String]
      stopResult <- manager.stopEndpoint(port)
      _ = stopResult shouldBe 'right
    } yield answer shouldBe "Hello"
  }

  it should "return port unavailable trying to start several endpoints on one port" in withWiring {
    case (port, manager) =>
      for {
        a <- manager.startEndpoint(port, testRoute)
        _ = a shouldBe 'right
        startFail <- manager.startEndpoint(port, testRoute)
        _ = startFail shouldBe 'left
        stopResult <- manager.stopEndpoint(port)
      } yield stopResult shouldBe 'right
  }

}

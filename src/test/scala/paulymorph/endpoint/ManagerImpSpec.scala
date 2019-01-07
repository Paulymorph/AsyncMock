package paulymorph.endpoint

import java.net.BindException

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._

class ManagerImpSpec extends FlatSpec with ScalaFutures with Matchers {

  import akka.http.scaladsl.server.Directives._

  implicit val actorSystem = ActorSystem("test")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  override implicit val patienceConfig = PatienceConfig(1 second, 100 millis)

  val testRoute = complete("Hello")
  val port = 8080
  val testRequest = {
    val uri = Uri(s"http://localhost:$port")
    HttpRequest(uri = uri)
  }

  "Manager" should "start endpoints on specific ports and successfully stop them" in new Wiring {
    val eventualResponse = for {
      _ <- manager.startEndpoint(port, testRoute)
      httpResponse <- Http().singleRequest(testRequest)
      answer <- Unmarshal(httpResponse.entity).to[String]
      _ <- manager.stopEndpoint(port)
    } yield answer

    whenReady(eventualResponse) { response =>
      response shouldBe "Hello"
    }
  }

  it should "throw port unavailable trying to start several endpoints on one port" in new Wiring {
    val testFuture = for {
      _ <- manager.startEndpoint(port, testRoute)
      startFail <- manager.startEndpoint(port, testRoute).failed
      _ <- manager.stopEndpoint(port)
    } yield startFail

    whenReady(testFuture) { failure =>
      failure shouldBe a[PortAlreadyInUse]
    }
  }

  trait Wiring {
    val manager = new ManagerImpl
  }

}

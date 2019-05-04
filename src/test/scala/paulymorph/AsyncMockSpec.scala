package paulymorph

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestProbe
import io.circe.Json
import io.circe.parser.decode
import org.scalatest.AsyncFlatSpec
import org.scalatest.concurrent.ScalaFutures
import paulymorph.utils.BaseSpec

import scala.concurrent.Future
import scala.concurrent.duration._


class AsyncMockSpec extends AsyncFlatSpec with BaseSpec with ScalaFutures {
  protected val completeMessage = "complete"

  "AsyncMock SSE" should "handle simple event configuration" in asyncMockTestSse(
    """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "events": [
                  {
                    "data": "543",
                    "id": "123",
                    "eventType": "first"
                  }
                ],
                "type": "sse"
              }
            ]
          }
        ]
      }
    """) { probe =>
    probe.expectMsg(ServerSentEvent("\"543\"", "first", "123"))
  }

  it should "handle mock with delay" in asyncMockTestSse(
    """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "events": [
                  {
                    "data": "543",
                    "id": "123",
                    "eventType": "first",
                    "delay": "1 second"
                  }
                ],
                "type": "sse"
              }
            ]
          }
        ]
      }
    """
  ) { probe =>
    probe.expectNoMessage(900.millis)
    probe.expectMsg(200.millis, ServerSentEvent(data = "\"543\"", `type` = "first", id = "123"))
  }

  it should "handle mock with several events" in asyncMockTestSse(
    """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "events": [
                  {
                    "data": "1"
                  },
                  {
                    "data": "2"
                  }
                ],
                "type": "sse"
              }
            ]
          }
        ]
      }
    """
  ) { probe =>
    probe.expectMsg(ServerSentEvent(data = "\"1\""))
    probe.expectMsg(ServerSentEvent(data = "\"2\""))
  }

  it should "handle mock with JSON in data" in asyncMockTestSse(
    """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "events": [
                  {
                    "data": {"foo": "bar"}
                  }
                ],
                "type": "sse"
              }
            ]
          }
        ]
      }
    """
  ) { probe =>
    val event = probe.expectMsgClass(classOf[ServerSentEvent])
    decode[Json](event.data) shouldBe decode[Json]("""{"foo": "bar"}""")
  }

  it should "cycle responses" in asyncMockTestSse(
    """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "events": [
                  {
                    "data": "1"
                  }
                ],
                "type": "sse"
              },
              {
                "events": [
                  {
                    "data": "2"
                  }
                ],
                "type": "sse"
              }
            ]
          }
        ]
      }
    """
  ) ({ firstProbe =>
    firstProbe.expectMsg(ServerSentEvent(data = "\"1\""))
  }, { secondProbe =>
    secondProbe.expectMsg(ServerSentEvent(data = "\"2\""))
  })

  def asyncMockTestSse(mockConfiguration: String)(responseChecker: (TestProbe => Unit)*) = {
    val server = new AsyncMock(2525)
    val probe = TestProbe()

    def checkResponsesFuture(checks: Seq[TestProbe => Unit]): Future[Unit] = {
      checks match {
        case Nil => Future.successful(())
        case nextCheck +: tail =>
          for {
            _ <- Http().singleRequest(HttpRequest(uri = "http://localhost:5000"))
              .flatMap(Unmarshal(_).to[Source[ServerSentEvent, NotUsed]])
              .map(_.runWith(Sink.actorRef(probe.ref, completeMessage)))
            _ = {
              nextCheck(probe)
              probe.expectMsg(completeMessage)
            }
            _ <- checkResponsesFuture(tail)
          } yield ()
      }
    }

    def testFuture = for {
      response <- Http().singleRequest(HttpRequest(HttpMethods.POST, "http://localhost:2525/mock", entity = HttpEntity(ContentTypes.`application/json`, mockConfiguration)))
      _ = response.status shouldBe StatusCodes.Created
      _ <- checkResponsesFuture(responseChecker)
    } yield succeed

    for {
      _ <- server.start
      testResult <- testFuture.andThen { case _ => server.stop }
    } yield testResult
  }
}
